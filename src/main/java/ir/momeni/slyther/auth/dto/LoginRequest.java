package ir.momeni.slyther.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

/**
 * Request payload for user login authentication.
 * <p>
 * Credentials are validated using {@link jakarta.validation.constraints.NotBlank}
 * to ensure both fields are provided.
 */
@Getter
public class LoginRequest {

    /** Username provided during login — required (not blank). */
    @NotBlank
    private String username;

    /** Plain-text password provided during login — required (not blank). */
    @NotBlank
    private String password;
}
