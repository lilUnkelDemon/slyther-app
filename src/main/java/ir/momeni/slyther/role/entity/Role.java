package ir.momeni.slyther.role.entity;

import ir.momeni.slyther.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "roles")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Role extends BaseEntity {
    @Column(unique = true, nullable = false, length = 64)
    private String name;
}
