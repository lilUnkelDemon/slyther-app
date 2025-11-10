package ir.momeni.slyther.role.repository;

import ir.momeni.slyther.role.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;



/**
 * Repository interface for managing {@link Role} entities.
 * <p>
 * Extends {@link JpaRepository} to inherit basic CRUD operations,
 * pagination, and sorting functionality.
 * Provides additional lookup and existence checks by role name.
 */
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Retrieves a role by its unique name.
     *
     * @param name the name of the role (e.g., "ROLE_ADMIN")
     * @return an {@link Optional} containing the role if found, or empty if not
     */
    Optional<Role> findByName(String name);

    /**
     * Checks whether a role with the given name already exists in the database.
     *
     * @param name the role name to check (case-sensitive)
     * @return true if the role exists, false otherwise
     */
    boolean existsByName(String name);
}
