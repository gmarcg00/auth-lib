package dev.auth.lib.controller.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ActivateUserRequest {
    @Email
    @NotBlank
    private String email;

    @JsonProperty("verification_code")
    @NotBlank
    private String verificationCode;

    private String password;
}
