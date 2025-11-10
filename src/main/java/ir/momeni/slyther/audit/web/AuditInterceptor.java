package ir.momeni.slyther.audit.web;

import ir.momeni.slyther.audit.entity.ActionLog;
import ir.momeni.slyther.audit.service.ActionLogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.ContentCachingResponseWrapper;

@Component
@RequiredArgsConstructor
public class AuditInterceptor implements HandlerInterceptor {

    // Service responsible for persisting audit entries
    private final ActionLogService logService;

    /**
     * Called by Spring MVC after request completion (view rendered or exception thrown).
     * Builds an {@link ActionLog} from the request/response context and persists it.
     */
    @Override
    public void afterCompletion(HttpServletRequest req, HttpServletResponse res, Object handler, Exception ex) {
        // Resolve authenticated username if present and not anonymous
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(String.valueOf(auth.getPrincipal())))
                ? auth.getName() : null;

        // Best-effort client IP extraction: prefer first X-Forwarded-For, then X-Real-IP, then remote addr
        String ip = firstNonNull(firstForwardedIp(req.getHeader("X-Forwarded-For")),
                req.getHeader("X-Real-IP"),
                req.getRemoteAddr());

        // Capture User-Agent and HTTP status
        String ua = req.getHeader("User-Agent");
        int status = res.getStatus();

        // Short human-readable summary like: "GET /api/items -> 200"
        String summary = req.getMethod() + " " + req.getRequestURI() + " -> " + status;

        // Collect error message when an exception occurred or status indicates an error
        String errMsg = null;
        if (ex != null) {
            // Prefer exception message when available
            errMsg = ex.getMessage();
        } else if (status >= 400) {

            // Optional error message set by downstream code on the request
            Object attr = req.getAttribute("errorMessage");
            if (attr != null) errMsg = String.valueOf(attr);

            // Fallback: if the response is cached, peek at body content (e.g., error JSON/message)
            if (errMsg == null && res instanceof ContentCachingResponseWrapper w) {
                byte[] buf = w.getContentAsByteArray();
                if (buf != null && buf.length > 0) {

                    // Limit to 1000 bytes to avoid storing huge payloads in logs
                    errMsg = new String(buf, 0, Math.min(buf.length, 1000), java.nio.charset.StandardCharsets.UTF_8);
                }
            }
        }


        // Build the audit record with masking and truncation safeguards
        ActionLog log = ActionLog.builder()
                .username(username)
                .method(req.getMethod())
                .path(req.getRequestURI())
                .ip(ip)
                .userAgent(ua)
                .status(status)
                .success(ex == null && status < 400)        // Success when no exception and 2xx/3xx status
                .msg(mask(summary))                         // msg is no longer empty
                .errorMessage(mask(truncate(errMsg, 500)))  // Attempt to fill the error
                .build();

        // Persist asynchronously/synchronously depending on service impl
        logService.save(log);
    }

    /**
     * Extract the first IP from X-Forwarded-For header (client's original IP).
     */
    private static String firstForwardedIp(String xff) {
        if (xff == null || xff.isBlank()) return null;
        return xff.split(",")[0].trim();
    }

    /**
     * Return the first non-null/non-blank string from the given arguments.
     */
    private static String firstNonNull(String... s) {
        for (String v: s) if (v != null && !v.isBlank()) return v;
        return null;
    }

    /**
     * Safely truncate a string to a maximum length.
     */
    private static String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() > max ? s.substring(0, max) : s;
    }

    /**
     * Mask sensitive tokens/credentials commonly appearing in logs.
     * - password=<value> (query/form)
     * - "password":"value" (JSON)
     * - Authorization: Bearer <token> (headers)
     */
    private String mask(String msg) {
        if (msg == null) return null;
        msg = msg.replaceAll("(?i)password=\\S+", "password=***");
        msg = msg.replaceAll("(?i)\"password\"\\s*:\\s*\"[^\"]*\"", "\"password\":\"***\"");
        msg = msg.replaceAll("(?i)Authorization:\\s*Bearer\\s+\\S+", "Authorization: Bearer ***");
        return msg;
    }
}
