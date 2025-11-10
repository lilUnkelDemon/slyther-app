// src/main/java/ir/momeni/slyther/config/FlywayRepairOnce.java
package ir.momeni.slyther.config;

import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * Custom Flyway startup configuration.
 *
 * When the application starts, Flyway will:
 *   1️⃣ Run {@code repair()}:
 *        - Rebuild checksums if migration files have changed
 *        - Fix corrupted schema history records
 *
 *   2️⃣ Run {@code migrate()}:
 *        - Apply any pending database migrations
 *
 * This helps avoid startup failures caused by:
 *   • Modified migration scripts (checksum mismatch)
 *   • Manually-edited database states
 *
 * ⚠ Should be used carefully in production environments!
 *    Flyway `repair()` can hide important migration issues if misused.
 */
@Configuration
public class FlywayRepairOnce {


    /**
     * Registers a FlywayMigrationStrategy bean to modify Flyway’s startup behavior.
     *
     * @return strategy that repairs schema history before applying migrations
     */
    @Bean
    public FlywayMigrationStrategy repairThenMigrate() {
        return flyway -> {
            flyway.repair();   // Sync checksums with current migration files
            flyway.migrate();  // Execute any missing migrations
        };
    }
}
