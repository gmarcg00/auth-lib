package dev.auth.lib.service.users.impl;

import dev.auth.lib.data.model.Role;
import dev.auth.lib.data.model.User;
import dev.auth.lib.data.model.UserStatus;
import dev.auth.lib.data.model.UserStatusEnum;
import dev.auth.lib.data.repository.RoleRepository;
import dev.auth.lib.data.repository.UserRepository;
import dev.auth.lib.data.repository.UserStatusRepository;
import dev.auth.lib.exception.*;
import dev.auth.lib.service.users.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;


@AllArgsConstructor
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserStatusRepository userStatusRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User createUser(User user, boolean external) {
        try {
            return createUser(user, List.of(DEFAULT_ROLE),external);
        } catch (RoleNotFoundException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private User createUser(User user, List<String> roles, boolean external) throws RoleNotFoundException {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new UserWithSameUsernameException("This username exists in system.");
        }

        user.setCreationDate(Instant.now());
        user.setExternalUser(external);
        addVerificationCode(user);
        addEncodedPassword(user, user.getPassword());
        addRoles(user, roles);
        addDefaultUserStatus(user);

        userRepository.save(user);
        return user;
    }

    @Override
    public Optional<User> findByEmail(String username) {
        return userRepository.findByEmail(username);
    }

    @Override
    public void activateUser(String email, String verificationCode, String password) {
        User user = getUser(email);
        checkStatusIsVerificationPending(user);
        checkVerificationCodeIsValid(user, verificationCode);
        checkUserWithoutPasswordCanBeAdded(user, password);
        checkUserWithPasswordAlreadyAdded(user, password);

        addStatus(user, UserStatusEnum.ACTIVE);
        user.setVerificationCode(null);
        addEncodedPassword(user, password);
        userRepository.save(user);
    }

    @Override
    public void changePassword(String email, String newPassword, String oldPassword) {
        User user = getUser(email);
        checkUserCanChangePassword(user);
        checkOldPassword(user, oldPassword);
        addEncodedPassword(user, newPassword);
        userRepository.save(user);
    }

    private void checkUserCanChangePassword(User user) {
        if (Boolean.TRUE.equals(user.getExternalUser())) {
            log.warn("El usuario {} no está disponible para iniciar el proceso de cambio de contraseña", user.getId());
            throw new InvalidUserTypeException("User is not available for password change.");
        }
    }

    private void checkOldPassword(User user, String oldPassword) {
        String databaseOldPassword = user.getPassword();
        if (!passwordEncoder.matches(oldPassword, databaseOldPassword)) {
            log.warn("El usuario {} no ha suministrado correctamente su antigua contraseña.", user.getId());
            throw new InvalidPasswordException("Invalid old password.");
        }
    }

    private void addEncodedPassword(User user, String password) {
        if (password != null) {
            user.setPassword(passwordEncoder.encode(password));
            user.setLastPasswordChange(Instant.now());
        }
    }

    @Override
    public User enableResetPassword(String email) {
        User user = getUser(email);
        checkUserCanRecoveryPassword(user);
        checkUserIsInactive(user);
        addVerificationCode(user);
        userRepository.save(user);
        return user;
    }


    @Override
    public void recoveryPasswordActivate(String email, String verificationCode, String password) {
        User user = getUser(email);
        checkUserCanRecoveryPassword(user);
        checkStatusIsActive(user);
        checkVerificationCodeIsValid(user, verificationCode);

        user.setVerificationCode(null);
        addEncodedPassword(user, password);
        userRepository.save(user);
    }

    private User getUser(String email) {
        Optional<User> oUser = userRepository.findByEmail(email);
        return oUser.orElseThrow(() -> {
            log.info("El usuario no existe en el sistema.");
            return new UserNotFoundException("User not found.");
        });
    }

    private void checkUserCanRecoveryPassword(User user) {
        if (Boolean.TRUE.equals(user.getExternalUser())) {
            log.warn("El usuario {} no está disponible para realizar el proceso de recuperación de contraseña", user.getId());
            throw new InvalidUserTypeException("User is not available for password recovery.");
        }
    }

    private void checkStatusIsActive(User user) {
        if (!UserStatusEnum.ACTIVE.getStatusCode().equals(user.getStatus().getName())) {
            log.warn("El usuario {} no está activo.", user.getId());
            throw new UserNotActiveException("User not available to password recovery.");
        }
    }

    private void checkUserIsInactive(User user) {
        UserStatusEnum status = UserStatusEnum.findByStatusCode(user.getStatus().getName());
        if (status == UserStatusEnum.INACTIVE) {
            log.warn("El usuario {} ha intentado restaurar una cuenta inactiva.", user.getId());
            throw new ForbiddenResetPasswordException("This user can not reset password.");
        }
    }

    private void checkStatusIsVerificationPending(User user) {
        if (!UserStatusEnum.VERIFICATION_PENDING.getStatusCode().equals(user.getStatus().getName())) {
            log.warn("El usuario {} no está pendiente de verificación.", user.getId());
            throw new UserAlreadyValidatedException("User already validated.");
        }
    }

    private void checkVerificationCodeIsValid(User user, String verificationCode) {
        String databaseVerificationCode = user.getVerificationCode();
        if(isNull(databaseVerificationCode)) {
            log.warn("El usuario {} no tiene un código de verificación.", user.getId());
            throw new InvalidVerificationCodeException("Verification code not found.");
        }
        if (!Objects.equals(databaseVerificationCode, verificationCode)) {
            log.warn("El usuario {} no ha suministrado correctamente el código de verificación.", user.getId());
            throw new InvalidVerificationCodeException("Invalid verification code.");
        }
    }

    private void checkUserWithoutPasswordCanBeAdded(User user, String password) {
        if (Boolean.TRUE.equals(!user.getExternalUser() && isNull(user.getPassword())) && isNull(password)) {
            log.warn("El usuario {} ha intentado activar una cuenta sin contraseña.", user.getId());
            throw new MandatoryPasswordException("Password is mandatory.");
        }
    }

    private void checkUserWithPasswordAlreadyAdded(User user, String password) {
        if (nonNull(user.getPassword()) && nonNull(password)) {
            log.warn("El usuario {} ha intentado activar una cuenta con contraseña.", user.getId());
            throw new PasswordAlreadyAddedException("User has already added a password.");
        }
    }

    private void addVerificationCode(User user) {
        user.setVerificationCode(UUID.randomUUID().toString());
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
