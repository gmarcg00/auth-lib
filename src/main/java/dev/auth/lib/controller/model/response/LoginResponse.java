package dev.auth.lib.controller.model.response;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serializable;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class LoginResponse implements Serializable {

    private String token;

    @JsonProperty(value = "expiration_date")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private String expirationDate;

    @JsonProperty("token_type")
    private final String tokenType = "Bearer";

    @JsonProperty("refresh_token")
    private String refreshToken;
}
