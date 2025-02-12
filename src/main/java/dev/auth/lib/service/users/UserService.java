package dev.auth.lib.service.users;

import dev.auth.lib.data.model.User;
import dev.auth.lib.data.model.UserStatusEnum;

import java.util.Optional;

public interface UserService {
    String DEFAULT_ROLE = "USER";
    UserStatusEnum DEFAULT_STATUS = UserStatusEnum.VERIFICATION_PENDING;

    User createUser(User user);
    Optional<User> findByEmail(String username);
    void activateUser(String email, String verificationCode, String password);
}
