package ir.momeni.slyther.auth.service;

import ir.momeni.slyther.audit.service.ActionLogService;
import ir.momeni.slyther.auth.dto.LoginRequest;
import ir.momeni.slyther.auth.dto.RefreshRequest;
import ir.momeni.slyther.auth.dto.RegisterRequest;
import ir.momeni.slyther.auth.dto.TokenResponse;
import ir.momeni.slyther.config.AppProperties;
import ir.momeni.slyther.role.entity.Role;
import ir.momeni.slyther.role.repository.RoleRepository;
import ir.momeni.slyther.security.JwtService;
import ir.momeni.slyther.session.entity.Session;
import ir.momeni.slyther.session.service.SessionService;
import ir.momeni.slyther.user.entity.User;
import ir.momeni.slyther.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;


/**
 * Authentication service containing core login, registration,
 * refresh-token rotation, logout, and security validation logic.
 *
 * Responsibilities:
 * - Validate credentials and user creation rules
 * - Generate signed JWT access tokens
 * - Create/revoke refresh token sessions
 * - Enforce password strength policy
 * - Log security events
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    /** Password strength rule: min 8 chars, 1 uppercase, 1 lowercase, 1 digit */
    private static final Pattern STRONG_PWD = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$");
    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder encoder;
    private final SessionService sessionService;
    private final AppProperties props;
    private final ActionLogService logService;


    /**
     * Handles registration of a new user.
     * Validates:
     * - Username unique
     * - Email unique
     * - Password strength (simple regex rule)
     *
     * Automatically assigns ROLE_USER to all new accounts.
     */
    public Map<String, String> register(RegisterRequest req) {
        if (userRepo.existsByUsername(req.getUsername())) throw new IllegalArgumentException("Username already exists");
        if (userRepo.existsByEmail(req.getEmail())) throw new IllegalArgumentException("Email already exists");
        if (!STRONG_PWD.matcher(req.getPassword()).matches()) throw new IllegalArgumentException("Password too weak");

        // Ensure ROLE_USER exists
        Role roleUser = roleRepo.findByName("ROLE_USER").orElseGet(() -> roleRepo.save(Role.builder().name("ROLE_USER").build()));


        // Create user record
        User u = User.builder().username(req.getUsername()).email(req.getEmail()).password(encoder.encode(req.getPassword())).roles(Set.of(roleUser)).enabled(true).build();
        userRepo.save(u);

        // Manual audit log entry
        logService.info("User registered", "/api/auth/register", req.getUsername(), null);

        return Map.of("success", "true", "msg", "user created");
    }


    /**
     * Authenticates user credentials, issues:
     * - New JWT access token
     * - New refresh token
     *
     * Refresh token hash is stored in DB as an active session.
     * Session stores:
     * - User, expiration date, IP, User-Agent.
     */
    public TokenResponse login(LoginRequest req, String userAgent, String ip) {

        // Audit: login attempt
        logService.info("Login attempt", "/api/auth/login", req.getUsername(), ip);

        try {
            Authentication auth = authManager.authenticate(new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword()));

            User u = (User) auth.getPrincipal();

            Map<String, Object> claims = Map.of("roles", u.getAuthorities().stream().map(Object::toString).toList());

            // Generate access token
            String access = jwtService.generateAccessToken(u.getUsername(), claims);

            // Generate and hash refresh token
            String refresh = UUID.randomUUID() + "-" + UUID.randomUUID();
            String refreshHash = ir.momeni.slyther.common.util.HashUtils.sha256Hex(refresh);
            if (refreshHash == null || refreshHash.isBlank()) {
                throw new AuthenticationServiceException("refreshTokenHash is null/blank");
            }

            // Calculate refresh-token expiration timestamp
            Instant exp = Instant.now().plusSeconds(props.getSecurity().getJwt().getRefreshExpDays() * 86400L);

            // Store hashed refresh token as session
            sessionService.create(Session.builder().user(u).refreshTokenHash(refreshHash).expiresAt(exp).userAgent(userAgent).ipAddress(ip).build());

            // Audit: successful login
            logService.infoHttp("Login success", "POST", 200, "/api/auth/login", u.getUsername(), ip);

            return TokenResponse.builder()
                    .tokenType("Bearer")
                    .accessToken(access)
                    .refreshToken(refresh) // returned raw — hash is stored
                    .expiresInSeconds(props.getSecurity().getJwt().getAccessExpMins() * 60L).build();
        } catch (org.springframework.security.core.AuthenticationException ex) {

            // Audit: login failed
            logService.errorHttp("Login failed", ex, "POST", 400, "/api/auth/login", req.getUsername(), ip);
            throw ex;
        }
    }


    /**
     * Rotates refresh token and issues a new access token
     * using the ROTATE-ON-USE security model:
     *
     * - Validate and revoke old session
     * - Create a new replacement refresh token session
     * - Issue a fresh JWT access token
     */
    public TokenResponse refresh(RefreshRequest req) {
        var oldSession = sessionService.validateActiveRawToken(req.getRefreshToken());

        var u = oldSession.getUser();

        // Security: Rotating refresh tokens prevents reuse
        sessionService.revokeRawToken(req.getRefreshToken());

        String newRefresh = UUID.randomUUID() + "-" + UUID.randomUUID();
        String newHash = ir.momeni.slyther.common.util.HashUtils.sha256Hex(newRefresh);
        Instant exp = Instant.now().plusSeconds(props.getSecurity().getJwt().getRefreshExpDays() * 86400L);

        sessionService.create(Session.builder().user(u).refreshTokenHash(newHash).expiresAt(exp).userAgent(oldSession.getUserAgent()).ipAddress(oldSession.getIpAddress()).build());

        Map<String, Object> claims = Map.of("roles", u.getAuthorities().stream().map(Object::toString).toList());
        String access = jwtService.generateAccessToken(u.getUsername(), claims);

        // Audit entry
        logService.info("Token refreshed", "/api/auth/refresh", u.getUsername(), oldSession.getIpAddress());

        return TokenResponse.builder().tokenType("Bearer").accessToken(access).refreshToken(newRefresh).expiresInSeconds(props.getSecurity().getJwt().getAccessExpMins() * 60L).build();
    }


    /**
     * Revokes refresh token session, effectively logging user out.
     */
    public void logout(String refreshToken) {
        sessionService.revokeRawToken(refreshToken);
        logService.info("Logged out", "/api/auth/logout", null, null);
    }
}
