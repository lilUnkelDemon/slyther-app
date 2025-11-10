package ir.momeni.slyther.session.repository;

import ir.momeni.slyther.session.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;


/**
 * Repository for managing {@link Session} entities.
 *
 * <p>Extends {@link JpaRepository} to provide standard CRUD, paging, and sorting operations.
 * Includes a convenience finder for locating a session by its refresh token hash.
 */
public interface SessionRepository extends JpaRepository<Session, Long> {

    /**
     * Finds a session by the stored refresh token hash.
     * <p>
     * Note: Only hashes of refresh tokens should be stored; never persist raw tokens.
     *
     * @param refreshTokenHash the (hashed) refresh token value
     * @return an {@link Optional} containing the matching {@link Session}, if present
     */
    Optional<Session> findByRefreshTokenHash(String refreshTokenHash);
}
