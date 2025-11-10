package ir.momeni.slyther.config;

import ir.momeni.slyther.role.entity.Role;
import ir.momeni.slyther.role.repository.RoleRepository;
import ir.momeni.slyther.user.entity.User;
import ir.momeni.slyther.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;


/**
 * Seeds initial roles and default users into the database on application startup.
 * <p>
 * This class ensures core security entities exist even when the database is empty,
 * making development and first-time deployments easier.
 */
@Configuration
@RequiredArgsConstructor
public class DataSeeder {

    /** Password hashing utility injected from CryptoConfig */
    private final PasswordEncoder encoder;


    /**
     * CommandLineRunner that executes once after the application context is loaded.
     *
     * @param roleRepo repository for managing Role entities
     * @param userRepo repository for managing User entities
     * @return a runner that seeds initial data if missing
     */
    @Bean
    CommandLineRunner seed(RoleRepository roleRepo, UserRepository userRepo) {
        return args -> {
            // Ensure base roles exist
            Role rUser  = roleRepo.findByName("ROLE_USER").orElseGet(() -> roleRepo.save(Role.builder().name("ROLE_USER").build()));
            Role rAdmin = roleRepo.findByName("ROLE_ADMIN").orElseGet(() -> roleRepo.save(Role.builder().name("ROLE_ADMIN").build()));
            Role rSudo  = roleRepo.findByName("ROLE_SUDO").orElseGet(() -> roleRepo.save(Role.builder().name("ROLE_SUDO").build()));


            // Create default users ONLY if they do not already exist
            if (!userRepo.existsByUsername("user")) {
                userRepo.save(User.builder()
                        .username("user")
                                .email("user@email.com")
                        .password(encoder.encode("123456")) // Weak: Dev-only usage
                        .roles(Set.of(rUser))
                        .enabled(true).build());
            }
            if (!userRepo.existsByUsername("admin")) {
                userRepo.save(User.builder()
                        .username("admin")
                                .email("admin@email.com")
                        .password(encoder.encode("123456"))
                        .roles(Set.of(rAdmin))
                        .enabled(true).build());
            }
            if (!userRepo.existsByUsername("sudo")) {
                userRepo.save(User.builder()
                        .username("sudo")
                                .email("sudo@email.com")
                        .password(encoder.encode("123456"))
                        .roles(Set.of(rUser, rAdmin, rSudo)) // Full access user
                        .enabled(true).build());
            }
        };
    }
}
