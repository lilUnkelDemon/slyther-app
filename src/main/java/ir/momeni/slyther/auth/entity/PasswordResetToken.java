package ir.momeni.slyther.auth.entity;

import ir.momeni.slyther.common.BaseEntity;
import ir.momeni.slyther.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Entity representing a password reset token tied to a specific user.
 * <p>
 * Stored data:
 * - SHA-256 hash of the raw reset token (never the raw token itself)
 * - Expiration timestamp
 * - Whether the token has already been used
 * <p>
 * Business rules:
 * - A token is considered active only if not used and not expired.
 * - Only one reset token may exist with the same hash (unique index).
 */
@Entity
@Table(
        name = "password_reset_tokens",
        indexes = {
                @Index(name = "idx_prt_hash", columnList = "token_hash", unique = true),
                @Index(name = "idx_prt_user", columnList = "user_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordResetToken extends BaseEntity {

    /** The user requesting password reset — many tokens may be associated with one user. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Hashed reset token (SHA-256). Raw token is only shown to the user once. */
    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    /** When the token expires and becomes invalid for resetting password. */
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    /** Indicates whether the token has already been used for a password reset. */
    @Builder.Default
    private boolean used = false;

    /**
     * Checks if the token is currently valid for use:
     * - Not already used
     * - Not expired
     *
     * @return true if token is still valid
     */
    public boolean isActive() {
        return !used && Instant.now().isBefore(expiresAt);
    }
}
