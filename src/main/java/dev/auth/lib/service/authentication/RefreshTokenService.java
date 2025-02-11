package dev.auth.lib.service.authentication;

import dev.auth.lib.data.model.RefreshToken;
import dev.auth.lib.data.model.User;

public interface RefreshTokenService {
    RefreshToken createRefreshToken(User user);
}
