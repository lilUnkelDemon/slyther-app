package ir.momeni.slyther.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;


/**
 * Request DTO for initiating a "forgot password" flow.
 * <p>
 * Contains the registered email address that will receive a password reset token.
 */

@Getter
public class ForgotPasswordRequest {

    /**
     * Email of the user requesting password reset. Must not be blank.
     */
    @NotBlank
    private String email;
}
