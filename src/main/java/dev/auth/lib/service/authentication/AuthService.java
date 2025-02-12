package dev.auth.lib.service.authentication;

import dev.auth.lib.data.model.User;
import dev.auth.lib.service.authentication.impl.AuthServiceImpl;


public interface AuthService {
    void signUp(User user);
    AuthServiceImpl.Tokens login(String username, String password,String requestUri);
    void logout(User user);
    AuthServiceImpl.Tokens refreshToken(String token);
    void activateUser(String email, String verificationCode, String password);
    void changePassword(String email, String userPassword, String oldPassword);
}
