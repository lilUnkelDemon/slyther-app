package ir.momeni.slyther.auth.service;

import ir.momeni.slyther.auth.dto.ForgotPasswordRequest;
import ir.momeni.slyther.auth.dto.ResetPasswordRequest;
import ir.momeni.slyther.auth.entity.PasswordResetToken;
import ir.momeni.slyther.auth.repository.PasswordResetTokenRepository;
import ir.momeni.slyther.common.util.HashUtils;
import ir.momeni.slyther.user.entity.User;
import ir.momeni.slyther.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class PasswordResetService {
    private final UserRepository userRepo;
    private final PasswordResetTokenRepository prtRepo;
    private final PasswordEncoder encoder;

    private static final Pattern STRONG_PWD = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$");

    public String forgot(ForgotPasswordRequest req) {
        User u = userRepo.findByUsername(req.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String token = UUID.randomUUID().toString() + "-" + UUID.randomUUID();
        String hash = HashUtils.sha256Hex(token);
        Instant exp = Instant.now().plusSeconds(15 * 60);

        prtRepo.save(PasswordResetToken.builder()
                .user(u).tokenHash(hash).expiresAt(exp).build());

        return token; // در تولید: ایمیل/SMS
    }

    public void reset(ResetPasswordRequest req) {
        if (!STRONG_PWD.matcher(req.getNewPassword()).matches()) {
            throw new IllegalArgumentException("Password too weak");
        }
        String hash = HashUtils.sha256Hex(req.getToken());
        var prt = prtRepo.findByTokenHash(hash)
                .orElseThrow(() -> new IllegalArgumentException("Invalid token"));
        if (!prt.isActive()) throw new IllegalStateException("Token expired/used");

        User u = prt.getUser();
        u.setPassword(encoder.encode(req.getNewPassword()));
        prt.setUsed(true);

        userRepo.save(u);
        prtRepo.save(prt);
    }
}
