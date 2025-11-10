package ir.momeni.slyther.audit.entity;

import ir.momeni.slyther.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;



/**
 * Audit log entity for recording HTTP actions performed in the system.
 * <p>
 * Inherits common fields (e.g., id, timestamps) from {@link BaseEntity}.
 * Each record represents a single request/response cycle and its outcome.
 */
@Entity
@Table(
        name = "action_logs",
        indexes = {
                // Index on request path to speed up path-based searches/filters
                @Index(name = "idx_log_path", columnList = "path"),
                // Index on username to speed up per-user auditing
                @Index(name = "idx_log_username", columnList = "username")
        }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ActionLog extends BaseEntity {

    /** Username associated with the action (may be null/anonymous). Max length 150. */
    @Column(length = 150)
    private String username;

    /** HTTP method (e.g., GET, POST, PUT, DELETE). Required. Max length 16. */
    @Column(nullable = false, length = 16)
    private String method;

    /** Request path/URI that was accessed. Required. Max length 255. */
    @Column(nullable = false, length = 255)
    private String path;

    /** Client IP address (supports IPv4/IPv6). Max length 46. */
    @Column(length = 46)
    private String ip;

    /** Raw User-Agent header string. Max length 255. */
    @Column(length = 255)
    private String userAgent;

    /** HTTP response status code (e.g., 200, 403, 500). Required. */
    @Column(nullable = false)
    private int status;

    /**
     * Convenience flag indicating whether the action was successful.
     * Defaults to true; set to false for error cases (non-2xx/expected failures).
     */
    @Builder.Default
    private boolean success = true;

    /** Optional error message captured when the action fails. Max length 512. */
    @Column(length = 512)
    private String errorMessage;

    /** Optional free-form message/notes regarding the action. Max length 512. */
    @Column(length = 512)
    private String msg;
}
