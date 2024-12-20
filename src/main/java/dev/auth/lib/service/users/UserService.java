package dev.auth.lib.service.users;

import dev.auth.lib.data.model.User;
import dev.auth.lib.data.model.UserStatusEnum;
import dev.auth.lib.exception.RoleNotFoundException;

import java.util.List;

public interface UserService {
    String DEFAULT_ROLE = "USER";
    UserStatusEnum DEFAULT_STATUS = UserStatusEnum.VERIFICATION_PENDING;

    User createUser(User user);
    User createUser(User user, List<String> roles) throws RoleNotFoundException;
}
