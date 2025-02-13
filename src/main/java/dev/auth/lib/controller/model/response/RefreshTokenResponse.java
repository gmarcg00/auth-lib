package dev.auth.lib.controller.model.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.Date;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class RefreshTokenResponse {

    private String token;

    @JsonProperty(value = "expiration_date")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date expirationDate;

    @JsonProperty("token_type")
    private final String tokenType = "Bearer";

    @JsonProperty("refresh_token")
    private String refreshToken;
}
