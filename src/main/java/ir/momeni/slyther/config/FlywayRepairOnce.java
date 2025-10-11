// src/main/java/ir/momeni/slyther/config/FlywayRepairOnce.java
package ir.momeni.slyther.config;

import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FlywayRepairOnce {
    @Bean
    public FlywayMigrationStrategy repairThenMigrate() {
        return flyway -> {
            flyway.repair();   // چک‌سام‌ها را با فایل‌های فعلی sync می‌کند
            flyway.migrate();  // سپس مایگریشن‌ها را اجرا می‌کند
        };
    }
}
