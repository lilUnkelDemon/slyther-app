package ir.momeni.slyther.auth.service;

import ir.momeni.slyther.auth.dto.*;
import ir.momeni.slyther.config.AppProperties;
import ir.momeni.slyther.role.entity.Role;
import ir.momeni.slyther.role.repository.RoleRepository;
import ir.momeni.slyther.security.JwtService;
import ir.momeni.slyther.session.entity.Session;
import ir.momeni.slyther.session.service.SessionService;
import ir.momeni.slyther.user.entity.User;
import ir.momeni.slyther.user.repository.UserRepository;
import ir.momeni.slyther.audit.service.ActionLogService;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder encoder;
    private final SessionService sessionService;
    private final AppProperties props;
    private final ActionLogService logService;

    private static final Pattern STRONG_PWD = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$");

    public Map<String, String> register(RegisterRequest req) {
        if (userRepo.existsByUsername(req.getUsername()))
            throw new IllegalArgumentException("Username already exists");
        if (userRepo.existsByEmail(req.getEmail()))
            throw new IllegalArgumentException("Email already exists");
        if (!STRONG_PWD.matcher(req.getPassword()).matches())
            throw new IllegalArgumentException("Password too weak");

        Role roleUser = roleRepo.findByName("ROLE_USER")
                .orElseGet(() -> roleRepo.save(Role.builder().name("ROLE_USER").build()));

        User u = User.builder()
                .username(req.getUsername())
                .email(req.getEmail())
                .password(encoder.encode(req.getPassword()))
                .roles(Set.of(roleUser))
                .enabled(true)
                .build();
        userRepo.save(u);

        // لاگ دستی
        logService.info("User registered", "/api/auth/register", req.getUsername(), null);

        return Map.of("success","true","msg","user created");
    }

    public TokenResponse login(LoginRequest req, String userAgent, String ip) {
        // لاگ تلاش لاگین
        logService.info("Login attempt", "/api/auth/login", req.getUsername(), ip);

        try {
            Authentication auth = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword()));

            User u = (User) auth.getPrincipal();

            Map<String, Object> claims = Map.of("roles",
                    u.getAuthorities().stream().map(Object::toString).toList());

            String access = jwtService.generateAccessToken(u.getUsername(), claims);

            // تولید رفرش‌توکن و هش
            String refresh = UUID.randomUUID().toString() + "-" + UUID.randomUUID();
            String refreshHash = ir.momeni.slyther.common.util.HashUtils.sha256Hex(refresh);
            if (refreshHash == null || refreshHash.isBlank()) {
                throw new AuthenticationServiceException("refreshTokenHash is null/blank");
            }

            Instant exp = Instant.now().plusSeconds(props.getSecurity().getJwt().getRefreshExpDays() * 86400L);

            // ذخیره سشن با هش
            sessionService.create(Session.builder()
                    .user(u)
                    .refreshTokenHash(refreshHash)
                    .expiresAt(exp)
                    .userAgent(userAgent)
                    .ipAddress(ip)
                    .build());

            // لاگ موفق
            logService.infoHttp("Login success","POST", 200,"/api/auth/login", u.getUsername(), ip);

            return TokenResponse.builder()
                    .tokenType("Bearer")
                    .accessToken(access)
                    .refreshToken(refresh) // خام برای کلاینت؛ هش در DB
                    .expiresInSeconds(props.getSecurity().getJwt().getAccessExpMins() * 60L)
                    .build();
        } catch (org.springframework.security.core.AuthenticationException ex) {
            // لاگ خطا
            logService.errorHttp("Login failed", ex,"POST" ,400,"/api/auth/login", req.getUsername(), ip);
            throw ex;
        }
    }

    public TokenResponse refresh(RefreshRequest req) {
        var oldSession = sessionService.validateActiveRawToken(req.getRefreshToken());
        var u = oldSession.getUser();

        // rotate
        sessionService.revokeRawToken(req.getRefreshToken());

        String newRefresh = UUID.randomUUID().toString() + "-" + UUID.randomUUID();
        String newHash = ir.momeni.slyther.common.util.HashUtils.sha256Hex(newRefresh);
        Instant exp = Instant.now().plusSeconds(props.getSecurity().getJwt().getRefreshExpDays() * 86400L);

        sessionService.create(Session.builder()
                .user(u)
                .refreshTokenHash(newHash)
                .expiresAt(exp)
                .userAgent(oldSession.getUserAgent())
                .ipAddress(oldSession.getIpAddress())
                .build());

        Map<String, Object> claims = Map.of("roles", u.getAuthorities().stream().map(Object::toString).toList());
        String access = jwtService.generateAccessToken(u.getUsername(), claims);

        // لاگ دستی
        logService.info("Token refreshed", "/api/auth/refresh", u.getUsername(), oldSession.getIpAddress());

        return TokenResponse.builder()
                .tokenType("Bearer")
                .accessToken(access)
                .refreshToken(newRefresh)
                .expiresInSeconds(props.getSecurity().getJwt().getAccessExpMins() * 60L)
                .build();
    }

    public void logout(String refreshToken) {
        sessionService.revokeRawToken(refreshToken);
        logService.info("Logged out", "/api/auth/logout", null, null);
    }
}
