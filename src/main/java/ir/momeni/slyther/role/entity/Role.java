package ir.momeni.slyther.role.entity;

import ir.momeni.slyther.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;


/**
 * JPA entity representing an application role/authority.
 * <p>
 * Typical naming convention is "ROLE_XYZ" (e.g., ROLE_ADMIN, ROLE_USER)
 * to align with Spring Security's {@code hasRole('XYZ')} checks.
 * Inherits common fields (e.g., id, timestamps) from {@link BaseEntity}.
 */
@Entity @Table(name = "roles")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Role extends BaseEntity {

    /** Canonical role name (unique), usually prefixed with "ROLE_". */
    @Column(unique = true, nullable = false, length = 64)
    private String name;
}
