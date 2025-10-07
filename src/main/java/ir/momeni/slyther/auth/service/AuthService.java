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
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
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

    private static final Pattern STRONG_PWD = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$");

    public String register(RegisterRequest req) {
        if (userRepo.existsByUsername(req.getUsername()))
            throw new IllegalArgumentException("Username already exists");
        if (!STRONG_PWD.matcher(req.getPassword()).matches())
            throw new IllegalArgumentException("Password too weak");

        Role roleUser = roleRepo.findByName("ROLE_USER")
                .orElseGet(() -> roleRepo.save(Role.builder().name("ROLE_USER").build()));

        User u = User.builder()
                .username(req.getUsername())
                .password(encoder.encode(req.getPassword()))
                .roles(Set.of(roleUser))
                .enabled(true)
                .build();
        userRepo.save(u);
        return "User created";
    }

    public TokenResponse login(LoginRequest req, String userAgent, String ip) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword()));

        User u = (User) auth.getPrincipal();

        Map<String, Object> claims = Map.of("roles",
                u.getAuthorities().stream().map(Object::toString).toList());

        String access = jwtService.generateAccessToken(u.getUsername(), claims);

        String refresh = UUID.randomUUID().toString() + "-" + UUID.randomUUID();
        Instant exp = Instant.now().plusSeconds(props.getSecurity().getJwt().getRefreshExpDays() * 86400L);

        sessionService.create(Session.builder()
                .user(u)
                .refreshTokenHash(ir.momeni.slyther.common.util.HashUtils.sha256Hex(refresh))
                .expiresAt(exp)
                .userAgent(userAgent)
                .ipAddress(ip)
                .build());

        return TokenResponse.builder()
                .tokenType("Bearer")
                .accessToken(access)
                .refreshToken(refresh)
                .expiresInSeconds(props.getSecurity().getJwt().getAccessExpMins() * 60L)
                .build();
    }

    public TokenResponse refresh(RefreshRequest req) {
        // اعتبارسنجی توکن قبلی
        var oldSession = sessionService.validateActiveRawToken(req.getRefreshToken());
        var u = oldSession.getUser();

        // rotate: revoke قبلی + ساخت جدید
        sessionService.revokeRawToken(req.getRefreshToken());

        String newRefresh = UUID.randomUUID().toString() + "-" + UUID.randomUUID();
        Instant exp = Instant.now().plusSeconds(props.getSecurity().getJwt().getRefreshExpDays() * 86400L);
        sessionService.create(Session.builder()
                .user(u)
                .refreshTokenHash(ir.momeni.slyther.common.util.HashUtils.sha256Hex(newRefresh))
                .expiresAt(exp)
                .userAgent(oldSession.getUserAgent())
                .ipAddress(oldSession.getIpAddress())
                .build());

        Map<String, Object> claims = Map.of("roles", u.getAuthorities().stream().map(Object::toString).toList());
        String access = jwtService.generateAccessToken(u.getUsername(), claims);

        return TokenResponse.builder()
                .tokenType("Bearer")
                .accessToken(access)
                .refreshToken(newRefresh) // رفرش جدید
                .expiresInSeconds(props.getSecurity().getJwt().getAccessExpMins() * 60L)
                .build();
    }

    public void logout(String refreshToken) {
        sessionService.revokeRawToken(refreshToken);
    }
}
