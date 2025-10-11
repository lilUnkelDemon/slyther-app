package ir.momeni.slyther.session.entity;

import ir.momeni.slyther.common.BaseEntity;
import ir.momeni.slyther.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity @Table(name = "sessions", indexes = {
        @Index(name="idx_session_refresh_hash", columnList = "refreshTokenHash", unique = true),
        @Index(name="idx_session_user", columnList = "user_id")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Session extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name="user_id", nullable = false)
    private User user;

    @Column(name = "refresh_token_hash",nullable = false, unique = true, length = 64)
    private String refreshTokenHash;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "revoked", nullable = false)
    private boolean revoked = false;

    public boolean isActive() {
        return !revoked && Instant.now().isBefore(expiresAt);
    }
}
