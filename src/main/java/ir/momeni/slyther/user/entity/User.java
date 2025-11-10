package ir.momeni.slyther.user.entity;

import ir.momeni.slyther.common.BaseEntity;
import ir.momeni.slyther.role.entity.Role;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;
import java.util.stream.Collectors;


/**
 * JPA entity representing an application user.
 * <p>
 * Extends {@link BaseEntity} for common fields (e.g., id, audit timestamps) and
 * implements {@link UserDetails} to integrate with Spring Security.
 */
@Entity @Table(name = "users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User extends BaseEntity implements UserDetails {

    /** Unique username used for login and identification. */
    @Column(unique = true, nullable = false, length = 150)
    private String username;

    /** Unique email address for the user (also used for lookup/notifications). */
    @Column(unique = true, nullable = false, length = 150)
    private String email;


    /** Encoded (hashed) password. Never store plain-text passwords. */
    @Column(nullable = false)
    private String password;


    /** Whether the user account is enabled/active. Defaults to true. */
    @Builder.Default
    private boolean enabled = true;


    /**
     * Roles assigned to the user (e.g., ADMIN, USER).
     * <p>
     * EAGER fetch ensures authorities are available when Spring Security loads the user.
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name="user_id"),
            inverseJoinColumns = @JoinColumn(name="role_id"))
    private Set<Role> roles;



    /**
     * Translates the user's roles into {@link GrantedAuthority} objects for Spring Security.
     * Returns an empty list if the user has no roles.
     */
    @Override public Collection<? extends GrantedAuthority> getAuthorities() {
        if (roles == null) return List.of();
        return roles.stream().map(r -> new SimpleGrantedAuthority(r.getName())).collect(Collectors.toSet());
    }

    /** Accounts do not expire in this model. */
    @Override public boolean isAccountNonExpired() { return true; }

    /** Accounts are not locked in this model. */
    @Override public boolean isAccountNonLocked() { return true; }

    /** Credentials do not expire in this model. */
    @Override public boolean isCredentialsNonExpired() { return true; }

    /** Mirrors the {@link #enabled} flag for Spring Security. */
    @Override public boolean isEnabled() { return enabled; }
}
