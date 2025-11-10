package ir.momeni.slyther.auth.service;

import ir.momeni.slyther.auth.dto.ForgotPasswordRequest;
import ir.momeni.slyther.auth.dto.ResetPasswordRequest;
import ir.momeni.slyther.auth.entity.PasswordResetToken;
import ir.momeni.slyther.auth.repository.PasswordResetTokenRepository;
import ir.momeni.slyther.common.util.HashUtils;
import ir.momeni.slyther.user.entity.User;
import ir.momeni.slyther.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;
import java.util.regex.Pattern;


/**
 * Handles password reset flow including:
 * - Creating short-lived password reset tokens
 * - Validating reset token and updating password
 *
 * Security:
 * - Tokens are stored hashed in DB (raw shown only once to client)
 * - Tokens expire automatically after a short timeout
 * - Tokens can only be used once
 */
@Service
@RequiredArgsConstructor
public class PasswordResetService {
    private final UserRepository userRepo;
    private final PasswordResetTokenRepository prtRepo;
    private final PasswordEncoder encoder;


    /** Basic password strength rule: ≥8 chars, upper/lowercase + digits required. */
    private static final Pattern STRONG_PWD = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$");


    /**
     * Initiates password reset procedure by:
     * - Verifying account existence via email
     * - Generating a random reset token
     * - Hashing and storing token in DB with 15 min validity
     * - Returning raw token (dev mode only)
     *
     * @return raw password reset token (should be sent via email/SMS in production)
     */
    public String forgot(ForgotPasswordRequest req) {
        User u = userRepo.findByEmail(req.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Create a secure random reset token
        String token = UUID.randomUUID().toString() + "-" + UUID.randomUUID();
        String hash = HashUtils.sha256Hex(token);
        Instant exp = Instant.now().plusSeconds(15 * 60);

        // Store hashed token bound to the user
        prtRepo.save(PasswordResetToken.builder()
                .user(u).tokenHash(hash).expiresAt(exp).build());

        return token; // Production: send via email/SMS, not in API response
    }


    /**
     * Completes password reset:
     * - Validates password strength
     * - Validates token via SHA-256 hash match
     * - Validates token activity (not expired and not used)
     * - Updates password and marks token as used
     *
     * Uses @Transactional to ensure both DB updates occur atomically.
     */
    @Transactional
    public void reset(ResetPasswordRequest req) {

        // Enforce password rules before processing
        if (!STRONG_PWD.matcher(req.getNewPassword()).matches()) {
            throw new IllegalArgumentException("Password too weak");
        }
        String hash = HashUtils.sha256Hex(req.getToken());
        var prt = prtRepo.findByTokenHash(hash)
                .orElseThrow(() -> new IllegalArgumentException("Invalid token"));
        if (!prt.isActive()) throw new IllegalStateException("Token expired/used");


        // Update user password and mark token as used
        User u = prt.getUser();
        u.setPassword(encoder.encode(req.getNewPassword()));
        prt.setUsed(true);

        userRepo.save(u);
        prtRepo.save(prt);
    }
}
