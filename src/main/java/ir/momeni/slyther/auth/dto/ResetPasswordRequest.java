package ir.momeni.slyther.auth.dto;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter; @Getter
public class ResetPasswordRequest { @NotBlank private String token; @NotBlank private String newPassword; }
