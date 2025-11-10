package ir.momeni.slyther.auth.repository;

import ir.momeni.slyther.auth.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository for managing {@link PasswordResetToken} entities.
 * <p>
 * Provides database access for password reset tokens using Spring Data JPA.
 */
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    /**
     * Finds a password reset token record by its stored hashed value.
     *
     * @param tokenHash SHA-256 hash of the raw reset token
     * @return an {@link Optional} containing the matching token if found
     */
    Optional<PasswordResetToken> findByTokenHash(String tokenHash);
}
