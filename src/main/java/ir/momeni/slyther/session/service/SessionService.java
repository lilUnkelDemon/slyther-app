package ir.momeni.slyther.session.service;

import ir.momeni.slyther.common.util.HashUtils;
import ir.momeni.slyther.session.entity.Session;
import ir.momeni.slyther.session.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SessionService {
    private final SessionRepository repo;

    public void create(Session s) {
        repo.save(s);
    }

    public Session validateActiveRawToken(String rawRefreshToken) {
        String hash = HashUtils.sha256Hex(rawRefreshToken);
        var s = repo.findByRefreshTokenHash(hash)
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));
        if (!s.isActive()) throw new IllegalStateException("Refresh token expired/revoked");
        return s;
    }

    public void revokeRawToken(String rawRefreshToken) {
        String hash = HashUtils.sha256Hex(rawRefreshToken);
        repo.findByRefreshTokenHash(hash).ifPresent(s -> {
            s.setRevoked(true);
            repo.save(s);
        });
    }
}
