package dev.auth.lib.service.authentication.impl;

import dev.auth.lib.authentication.model.AuthenticationDTO;
import dev.auth.lib.authentication.model.AuthenticationRequestDetails;
import dev.auth.lib.data.model.AccessToken;
import dev.auth.lib.data.model.RefreshToken;
import dev.auth.lib.data.model.User;
import dev.auth.lib.data.model.UserStatusEnum;
import dev.auth.lib.exception.InvalidCredentialsException;
import dev.auth.lib.exception.RefreshTokenExpiredException;
import dev.auth.lib.exception.RefreshTokenNotFoundException;
import dev.auth.lib.exception.UserNotFoundException;
import dev.auth.lib.service.authentication.AuthService;
import dev.auth.lib.service.authentication.JwtService;
import dev.auth.lib.service.authentication.RefreshTokenService;
import dev.auth.lib.service.users.UserService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static java.util.Objects.isNull;


@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final String INVALID_CREDENTIALS_ERROR = "Credentials provided by the user are not valid.";
    private static final String USER_NOT_FOUND_ERROR = "User not found.";

    private final UserService userService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    @Override
    public void signUp(User user) {
       userService.createUser(user);
    }

    @Override
    public Tokens login(String email, String password, String requestUri) {
        AuthenticationDTO authDTO = AuthenticationDTO.builder()
                .user(new User(email, password))
                .details(new AuthenticationRequestDetails(requestUri))
                .authenticated(false)
                .build();
        Authentication authentication = authenticationManager.authenticate(authDTO);
        if(!authentication.isAuthenticated()){
            log.info("Las credenciales suministradas por el usuario no son válidas.");
            throw new InvalidCredentialsException(INVALID_CREDENTIALS_ERROR);
        }
        User user = (User) authentication.getPrincipal();
        return generateTokens(user);
    }

    @Transactional
    @Override
    public void logout(User user) {
        if(isNull(user)) throw new UserNotFoundException(USER_NOT_FOUND_ERROR);
        refreshTokenService.deleteByUser(user);
    }

    @Transactional
    @Override
    public Tokens refreshToken(String token) {
        Optional<RefreshToken> optionalRefreshToken = refreshTokenService.findByToken(token);
        RefreshToken refreshToken = optionalRefreshToken
                .orElseThrow(() -> {
                    log.warn("El token de refresco ya no existe.");
                    return new RefreshTokenNotFoundException("Refresh token not found.");
                });
        if(!refreshTokenService.isRefreshTokenValid(refreshToken)) {
            log.info("El token de refresco ha expirado.");
            throw new RefreshTokenExpiredException("Refresh token has expired.");
        }
        User user = refreshToken.getUser();
        return this.generateTokens(user);
    }

    private AuthServiceImpl.Tokens generateTokens(User user) {
        checkIfUserIsActive(user);
        AccessToken accessToken = jwtService.generateAccessToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);
        return new AuthServiceImpl.Tokens(accessToken, refreshToken);
    }


    private static void checkIfUserIsActive(User user) {
        if (!UserStatusEnum.ACTIVE.getStatusCode().equals(user.getStatus().getName())) {
            log.warn("Intento de acceso con usuario no activo: {}", user.getId());
            throw new InvalidCredentialsException(INVALID_CREDENTIALS_ERROR);
        }
    }

    @AllArgsConstructor
    @Data
    public static class Tokens {
        private AccessToken accessToken;
        private RefreshToken refreshToken;

    }
}
