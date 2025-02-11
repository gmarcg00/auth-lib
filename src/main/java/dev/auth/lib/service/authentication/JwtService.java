package dev.auth.lib.service.authentication;

import dev.auth.lib.data.model.AccessToken;
import dev.auth.lib.data.model.User;

public interface JwtService {
    AccessToken generateAccessToken(User user);
    String extractUsername(String jwt);
    Long getValidationTokenTime();
}
