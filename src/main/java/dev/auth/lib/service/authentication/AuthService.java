package dev.auth.lib.service.authentication;

import dev.auth.lib.data.model.ExchangeSessionCode;
import dev.auth.lib.data.model.User;
import dev.auth.lib.service.authentication.impl.AuthServiceImpl;

import java.util.Optional;


public interface AuthService {
    void signUp(User user);
    AuthServiceImpl.Tokens login(String email, String password,String requestUri);
    Optional<ExchangeSessionCode> externalAccess(String email);
    void logout(User user);
    AuthServiceImpl.Tokens refreshToken(String token);
    void activateUser(String email, String verificationCode, String password);
    void changePassword(String email, String userPassword, String oldPassword);
    void recoveryPassword(String email);
    void recoveryPasswordActivate(String email, String verificationCode, String password);
    AuthServiceImpl.Tokens externalLogin(String exchangeCode);
}
