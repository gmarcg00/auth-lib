package dev.auth.lib.service.users;

import dev.auth.lib.data.model.User;
import dev.auth.lib.data.model.UserStatusEnum;
import dev.auth.lib.service.authentication.impl.AuthServiceImpl;

import java.util.Optional;

public interface UserService {
    String DEFAULT_ROLE = "USER";
    UserStatusEnum DEFAULT_STATUS = UserStatusEnum.VERIFICATION_PENDING;

    User createUser(User user, boolean external);
    Optional<User> findByEmail(String username);
    void activateUser(String email, String verificationCode, String password);
    void changePassword(String email, String newPassword, String oldPassword);
    User enableResetPassword(String email);
    void recoveryPasswordActivate(String email, String verificationCode, String password);
}
