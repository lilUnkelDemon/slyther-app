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

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserRepository userRepo;

    // مسیرهایی که نباید وارد فیلتر JWT شوند
    private static final Set<String> SKIP_PREFIXES = Set.of(
            "/api/auth",        // login/register/refresh/forgot-password
            "/v3/api-docs",     // swagger JSON + groups (/v3/api-docs/core)
            "/swagger-ui",      // UI
            "/swagger-ui.html", // UI alias
            "/actuator"         // health و ...
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest req) {
        final String path = req.getRequestURI();
        final String method = req.getMethod();

        // preflight اصلاً وارد زنجیره JWT نشود
        if (HttpMethod.OPTIONS.matches(method)) return true;

        // swagger/openapi/actuator/auth را کامل اسکیپ کن
        for (String pfx : SKIP_PREFIXES) {
            if (path.startsWith(pfx)) return true;
        }
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        // اگر از قبل احراز شده‌ایم، کاری نکن
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
                // توکن نامعتبر: auth ست نمی‌شود؛ مسیرهای محافظت‌شده بعداً 401 می‌دهند.
            }
        }

        chain.doFilter(req, res);
    }
}
