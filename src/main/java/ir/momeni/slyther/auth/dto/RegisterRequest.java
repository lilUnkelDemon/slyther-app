package ir.momeni.slyther.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

/**
 * Request payload for creating a new user account.
 * <p>
 * All fields are required. Validation is enforced before processing the request.
 */
@Getter
public class RegisterRequest {

    /** Desired username for the new account — must be unique and not blank. */
    @NotBlank
    private String username;

    /** Email address for the new account — must be valid and unique in the system. */
    @NotBlank
    private String email;

    /** Plain-text password for account creation — will be encoded before storage. */
    @NotBlank
    private String password;
}
