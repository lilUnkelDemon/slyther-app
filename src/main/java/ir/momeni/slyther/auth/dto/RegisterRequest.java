package ir.momeni.slyther.auth.dto;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter; @Getter
public class RegisterRequest { @NotBlank private String username;@NotBlank private String email; @NotBlank private String password; }
