package ir.momeni.slyther.audit.repository;

import ir.momeni.slyther.audit.entity.ActionLog;
import org.springframework.data.jpa.repository.JpaRepository;


/**
 * Repository interface for performing CRUD operations on {@link ActionLog} entities.
 * <p>
 * Extends Spring Data JPA's {@link JpaRepository} to automatically provide:
 * - Basic CRUD operations (save, findAll, findById, delete, etc.)
 * - Paging and sorting capabilities
 * <p>
 * No additional methods are defined here, but custom query methods can be added if needed.
 */
public interface ActionLogRepository extends JpaRepository<ActionLog, Long> { }
