package ir.momeni.slyther.audit.web;

import ir.momeni.slyther.audit.entity.ActionLog;
import ir.momeni.slyther.audit.service.ActionLogService;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class AuditInterceptor implements HandlerInterceptor {
    private final ActionLogService logService;

    @Override
    public void afterCompletion(HttpServletRequest req, HttpServletResponse res, Object handler, Exception ex) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(String.valueOf(auth.getPrincipal())))
                ? auth.getName() : null;

        String ip = req.getHeader("X-Forwarded-For");
        if (ip != null) ip = ip.split(",")[0].trim();
        if (ip == null) ip = req.getHeader("X-Real-IP");
        if (ip == null) ip = req.getRemoteAddr();

        String ua = req.getHeader("User-Agent");
        int status = res.getStatus();

        ActionLog log = ActionLog.builder()
                .username(username)
                .method(req.getMethod())
                .path(req.getRequestURI())
                .ip(ip)
                .userAgent(ua)
                .status(status)
                .success(ex == null && status < 400)
                .errorMessage(mask(ex != null ? ex.getMessage() : null))
                .build();
        logService.save(log);
    }

    private String mask(String msg) {
        if (msg == null) return null;
        msg = msg.replaceAll("(?i)password=\\S+", "password=***");
        msg = msg.replaceAll("(?i)Authorization: Bearer \\S+", "Authorization: Bearer ***");
        return msg.length() > 500 ? msg.substring(0, 500) : msg;
    }
}
