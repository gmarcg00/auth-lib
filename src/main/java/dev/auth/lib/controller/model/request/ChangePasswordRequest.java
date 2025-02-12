package dev.auth.lib.controller.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ChangePasswordRequest {
    @NotBlank
    private String password;

    @JsonProperty("old_password")
    @NotBlank
    private String oldPassword;
}
