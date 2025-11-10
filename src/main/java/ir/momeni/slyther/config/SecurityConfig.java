package ir.momeni.slyther.config;

import ir.momeni.slyther.security.JwtAuthFilter;
import ir.momeni.slyther.security.RateLimitFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;



/**
 * Main Spring Security configuration.
 *
 * ✅ Stateless JWT authentication (no HTTP sessions)
 * ✅ Method-level security enabled via @PreAuthorize, etc.
 * ✅ Separate security rules for Swagger/OpenAPI/Actuator vs. API endpoints
 * ✅ Includes rate-limiting and JWT filters in the request chain
 */
@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;     // Extracts auth from JWT tokens
    private final UserDetailsService userDetailsService;
    private final RateLimitFilter rateLimitFilter; // Applies login/forgot-password throttling


    /**
     * 1️⃣ Security chain ONLY for Swagger / OpenAPI / Actuator endpoints.
     *    These must always be fully accessible without authentication,
     *    and processed BEFORE the main API filter chain.
     */
    @Order(1)
    @Bean
    SecurityFilterChain swaggerChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/v3/api-docs/**", "/swagger-ui.html", "/swagger-ui/**", "/actuator/**", "/error")
                .csrf(cs -> cs.disable())
                .authorizeHttpRequests(reg -> reg.anyRequest().permitAll());
        return http.build();
    }


    /**
     * 2️⃣ Main Security chain for API endpoints requiring JWT authentication.
     *    Stateless configuration → no HTTP session will be created.
     */
    @Bean
    @Order(2)
    SecurityFilterChain apiChain(HttpSecurity http, DaoAuthenticationProvider provider) throws Exception {
        http
                .csrf(cs -> cs.disable()) // API uses JWT → no CSRF needed
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(reg -> reg
                        .requestMatchers("/api/auth/**", "/api/test/public").permitAll() // Public endpoints
                        .anyRequest().authenticated() // Everything else requires JWT
                )
                .authenticationProvider(provider)

                // Apply rate limiting BEFORE authentication attempts
                .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)

                // Apply JWT authentication BEFORE username/password auth
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }


    /**
     * Authentication provider using stored user details + BCrypt password hashing.
     */    @Bean
    DaoAuthenticationProvider daoProvider(PasswordEncoder passwordEncoder) {
        var p = new DaoAuthenticationProvider();
        p.setUserDetailsService(userDetailsService);
        p.setPasswordEncoder(passwordEncoder);
        return p;
    }


    /**
     * Retrieves AuthenticationManager from Spring Boot auto-configuration.
     */
    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }



    /**
     * Completely bypass security filters for documentation endpoints.
     * (Performance improvement — they do not even hit the filter chain)
     */
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring().requestMatchers(
                "/v3/api-docs/**",
                "/swagger-ui/**",
                "/swagger-ui.html",
                "/actuator/**",
                "/error"
        );
    }

}
