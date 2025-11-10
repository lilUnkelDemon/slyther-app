package ir.momeni.slyther.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

/**
 * Request payload for resetting a user’s password.
 * <p>
 * The token verifies the validity of the password reset request,
 * and the new password will replace the old one after validation.
 */
@Getter
public class ResetPasswordRequest {

    /** Reset token issued during the "forgot password" step — required. */
    @NotBlank
    private String token;

    /** New plain-text password to update — required. */
    @NotBlank
    private String newPassword;
}
