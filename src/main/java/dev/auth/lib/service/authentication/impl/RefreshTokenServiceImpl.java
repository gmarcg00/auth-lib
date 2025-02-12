package dev.auth.lib.service.authentication.impl;

import dev.auth.lib.data.model.RefreshToken;
import dev.auth.lib.data.model.User;
import dev.auth.lib.data.repository.RefreshTokenRepository;
import dev.auth.lib.service.authentication.RefreshTokenService;
import dev.auth.lib.utils.DateUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    @Value("${security.jwt.expiration-refresh-token}")
    private String  expirationRefreshTokenTime;

    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    @Override
    public RefreshToken createRefreshToken(User user) {
        Optional<RefreshToken> oRefreshToken = refreshTokenRepository.findByUserId(user.getId());
        RefreshToken refreshToken = oRefreshToken
                .map(this::updateRefreshToken)
                .orElseGet(() -> createNewToken(user));
        refreshTokenRepository.save(refreshToken);
        return refreshToken;
    }

    @Override
    public boolean isRefreshTokenValid(RefreshToken token) {
        if (token.getExpirationDate().compareTo(new Date(System.currentTimeMillis())) < 0) {
            refreshTokenRepository.delete(token);
            return false;
        }
        return true;
    }

    @Override
    public void deleteByUser(User user) {
        refreshTokenRepository.deleteByUser(user);
    }

    private RefreshToken updateRefreshToken(RefreshToken refreshToken) {
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpirationDate(DateUtils.currentTimePlusSeconds(Long.parseLong(expirationRefreshTokenTime)));
        return refreshToken;
    }

    private RefreshToken createNewToken(User user) {
        return RefreshToken.builder()
                .user(user)
                .expirationDate(DateUtils.currentTimePlusSeconds(Long.parseLong(expirationRefreshTokenTime)))
                .token(UUID.randomUUID().toString())
                .build();
    }
}
