package dev.auth.lib.service.authentication;

import dev.auth.lib.data.model.User;


public interface AuthService {
    void signUp(User user);
}
