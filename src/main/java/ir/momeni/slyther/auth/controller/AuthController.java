package ir.momeni.slyther.auth.controller;

import ir.momeni.slyther.audit.entity.ActionLog;
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

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final PasswordResetService passwordResetService;
    private final ActionLogService logService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid RegisterRequest req) {
        return ResponseEntity.ok(authService.register(req));
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody @Valid LoginRequest req,
                                               @RequestHeader(value = "User-Agent", required = false) String ua,
                                               @RequestHeader(value = "X-Forwarded-For", required = false) String xff,
                                               @RequestHeader(value = "X-Real-IP", required = false) String rip) {
        String ip = (xff != null) ? xff.split(",")[0].trim() : (rip != null ? rip : null);
        return ResponseEntity.ok(authService.login(req, ua, ip));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@RequestBody @Valid RefreshRequest req) {
        return ResponseEntity.ok(authService.refresh(req));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logout(@RequestBody @Valid RefreshRequest req) {
        authService.logout(req.getRefreshToken());
        return ResponseEntity.ok(new ApiResponse(true, "Successfully logged out"));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgot(@RequestBody @Valid ForgotPasswordRequest req) {
        String token = passwordResetService.forgot(req);
        // در تولید واقعی: ارسال ایمیل/SMS. اینجا برای توسعه نمایش می‌دهیم:
        return ResponseEntity.ok(Map.of("success", true, "token_dev_mode_only", token));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse> reset(@RequestBody @Valid ResetPasswordRequest req) {
        passwordResetService.reset(req);
        return ResponseEntity.ok(new ApiResponse(true,"Reset password successful"));
    }
}
