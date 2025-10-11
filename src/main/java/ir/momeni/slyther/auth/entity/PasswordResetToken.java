package ir.momeni.slyther.auth.entity;

import ir.momeni.slyther.common.BaseEntity;
import ir.momeni.slyther.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity @Table(name = "password_reset_tokens", indexes = {
        @Index(name="idx_prt_hash", columnList = "token_hash", unique = true),
        @Index(name="idx_prt_user", columnList = "user_id")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PasswordResetToken extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name="user_id", nullable = false)
    private User user;

    @Column(name = "token_hash",nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(name = "expires_at",nullable = false)
    private Instant expiresAt;

    @Builder.Default
    private boolean used = false;

    public boolean isActive() {
        return !used && Instant.now().isBefore(expiresAt);
    }
}
