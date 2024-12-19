package dev.auth.lib.service.authentication.impl;

import dev.auth.lib.data.model.AccessToken;
import dev.auth.lib.data.model.RefreshToken;
import dev.auth.lib.data.model.User;
import dev.auth.lib.service.authentication.AuthService;
import dev.auth.lib.service.authentication.JwtService;
import dev.auth.lib.service.authentication.RefreshTokenService;
import dev.auth.lib.service.users.UserService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.stereotype.Service;


@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;


    @Override
    public void signUp(User user, String referrerCode) {

    }



    @AllArgsConstructor
    @Data
    public static class Tokens {
        private AccessToken accessToken;
        private RefreshToken refreshToken;

    }
}
