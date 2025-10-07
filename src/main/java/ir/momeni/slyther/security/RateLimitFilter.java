package ir.momeni.slyther.security;

import ir.momeni.slyther.config.AppProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final AppProperties props;

    private static class Window {
        volatile long windowStartSec;
        AtomicInteger count = new AtomicInteger(0);
    }
    private final Map<String, Window> buckets = new ConcurrentHashMap<>();

    // مسیرهایی که همیشه باید از ریت‌لیمیت خارج باشند
    private static final Set<String> ALWAYS_SKIP_PREFIXES = Set.of(
            "/v3/api-docs", "/swagger-ui", "/swagger-ui.html", "/actuator"
    );

    // فقط این دو endpoint آن هم با متد POST هدف ریت‌لیمیت هستند
    private static final String LOGIN_PATH = "/api/auth/login";
    private static final String FORGOT_PATH = "/api/auth/forgot-password";

    @Override
    protected boolean shouldNotFilter(HttpServletRequest req) {
        final String path = req.getRequestURI();
        final String method = req.getMethod();

        // پیش‌فلایت و متدهای غیر POST اصلاً وارد ریت‌لیمیت نشن
        if (!HttpMethod.POST.matches(method)) return true;
        if (HttpMethod.OPTIONS.matches(method)) return true;

        // Swagger/OpenAPI/Actuator هرگز محدود نشوند
        for (String pfx : ALWAYS_SKIP_PREFIXES) {
            if (path.startsWith(pfx)) return true;
        }

        // فقط POST روی این دو مسیر فیلتر شود
        return !(LOGIN_PATH.equals(path) || FORGOT_PATH.equals(path));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        final String path = req.getRequestURI();
        final var rl = LOGIN_PATH.equals(path)
                ? props.getSecurity().getRatelimit().getLogin()
                : props.getSecurity().getRatelimit().getForgotPassword();

        final int limit = rl.getMaxRequests();
        final int windowSec = rl.getWindowSeconds();

        String ip = extractClientIp(req);
        final String key = path + "|" + ip;
        final long nowSec = Instant.now().getEpochSecond();

        Window w = buckets.computeIfAbsent(key, k -> {
            Window nw = new Window();
            nw.windowStartSec = nowSec;
            return nw;
        });

        synchronized (w) {
            if (nowSec - w.windowStartSec >= windowSec) {
                w.windowStartSec = nowSec;
                w.count.set(0);
            }
            int c = w.count.incrementAndGet();
            if (c > limit) {
                int retry = (int) Math.max(1, windowSec - (nowSec - w.windowStartSec));
                res.setStatus(429);
                res.setHeader("Retry-After", String.valueOf(retry));
                res.setContentType(MediaType.APPLICATION_JSON_VALUE);
                res.getWriter().write(
                        "{\"error\":\"too_many_requests\",\"message\":\"Rate limit exceeded\",\"retryAfterSeconds\":" + retry + "}"
                );
                return;
            }
        }

        chain.doFilter(req, res);
    }

    private String extractClientIp(HttpServletRequest req) {
        String ip = req.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isBlank()) ip = ip.split(",")[0].trim();
        if (ip == null || ip.isBlank()) ip = req.getHeader("X-Real-IP");
        if (ip == null || ip.isBlank()) ip = req.getRemoteAddr();
        return ip;
    }
}
