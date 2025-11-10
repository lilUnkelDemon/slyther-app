package ir.momeni.slyther.security;

import ir.momeni.slyther.user.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;


/**
 * Servlet filter that authenticates requests based on a JWT in the Authorization header.
 * <p>
 * Behavior:
 * - Skips certain public/infra paths and CORS preflight (OPTIONS) requests.
 * - If a Bearer token is present, parses it and loads the corresponding user.
 * - On success, sets a {@link UsernamePasswordAuthenticationToken} in the {@link SecurityContextHolder}.
 * - On failure (invalid/expired token), it does not authenticate; protected routes will later return 401.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserRepository userRepo;

    // Paths that must not go through the JWT filter
    private static final Set<String> SKIP_PREFIXES = Set.of(
            "/api/auth",        // login/register/refresh/forgot-password
            "/v3/api-docs",     // swagger JSON + groups (/v3/api-docs/core)
            "/swagger-ui",      // Swagger UI
            "/swagger-ui.html", // UI alias
            "/actuator"         // health, etc.
    );


    /**
     * Determines whether this request should bypass the filter entirely.
     * <p>
     * Skips:
     * - CORS preflight (OPTIONS)
     * - Swagger/OpenAPI, actuator, and auth endpoints (as listed in {@link #SKIP_PREFIXES})
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest req) {
        final String path = req.getRequestURI();
        final String method = req.getMethod();

        // Do not run the JWT chain for preflight requests
        if (HttpMethod.OPTIONS.matches(method)) return true;

        // Completely skip swagger/openapi/actuator/auth
        for (String pfx : SKIP_PREFIXES) {
            if (path.startsWith(pfx)) return true;
        }
        return false;
    }


    /**
     * Core filtering logic:
     * - If already authenticated, continue the chain as-is.
     * - Otherwise, try to extract and validate a Bearer token.
     * - On valid token: load the user and set authentication in the security context.
     * - On invalid token: ignore (no auth set), the downstream security layer will handle 401s.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        // If we are already authenticated, do nothing
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            chain.doFilter(req, res);
            return;
        }

        String header = req.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                var claims = jwtService.parse(token).getBody();
                var username = claims.getSubject();
                userRepo.findByUsername(username).ifPresent(user -> {
                    var auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                });
            } catch (Exception ignored) {
                // Invalid token: auth is not set; protected endpoints will later return 401.
            }
        }

        chain.doFilter(req, res);
    }
}
