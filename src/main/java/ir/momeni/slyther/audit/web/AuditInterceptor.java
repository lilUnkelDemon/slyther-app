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
    private final ActionLogService logService;

    @Override
    public void afterCompletion(HttpServletRequest req, HttpServletResponse res, Object handler, Exception ex) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(String.valueOf(auth.getPrincipal())))
                ? auth.getName() : null;

        String ip = firstNonNull(firstForwardedIp(req.getHeader("X-Forwarded-For")),
                req.getHeader("X-Real-IP"),
                req.getRemoteAddr());
        String ua = req.getHeader("User-Agent");
        int status = res.getStatus();

        String summary = req.getMethod() + " " + req.getRequestURI() + " -> " + status;

        String errMsg = null;
        if (ex != null) {
            errMsg = ex.getMessage();
        } else if (status >= 400) {
            Object attr = req.getAttribute("errorMessage");
            if (attr != null) errMsg = String.valueOf(attr);
            if (errMsg == null && res instanceof ContentCachingResponseWrapper w) {
                byte[] buf = w.getContentAsByteArray();
                if (buf != null && buf.length > 0) {
                    errMsg = new String(buf, 0, Math.min(buf.length, 1000), java.nio.charset.StandardCharsets.UTF_8);
                }
            }
        }

        ActionLog log = ActionLog.builder()
                .username(username)
                .method(req.getMethod())
                .path(req.getRequestURI())
                .ip(ip)
                .userAgent(ua)
                .status(status)
                .success(ex == null && status < 400)
                .msg(mask(summary))                         // msg دیگر خالی نیست
                .errorMessage(mask(truncate(errMsg, 500)))  // تلاش برای پر کردن خطا
                .build();

        logService.save(log);
    }

    private static String firstForwardedIp(String xff) {
        if (xff == null || xff.isBlank()) return null;
        return xff.split(",")[0].trim();
    }
    private static String firstNonNull(String... s) {
        for (String v: s) if (v != null && !v.isBlank()) return v;
        return null;
    }
    private static String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() > max ? s.substring(0, max) : s;
    }
    private String mask(String msg) {
        if (msg == null) return null;
        msg = msg.replaceAll("(?i)password=\\S+", "password=***");
        msg = msg.replaceAll("(?i)\"password\"\\s*:\\s*\"[^\"]*\"", "\"password\":\"***\"");
        msg = msg.replaceAll("(?i)Authorization:\\s*Bearer\\s+\\S+", "Authorization: Bearer ***");
        return msg;
    }
}
