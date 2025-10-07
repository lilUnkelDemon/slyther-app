package ir.momeni.slyther.config;

import ir.momeni.slyther.role.entity.Role;
import ir.momeni.slyther.role.repository.RoleRepository;
import ir.momeni.slyther.user.entity.User;
import ir.momeni.slyther.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@Configuration
@RequiredArgsConstructor
public class DataSeeder {
    private final PasswordEncoder encoder;

    @Bean
    CommandLineRunner seed(RoleRepository roleRepo, UserRepository userRepo) {
        return args -> {
            Role rUser  = roleRepo.findByName("ROLE_USER").orElseGet(() -> roleRepo.save(Role.builder().name("ROLE_USER").build()));
            Role rAdmin = roleRepo.findByName("ROLE_ADMIN").orElseGet(() -> roleRepo.save(Role.builder().name("ROLE_ADMIN").build()));
            Role rSudo  = roleRepo.findByName("ROLE_SUDO").orElseGet(() -> roleRepo.save(Role.builder().name("ROLE_SUDO").build()));

            if (!userRepo.existsByUsername("user")) {
                userRepo.save(User.builder()
                        .username("user")
                        .password(encoder.encode("123456"))
                        .roles(Set.of(rUser))
                        .enabled(true).build());
            }
            if (!userRepo.existsByUsername("admin")) {
                userRepo.save(User.builder()
                        .username("admin")
                        .password(encoder.encode("123456"))
                        .roles(Set.of(rAdmin))
                        .enabled(true).build());
            }
            if (!userRepo.existsByUsername("sudo")) {
                userRepo.save(User.builder()
                        .username("sudo")
                        .password(encoder.encode("123456"))
                        .roles(Set.of(rUser, rAdmin, rSudo))
                        .enabled(true).build());
            }
        };
    }
}
