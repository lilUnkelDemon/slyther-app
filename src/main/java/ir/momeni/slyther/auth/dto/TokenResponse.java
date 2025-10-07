package ir.momeni.slyther.auth.dto;
import lombok.*;
@Getter @Setter @Builder @AllArgsConstructor @NoArgsConstructor
public class TokenResponse {
    private String tokenType; private String accessToken; private String refreshToken; private long expiresInSeconds;

}