package dev.auth.lib.controller.mappers;

import dev.auth.lib.controller.model.SignUpRequest;
import dev.auth.lib.controller.model.response.LoginResponse;
import dev.auth.lib.controller.model.response.RefreshTokenResponse;
import dev.auth.lib.data.model.AccessToken;
import dev.auth.lib.data.model.RefreshToken;
import dev.auth.lib.data.model.User;
import dev.auth.lib.service.authentication.impl.AuthServiceImpl;

public class UsersMapper {

    private UsersMapper(){}

    public static User requestToEntity(SignUpRequest request) {
        return User.builder()
                .email(request.getEmail())
                .password(request.getPassword())
                .build();
    }

    public static LoginResponse toLoginResponse(AuthServiceImpl.Tokens tokens) {
        AccessToken accessToken = tokens.getAccessToken();
        RefreshToken refreshToken = tokens.getRefreshToken();
        return LoginResponse.builder()
                .token(accessToken.getToken())
                .expirationDate(String.valueOf(accessToken.getExpirationDate()))
                .refreshToken(refreshToken.getToken())
                .build();
    }

    public static RefreshTokenResponse toRefreshTokenResponse(AuthServiceImpl.Tokens tokens) {
        AccessToken accessToken = tokens.getAccessToken();
        RefreshToken refreshToken = tokens.getRefreshToken();
        return RefreshTokenResponse.builder()
                .token(accessToken.getToken())
                .expirationDate(String.valueOf(accessToken.getExpirationDate()))
                .refreshToken(refreshToken.getToken())
                .build();
    }
}
