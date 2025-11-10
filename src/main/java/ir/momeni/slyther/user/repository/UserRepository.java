package ir.momeni.slyther.user.repository;

import ir.momeni.slyther.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Repository interface for managing {@link User} entities.
 * <p>
 * Extends {@link JpaRepository} to inherit common CRUD operations,
 * pagination, and query derivation support from Spring Data JPA.
 * Provides additional methods for user lookups by username or email.
 */
public interface UserRepository extends JpaRepository<User, Long> {


    /**
     * Retrieves a user by their username.
     *
     * @param username the username to search for
     * @return an {@link Optional} containing the user if found, or empty otherwise
     */
    Optional<User> findByUsername(String username);

    /**
     * Retrieves a user by their email address.
     *
     * @param email the email to search for
     * @return an {@link Optional} containing the user if found, or empty otherwise
     */
    Optional<User> findByEmail(String email);

    /**
     * Checks if a user with the given username already exists.
     *
     * @param username the username to check
     * @return true if a user with that username exists, false otherwise
     */
    boolean existsByUsername(String username);


    /**
     * Checks if a user with the given email address already exists.
     *
     * @param email the email to check
     * @return true if a user with that email exists, false otherwise
     */
    boolean existsByEmail(String email);
}
