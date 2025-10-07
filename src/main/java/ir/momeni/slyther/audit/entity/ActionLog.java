package ir.momeni.slyther.audit.entity;

import ir.momeni.slyther.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "action_logs", indexes = {
        @Index(name="idx_log_path", columnList = "path"),
        @Index(name="idx_log_username", columnList = "username")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ActionLog extends BaseEntity {

    @Column(length = 150)
    private String username;

    @Column(nullable = false, length = 16)
    private String method;

    @Column(nullable = false, length = 255)
    private String path;

    @Column(length = 46)
    private String ip;

    @Column(length = 255)
    private String userAgent;

    @Column(nullable = false)
    private int status;

    @Builder.Default
    private boolean success = true;

    @Column(length = 512)
    private String errorMessage;
}
