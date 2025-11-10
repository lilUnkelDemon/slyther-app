package ir.momeni.slyther.user.service;

import ir.momeni.slyther.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


/**
 * Service class responsible for user-related operations.
 * <p>
 * Implements {@link UserDetailsService} to integrate with Spring Security’s
 * authentication system. Handles user retrieval and password encoding.
 */
@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    /** Repository for accessing user data from the database. */
    private final UserRepository repo;

    /** Password encoder used for securely hashing user passwords. */
    private final PasswordEncoder encoder;


    /**
     * Loads a user by their username for authentication purposes.
     *
     * @param username the username to look up
     * @return a {@link UserDetails} instance for Spring Security
     * @throws UsernameNotFoundException if no matching user is found
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return repo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }


    /**
     * Encodes a raw password string using the configured {@link PasswordEncoder}.
     *
     * @param raw the plain-text password
     * @return the encoded (hashed) password
     */
    public String encode(String raw) { return encoder.encode(raw); }
}
