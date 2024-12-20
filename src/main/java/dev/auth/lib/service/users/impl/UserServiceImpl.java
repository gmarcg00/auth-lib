package dev.auth.lib.service.users.impl;

import dev.auth.lib.data.model.Role;
import dev.auth.lib.data.model.User;
import dev.auth.lib.data.model.UserStatus;
import dev.auth.lib.data.model.UserStatusEnum;
import dev.auth.lib.data.repository.RoleRepository;
import dev.auth.lib.data.repository.UserRepository;
import dev.auth.lib.data.repository.UserStatusRepository;
import dev.auth.lib.exception.RoleNotFoundException;
import dev.auth.lib.exception.UserWithSameUsernameException;
import dev.auth.lib.service.users.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserStatusRepository userStatusRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User createUser(User user) {
        try {
            return createUser(user, List.of(DEFAULT_ROLE));
        } catch (RoleNotFoundException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    public User createUser(User user, List<String> roles) throws RoleNotFoundException {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new UserWithSameUsernameException("This username exists in system.");
        }

        user.setCreationDate(Instant.now());
        user.setExternalUser(false);
        addVerificationCode(user);
        addEncodedPassword(user, user.getPassword());
        addRoles(user, roles);
        addDefaultUserStatus(user);

        userRepository.save(user);
        return user;
    }

    private void addVerificationCode(User user) {
        user.setVerificationCode(UUID.randomUUID().toString());
    }

    private void addEncodedPassword(User user, String password) {
        if (password != null) {
            user.setPassword(passwordEncoder.encode(password));
            user.setLastPasswordChange(Instant.now());
        }
    }

    private void addRoles(User user, List<String> roles) throws RoleNotFoundException {
        Set<Role> rolesSet = (user.getRoles() == null) ? new HashSet<>() : user.getRoles();
        for (String role : roles) {
            Optional<Role> odRole = roleRepository.findByName(role);
            Role dRole = odRole.orElseThrow(() -> {
                log.warn("El rol {} no se encuentra en el sistema.", role);
                return new RoleNotFoundException("Role " + role + " not found.");
            });
            rolesSet.add(dRole);
        }
        user.setRoles(rolesSet);
    }

    private void addDefaultUserStatus(User user) {
        addStatus(user, DEFAULT_STATUS);
    }

    private void addStatus(User user, UserStatusEnum targetStatus) {
        UserStatus newStatus = userStatusRepository.findByName(targetStatus.getStatusCode());
        user.setStatus(newStatus);
    }
}
