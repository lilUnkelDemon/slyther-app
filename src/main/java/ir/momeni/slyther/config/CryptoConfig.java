package ir.momeni.slyther.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;


/**
 * Spring configuration for providing cryptographic utilities.
 *
 * Defines a {@link PasswordEncoder} bean used for securely hashing user passwords
 * before storage. BCrypt applies a computational cost (work factor) to ensure
 * security against brute-force attacks.
 */
@Configuration
public class CryptoConfig {

    /**
     * BCrypt-based password hashing.
     *
     * @return PasswordEncoder with a strength factor of 12
     *         (recommended for production environments,
     *         higher = stronger but more CPU usage)
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
