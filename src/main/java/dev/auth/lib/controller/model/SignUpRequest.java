package dev.auth.lib.controller.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class SignUpRequest implements Serializable {
    @NotBlank
    @Email
    String email;

    @NotBlank
    String password;
}
