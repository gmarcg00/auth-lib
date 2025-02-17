package dev.auth.lib.service.users;

import dev.auth.lib.data.model.Role;
import dev.auth.lib.data.model.User;
import dev.auth.lib.data.model.UserStatus;
import dev.auth.lib.data.model.UserStatusEnum;
import dev.auth.lib.data.repository.RoleRepository;
import dev.auth.lib.data.repository.UserRepository;
import dev.auth.lib.data.repository.UserStatusRepository;
import dev.auth.lib.exception.*;
import dev.auth.lib.service.users.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.util.Pair;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    private static final String EMAIL = "username@email.com";
    private static final String PASSWORD = "password";
    public static final String OLD_PASSWORD = "oldPassword";
    private static final String ENCODED_PASSWORD = "encoded_password";
    private static final String DEFAULT_ROLE_NAME = "USER";
    private static final String VERIFICATION_PENDING_STATUS_NAME = "VERIFICATION_PENDING";
    private static final String VERIFICATION_CODE = "abcde-1234";
    private static final String ACTIVE_STATUS_NAME = "ACTIVE";

    private UserServiceImpl userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserStatusRepository userStatusRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    public void setUp() {
        userService = new UserServiceImpl(userRepository, roleRepository, userStatusRepository, passwordEncoder);
    }

    @Test
    void testCreateUserWithExistingUser() {
        // Given
        User user = User.builder()
                .email(EMAIL)
                .build();
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

        // When
        UserWithSameUsernameException exception = assertThrows(UserWithSameUsernameException.class, () -> userService.createUser(user,false));

        // Then
        assertEquals("This username exists in system.", exception.getMessage());
    }

    @Test
    void testCreateUserSuccessful() throws Exception {
        // Given
        Pair<User, AutoCloseable> result = mockUserCreation(true ,List.of(DEFAULT_ROLE_NAME));
        User user = result.getFirst();

        // When
        User savedUser = userService.createUser(user,false);

        // Then
        verify(userRepository, times(1)).save(any(User.class));
        assertEquals(EMAIL, savedUser.getUsername());
        assertEquals(ENCODED_PASSWORD, savedUser.getPassword());
        assertEquals(Instant.MAX, savedUser.getCreationDate());
        assertFalse(savedUser.getExternalUser());
        Collection<String> roles = savedUser.getFlattenRoles();
        assertEquals(1, roles.size());
        assertTrue(roles.contains(DEFAULT_ROLE_NAME));
        assertEquals(VERIFICATION_PENDING_STATUS_NAME, savedUser.getStatus().getName());

        result.getSecond().close();
    }

    private Pair<User, AutoCloseable> mockUserCreation(boolean withPassword, List<String> roles) {
        AutoCloseable closeable = mockStatic(Instant.class);
        String password = (withPassword) ? PASSWORD : null;
        User user = User.builder()
                .email(EMAIL)
                .password(password)
                .build();
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        if (withPassword)
            when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(Instant.now()).thenReturn(Instant.MAX);
        for(String role : roles) {
            Role roleUser = new Role();
            roleUser.setName(role);
            when(roleRepository.findByName(role)).thenReturn(Optional.of(roleUser));
        }
        UserStatus userStatus = UserStatus.builder()
                .name(VERIFICATION_PENDING_STATUS_NAME)
                .build();
        when(userStatusRepository.findByName(VERIFICATION_PENDING_STATUS_NAME)).thenReturn(userStatus);

        return Pair.of(user, closeable);
    }

    @Test
    void testActivateUserUserNotFound() {
        // Given
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        // When y then
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> userService.activateUser(EMAIL, VERIFICATION_CODE, null));
        assertEquals("User not found.", exception.getMessage());
    }

    @Test
    void testActivateUserUserAlreadyValidated() {
        // Given
        UserStatus status = UserStatus.builder()
                .name(ACTIVE_STATUS_NAME)
                .build();
        User user = User.builder()
                .status(status)
                .build();
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

        // When y then
        UserAlreadyValidatedException exception = assertThrows(UserAlreadyValidatedException.class, () -> userService.activateUser(EMAIL, VERIFICATION_CODE, null));
        assertEquals("User already validated.", exception.getMessage());
    }

    @Test
    void testActiveUserInvalidVerificationCode() {
        // Given
        UserStatus status = UserStatus.builder()
                .name(VERIFICATION_PENDING_STATUS_NAME)
                .build();
        User user = User.builder()
                .status(status)
                .verificationCode(VERIFICATION_CODE)
                .build();
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

        // When y then
        InvalidVerificationCodeException exception = assertThrows(InvalidVerificationCodeException.class, () -> userService.activateUser(EMAIL, "VERIFICATION_CODE", null));
        assertEquals("Invalid verification code.", exception.getMessage());
    }

    @Test
    void testActiveUserNotPasswordInUserAndInput() {
        // Given
        UserStatus status = UserStatus.builder()
                .name(VERIFICATION_PENDING_STATUS_NAME)
                .build();
        User user = User.builder()
                .status(status)
                .verificationCode(VERIFICATION_CODE)
                .externalUser(Boolean.FALSE)
                .build();
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

        // When y then
        MandatoryPasswordException exception = assertThrows(MandatoryPasswordException.class, () -> userService.activateUser(EMAIL, VERIFICATION_CODE, null));
        assertEquals("Password is mandatory.", exception.getMessage());
    }

    @Test
    void testActiveUserPasswordInUserAndInput() {
        // Given
        UserStatus status = UserStatus.builder()
                .name(VERIFICATION_PENDING_STATUS_NAME)
                .build();
        User user = User.builder()
                .status(status)
                .password(PASSWORD)
                .verificationCode(VERIFICATION_CODE)
                .externalUser(Boolean.FALSE)
                .build();
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

        // When y then
        PasswordAlreadyAddedException exception = assertThrows(PasswordAlreadyAddedException.class, () -> userService.activateUser(EMAIL, VERIFICATION_CODE, PASSWORD));
        assertEquals("User has already added a password.", exception.getMessage());
    }

    @Test
    void testActivateUserWithoutPasswordSuccessful() {
        // Given
        UserStatus status = UserStatus.builder()
                .name(VERIFICATION_PENDING_STATUS_NAME)
                .build();
        User user = User.builder()
                .status(status)
                .verificationCode(VERIFICATION_CODE)
                .password(PASSWORD)
                .externalUser(Boolean.FALSE)
                .build();
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        UserStatus newStatus = UserStatus.builder()
                .name(ACTIVE_STATUS_NAME)
                .build();
        when(userStatusRepository.findByName(ACTIVE_STATUS_NAME)).thenReturn(newStatus);


        // When
        userService.activateUser(EMAIL, VERIFICATION_CODE, null);

        // Then
        verify(userRepository, times(1)).save(user);
        assertEquals(newStatus, user.getStatus());
        assertNull(user.getVerificationCode());
    }

    @Test
    void testActivateUserWithPasswordSuccessful() {
        // Given
        UserStatus status = UserStatus.builder()
                .name(VERIFICATION_PENDING_STATUS_NAME)
                .build();
        User user = User.builder()
                .status(status)
                .verificationCode(VERIFICATION_CODE)
                .externalUser(Boolean.FALSE)
                .build();
        when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        UserStatus newStatus = UserStatus.builder()
                .name(ACTIVE_STATUS_NAME)
                .build();
        when(userStatusRepository.findByName(ACTIVE_STATUS_NAME)).thenReturn(newStatus);

        // When
        userService.activateUser(EMAIL, VERIFICATION_CODE, PASSWORD);

        // Then
        verify(userRepository, times(1)).save(user);
        assertEquals(newStatus, user.getStatus());
        assertNull(user.getVerificationCode());
        assertEquals(ENCODED_PASSWORD, user.getPassword());
        assertNotNull(user.getLastPasswordChange());
    }

    @Test
    void testChangePasswordUserNotFound() {
        // Given
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        // When y then
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> userService.changePassword(EMAIL, PASSWORD, OLD_PASSWORD));
        assertEquals("User not found.", exception.getMessage());
    }

    @Test
    void testChangePasswordBadOldPassword() {
        // Given
        User user = User.builder()
                .email(EMAIL)
                .password("$2a$12$y2LCCFGB/2l8FYX1ktrqHOsaYh6V7okXhMX4/ZG0B0RFwYgniRPJK")
                .build();
        when(passwordEncoder.matches(OLD_PASSWORD, user.getPassword())).thenReturn(false);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

        // When y then
        InvalidPasswordException exception = assertThrows(InvalidPasswordException.class, () -> userService.changePassword(EMAIL, PASSWORD, OLD_PASSWORD));
        assertEquals("Invalid old password.", exception.getMessage());
    }

    @Test
    void testChangePasswordExternalUser(){
        // Given
        User user = User.builder()
                .email(EMAIL)
                .password("$2a$12$y2LCCFGB/2l8FYX1ktrqHOsaYh6V7okXhMX4/ZG0B0RFwYgniRPJK")
                .externalUser(true)
                .build();
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

        //When && Then
        InvalidUserTypeException exception = assertThrows(InvalidUserTypeException.class, () -> userService.changePassword(EMAIL, PASSWORD, OLD_PASSWORD));
        assertEquals("User is not available for password change.", exception.getMessage());
    }

    @Test
    void testChangePasswordSuccessful() throws Exception {
        // Given
        AutoCloseable closeable = Mockito.mockStatic(Instant.class);
        User user = User.builder()
                .email(EMAIL)
                .password("$2a$12$y2LCCFGB/2l8FYX1ktrqHOsaYh6V7okXhMX4/ZG0B0RFwYgniRPJK")
                .build();
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(OLD_PASSWORD, user.getPassword())).thenReturn(true);
        when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(Instant.now()).thenReturn(Instant.MAX);

        // When
        userService.changePassword(EMAIL, PASSWORD, OLD_PASSWORD);

        // Then
        verify(userRepository, times(1)).save(any(User.class));
        closeable.close();
    }

    @Test
    void testResetPasswordUserNotFound() {
        // Given
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        // When
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> userService.enableResetPassword(EMAIL));

        // Then
        assertEquals("User not found.", exception.getMessage());
    }

    @Test
    void testResetPasswordInactiveUser() {
        // Given
        UserStatus status = UserStatus.builder()
                .name(UserStatusEnum.INACTIVE.getStatusCode())
                .build();
        User user = User.builder()
                .email(EMAIL)
                .password(PASSWORD)
                .status(status)
                .build();
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

        // When
        ForbiddenResetPasswordException exception = assertThrows(ForbiddenResetPasswordException.class, () -> userService.enableResetPassword(EMAIL));

        // Then
        assertEquals("This user can not reset password.", exception.getMessage());
    }

    @Test
    void testResetPasswordExternalUser(){
        // Given
        UserStatus status = UserStatus.builder()
                .name(UserStatusEnum.ACTIVE.getStatusCode())
                .build();
        User user = User.builder()
                .email(EMAIL)
                .password(PASSWORD)
                .status(status)
                .externalUser(true)
                .build();
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

        // When && Then
        InvalidUserTypeException exception = assertThrows(InvalidUserTypeException.class, () -> userService.enableResetPassword(EMAIL));
        assertEquals("User is not available for password recovery.", exception.getMessage());
    }

    @Test
    void testResetPasswordSuccessful() {
        // Given
        UserStatus status = UserStatus.builder()
                .name(UserStatusEnum.ACTIVE.getStatusCode())
                .build();
        User user = User.builder()
                .email(EMAIL)
                .password(PASSWORD)
                .lastPasswordChange(Instant.MIN)
                .status(status)
                .build();
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

        // When
        userService.enableResetPassword(EMAIL);

        // Then
        verify(userRepository, times(1)).save(user);
        assertEquals(UserStatusEnum.ACTIVE.getStatusCode(), user.getStatus().getName());
        assertNotNull(user.getVerificationCode());
        assertEquals(PASSWORD,user.getPassword());
    }

    @Test
    void testRecoveryPasswordActivateCodeNull() {
        // Given
        UserStatus status = UserStatus.builder()
                .name(UserStatusEnum.ACTIVE.getStatusCode())
                .build();
        User user = User.builder()
                .email(EMAIL)
                .password(PASSWORD)
                .lastPasswordChange(Instant.MIN)
                .verificationCode(null)
                .status(status)
                .build();
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

        // When && Then
        assertThrows(InvalidVerificationCodeException.class, () -> userService.recoveryPasswordActivate(EMAIL, VERIFICATION_CODE, PASSWORD));
    }

    @Test
    void testRecoveryPasswordActivateStatusNotActive(){
        // Given
        UserStatus status = UserStatus.builder()
                .name(UserStatusEnum.VERIFICATION_PENDING.getStatusCode())
                .build();
        User user = User.builder()
                .email(EMAIL)
                .password(PASSWORD)
                .lastPasswordChange(Instant.MIN)
                .verificationCode(VERIFICATION_CODE)
                .status(status)
                .build();
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

        // When && Then
        assertThrows(UserNotActiveException.class, () -> userService.recoveryPasswordActivate(EMAIL, VERIFICATION_CODE, PASSWORD));
    }

    @Test
    void testRecoveryPasswordActivateSuccessful(){
        // Given
        UserStatus status = UserStatus.builder()
                .name(UserStatusEnum.ACTIVE.getStatusCode())
                .build();
        User user = User.builder()
                .email(EMAIL)
                .password(PASSWORD)
                .lastPasswordChange(Instant.MIN)
                .verificationCode(VERIFICATION_CODE)
                .status(status)
                .build();
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCODED_PASSWORD);

        // When
        userService.recoveryPasswordActivate(EMAIL, VERIFICATION_CODE, PASSWORD);

        // Then
        verify(userRepository, times(1)).save(user);
        assertEquals(UserStatusEnum.ACTIVE.getStatusCode(), user.getStatus().getName());
        assertNull(user.getVerificationCode());
    }
}
