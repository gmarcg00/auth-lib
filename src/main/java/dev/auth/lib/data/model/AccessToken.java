package dev.auth.lib.data.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class AccessToken {
    private String token;

    private Date expirationDate;

    private Long expiresIn;

    private User user;
}
