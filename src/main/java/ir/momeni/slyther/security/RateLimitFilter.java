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


/**
 * Simple in-memory sliding-window-ish (fixed window per second) rate limiter for selected endpoints.
 * <p>
 * Scope:
 * - Applies only to POST /api/auth/login and POST /api/auth/forgot-password.
 * - Skips Swagger/OpenAPI/Actuator paths and all non-POST or CORS preflight (OPTIONS) requests.
 * <p>
 * Implementation details:
 * - Uses a per-(path|clientIp) fixed window with a counter reset every {@code windowSeconds}.
 * - Counters are stored in a concurrent map; per-window mutations are synchronized on the Window object.
 * - When the limit is exceeded, responds with HTTP 429 and a Retry-After header in seconds.
 * <p>
 * Notes:
 * - This is local-node memory only; if you run behind multiple instances, you likely want a shared store (e.g., Redis).
 * - Buckets can grow with unique client IPs; consider eviction if needed for long-running processes.
 * - The windowing strategy is "fixed window" (not token bucket / leaky bucket); bursts at window boundaries may pass.
 */
@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final AppProperties props;


    /** Per-client window state: window start (seconds) + atomic request count. */
    private static class Window {
        volatile long windowStartSec;
        AtomicInteger count = new AtomicInteger(0);
    }


    /** Buckets keyed by "path|ip". Concurrent for multi-threaded servlet container access. */
    private final Map<String, Window> buckets = new ConcurrentHashMap<>();

    // Paths that must always be excluded from rate limiting
    private static final Set<String> ALWAYS_SKIP_PREFIXES = Set.of(
            "/v3/api-docs", "/swagger-ui", "/swagger-ui.html", "/actuator"
    );

    // Only these two endpoints (and only with POST) are rate limited
    private static final String LOGIN_PATH = "/api/auth/login";
    private static final String FORGOT_PATH = "/api/auth/forgot-password";


    /**
     * Decide whether to bypass this filter for the current request.
     * <p>
     * Skips:
     * - Any method other than POST
     * - OPTIONS (CORS preflight)
     * - Swagger/OpenAPI/Actuator paths
     * Applies only to:
     * - POST /api/auth/login
     * - POST /api/auth/forgot-password
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest req) {
        final String path = req.getRequestURI();
        final String method = req.getMethod();

        // Do not rate limit non-POST methods and preflight OPTIONS
        if (!HttpMethod.POST.matches(method)) return true;
        if (HttpMethod.OPTIONS.matches(method)) return true;

        // Exclude Swagger/OpenAPI/Actuator completely
        for (String pfx : ALWAYS_SKIP_PREFIXES) {
            if (path.startsWith(pfx)) return true;
        }

        // Only POST on these two paths should be filtered
        return !(LOGIN_PATH.equals(path) || FORGOT_PATH.equals(path));
    }


    /**
     * Enforces the per-(path|ip) rate limit using a fixed time window.
     * On limit exceed:
     * - Returns 429 Too Many Requests
     * - Sets Retry-After (seconds)
     * - Provides a small JSON body with error details
     */
    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        final String path = req.getRequestURI();


        // Choose rate-limit bucket configuration based on the exact path
        final var rl = LOGIN_PATH.equals(path)
                ? props.getSecurity().getRatelimit().getLogin()
                : props.getSecurity().getRatelimit().getForgotPassword();

        final int limit = rl.getMaxRequests();
        final int windowSec = rl.getWindowSeconds();


        // Derive client IP (prefers X-Forwarded-For, then X-Real-IP, then remote addr)
        String ip = extractClientIp(req);
        final String key = path + "|" + ip;
        final long nowSec = Instant.now().getEpochSecond();


        // Initialize or fetch the current window for this (path|ip)
        Window w = buckets.computeIfAbsent(key, k -> {
            Window nw = new Window();
            nw.windowStartSec = nowSec;
            return nw;
        });


        // Synchronize per-window to ensure atomic reset+increment semantics
        synchronized (w) {


            // Reset the window if the current window has elapsed
            if (nowSec - w.windowStartSec >= windowSec) {
                w.windowStartSec = nowSec;
                w.count.set(0);
            }

            // Increment and compare against limit
            int c = w.count.incrementAndGet();
            if (c > limit) {

                // Compute remaining seconds in the current window for Retry-After
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


        // Continue the chain for requests within the limit
        chain.doFilter(req, res);
    }


    /**
     * Attempts to extract the client IP in a proxy-friendly way:
     * - First IP from X-Forwarded-For (if present)
     * - Else X-Real-IP
     * - Else request remote address
     * <p>
     * Note: Trusting these headers assumes they are set by a trusted reverse proxy.
     */
    private String extractClientIp(HttpServletRequest req) {
        String ip = req.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isBlank()) ip = ip.split(",")[0].trim();
        if (ip == null || ip.isBlank()) ip = req.getHeader("X-Real-IP");
        if (ip == null || ip.isBlank()) ip = req.getRemoteAddr();
        return ip;
    }
}
