package ir.momeni.slyther.session.entity;

import ir.momeni.slyther.common.BaseEntity;
import ir.momeni.slyther.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;


/**
 * Represents a refresh-session entry tied to a specific {@link User}.
 * <p>
 * Each record stores a hashed refresh token (never the raw token), its expiry,
 * and basic client context (user agent and IP). Sessions can be revoked and
 * are considered active only when not revoked and not expired.
 */
@Entity @Table(name = "sessions", indexes = {

        // Unique index to ensure a given refresh token hash is stored only once
        @Index(name="idx_session_refresh_hash", columnList = "refreshTokenHash", unique = true),

        // Index to speed up lookups by user
        @Index(name="idx_session_user", columnList = "user_id")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Session extends BaseEntity {


    /** Owning user of this session (many sessions may belong to one user). */
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name="user_id", nullable = false)
    private User user;


    /** Hash of the refresh token (store only hashes, never raw tokens). */
    @Column(name = "refresh_token_hash",nullable = false, unique = true, length = 64)
    private String refreshTokenHash;


    /** Absolute expiration time for this session's refresh token. */
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;


    /** Optional client User-Agent string captured when the session was created. */
    @Column(name = "user_agent")
    private String userAgent;


    /** Optional IP address from which the session was issued. */
    @Column(name = "ip_address")
    private String ipAddress;


    /** Soft-revocation flag; when true, the session should no longer be accepted. */
    @Column(name = "revoked", nullable = false)
    private boolean revoked = false;



    /**
     * Indicates whether the session is currently valid.
     * A session is active when it is not revoked and its expiry is in the future.
     *
     * @return true if not revoked and not expired; false otherwise
     */
    public boolean isActive() {
        return !revoked && Instant.now().isBefore(expiresAt);
    }
}
