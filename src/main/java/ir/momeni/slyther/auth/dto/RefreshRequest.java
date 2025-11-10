package ir.momeni.slyther.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

/**
 * Request payload for refreshing the access token.
 * <p>
 * Contains the refresh token provided during login, used to issue a new access token.
 */
@Getter
public class RefreshRequest {

    /** Refresh token (raw value) — required to be present and non-blank. */
    @NotBlank
    private String refreshToken;
}
