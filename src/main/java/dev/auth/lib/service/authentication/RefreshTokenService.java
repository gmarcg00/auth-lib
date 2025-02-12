package dev.auth.lib.service.authentication;

import dev.auth.lib.data.model.RefreshToken;
import dev.auth.lib.data.model.User;

import java.util.Optional;

public interface RefreshTokenService {
    Optional<RefreshToken> findByToken(String token);
    RefreshToken createRefreshToken(User user);
    boolean isRefreshTokenValid(RefreshToken token);
    boolean existsRefreshToken(User user);
    void deleteByUser(User user);
}
