package ir.momeni.slyther.auth.controller;

import ir.momeni.slyther.audit.service.ActionLogService;
import ir.momeni.slyther.auth.dto.*;
import ir.momeni.slyther.auth.service.AuthService;
import ir.momeni.slyther.auth.service.PasswordResetService;
import ir.momeni.slyther.common.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


/**
 * Authentication and user lifecycle controller.
 * <p>
 * Provides endpoints for:
 * - Register
 * - Login (JWT + refresh token session creation)
 * - Refresh access token
 * - Logout (refresh token revocation)
 * - Forgot password (token issue)
 * - Reset password (token validation)
 * <p>
 * Security:
 * - JWT authentication is handled by JwtAuthFilter
 * - RateLimitFilter applies for login and forgot-password
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final PasswordResetService passwordResetService;
    private final ActionLogService logService; // currently unused here, used inside interceptor


    /**
     * Register a new user account.
     *
     * @param req user registration details (validated DTO)
     * @return newly created user info or auth tokens based on implementation
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid RegisterRequest req) {
        return ResponseEntity.ok(authService.register(req));
    }


    /**
     * Login endpoint for user authentication.
     * <p>
     * Returns: JWT access token + refresh token for session continuity.
     * IP/User-Agent forwarded to {@link AuthService} for session context.
     */
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody @Valid LoginRequest req, @RequestHeader(value = "User-Agent", required = false) String ua, @RequestHeader(value = "X-Forwarded-For", required = false) String xff, @RequestHeader(value = "X-Real-IP", required = false) String rip) {

        // Preferred client IP resolution: XFF first → X-Real-IP → null
        String ip = (xff != null) ? xff.split(",")[0].trim() : (rip);
        return ResponseEntity.ok(authService.login(req, ua, ip));
    }


    /**
     * Refresh the access token using the refresh token.
     *
     * @param req valid refresh token request
     * @return new access token + potentially new refresh token
     */
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@RequestBody @Valid RefreshRequest req) {
        return ResponseEntity.ok(authService.refresh(req));
    }


    /**
     * Logout a user by revoking the refresh token.
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logout(@RequestBody @Valid RefreshRequest req) {
        authService.logout(req.getRefreshToken());
        return ResponseEntity.ok(new ApiResponse(true, "Successfully logged out"));
    }


    /**
     * Forgot password request:
     * Issues a reset token linked to the registered email.
     * In production:
     * - This should be sent via email or SMS instead of returned in response.
     *
     * @return response with reset token for development/testing
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgot(@RequestBody @Valid ForgotPasswordRequest req) {
        String token = passwordResetService.forgot(req);

        // DEV-ONLY: In production, send via email/SMS instead of returning
        return ResponseEntity.ok(Map.of("success", true, "token_dev_mode_only", token));
    }


    /**
     * Reset the user's password using a valid reset token.
     */
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse> reset(@RequestBody @Valid ResetPasswordRequest req) {
        passwordResetService.reset(req);
        return ResponseEntity.ok(new ApiResponse(true, "Reset password successful"));
    }
}
