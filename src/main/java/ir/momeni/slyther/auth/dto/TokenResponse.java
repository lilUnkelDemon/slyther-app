package ir.momeni.slyther.auth.dto;

import lombok.*;

/**
 * Response payload returned after successful authentication or token refresh.
 * <p>
 * Contains:
 * - tokenType: usually "Bearer" for Authorization header usage
 * - accessToken: short-lived JWT used for API access
 * - refreshToken: long-lived token used to obtain new access tokens
 * - expiresInSeconds: lifetime of the access token in seconds (client countdown)
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TokenResponse {

    /** Authorization scheme, commonly "Bearer". */
    private String tokenType;

    /** Short-lived JWT used to authenticate API requests. */
    private String accessToken;

    /** Refresh token linked to a session to generate new access tokens. */
    private String refreshToken;

    /** Access token expiration time (in seconds) so clients can refresh before expiry. */
    private long expiresInSeconds;
}
