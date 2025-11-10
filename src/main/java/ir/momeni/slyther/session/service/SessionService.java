package ir.momeni.slyther.session.service;

import ir.momeni.slyther.common.util.HashUtils;
import ir.momeni.slyther.session.entity.Session;
import ir.momeni.slyther.session.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


/**
 * Service layer for managing refresh-token sessions.
 * <p>
 * Stores only the SHA-256 hash of refresh tokens (never the raw token) and
 * provides helpers to validate and revoke sessions based on a raw token value.
 */
@Service
@RequiredArgsConstructor
public class SessionService {


    /** Data-access repository for {@link Session} entities. */
    private final SessionRepository repo;


    /**
     * Persists a new {@link Session}.
     * Assumes the {@code refreshTokenHash} has already been computed on the entity.
     *
     * @param s the session to store
     */
    public void create(Session s) {
        repo.save(s);
    }


    /**
     * Validates that a raw refresh token corresponds to an existing, active session.
     * <p>
     * Steps:
     * 1) Hash the provided raw token using SHA-256.
     * 2) Lookup the session by hash.
     * 3) Ensure the session is active (not revoked and not expired).
     *
     * @param rawRefreshToken the plain refresh token provided by the client
     * @return the matching {@link Session} if valid and active
     * @throws IllegalArgumentException if the token does not match any session
     * @throws IllegalStateException if the session is found but expired or revoked
     */
    public Session validateActiveRawToken(String rawRefreshToken) {
        String hash = HashUtils.sha256Hex(rawRefreshToken);
        var s = repo.findByRefreshTokenHash(hash)
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));
        if (!s.isActive()) throw new IllegalStateException("Refresh token expired/revoked");
        return s;
    }


    /**
     * Revokes a session identified by the raw refresh token.
     * <p>
     * Hashes the token, finds the session, marks it as revoked, and saves the change.
     * If no matching session is found, this is a no-op.
     *
     * @param rawRefreshToken the plain refresh token to revoke
     */
    public void revokeRawToken(String rawRefreshToken) {
        String hash = HashUtils.sha256Hex(rawRefreshToken);
        repo.findByRefreshTokenHash(hash).ifPresent(s -> {
            s.setRevoked(true);
            repo.save(s);
        });
    }
}
