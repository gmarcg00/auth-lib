package dev.auth.lib.service.auth;

import dev.auth.lib.data.model.*;
import dev.auth.lib.exception.*;
import dev.auth.lib.service.authentication.JwtService;
import dev.auth.lib.service.authentication.RefreshTokenService;
import dev.auth.lib.service.authentication.impl.AuthServiceImpl;
import dev.auth.lib.service.email.EmailService;
import dev.auth.lib.service.email.types.EmailFormatterFactory;
import dev.auth.lib.service.email.types.UserRecoveryPasswordEmailFormatter;
import dev.auth.lib.service.email.types.UserRegistrationEmailFormatter;
import dev.auth.lib.service.users.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    private static final String USER_STATUS_ACTIVE = "ACTIVE";
    private static final String USER_ROLE = "TEST_ROLE";
    private static final String USER_TEST = "User test";
    private static final String USER_PASSWORD = "User password";
    private static final String OLD_PASSWORD = "oldPassword";
    private static final String USER_MAIL = "test@mail.com";
    private static final String VERIFICATION_CODE = "abcdefgh";
    private static final String REQUEST_URI = "http://www.test.com";
    private static final String REFRESH_TOKEN = "abcdefg";

    @InjectMocks
    private AuthServiceImpl authService;

    @Mock
    private UserService userService;

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private EmailService emailService;

    private AutoCloseable closeable;

    private User inputUser;

    @BeforeEach
    public void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        UserStatus userStatus = UserStatus.builder()
                .name(USER_STATUS_ACTIVE)
                .build();
        inputUser = User.builder()
                .password(USER_PASSWORD)
                .email(USER_MAIL)
                .status(userStatus)
                .build();
    }

    @AfterEach
    public void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void testSignUpSuccessful() throws Exception {
        // Given
        AutoCloseable ac = mockStatic(EmailFormatterFactory.class);
        Role role = new Role();
        role.setName(USER_ROLE);
        User databaseUser = User.builder()
                .password(USER_PASSWORD)
                .email(USER_MAIL)
                .roles(Set.of(role))
                .verificationCode(VERIFICATION_CODE)
                .build();
        when(userService.createUser(inputUser)).thenReturn(databaseUser);
        UserRegistrationEmailFormatter emailFormatter = mock(UserRegistrationEmailFormatter.class);
        when(EmailFormatterFactory.createUserRegistrationEmailFormatter(databaseUser, null)).thenReturn(emailFormatter);

        // When
        authService.signUp(inputUser);

        // Then
        verify(emailService, times(1)).sendEmail(USER_MAIL, emailFormatter);

        ac.close();
    }

    @Test
    void testLoginBadCredentials(){
        // Given
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);

        // When y Given
        InvalidCredentialsException exception = assertThrows(InvalidCredentialsException.class, () -> authService.login(USER_TEST, USER_PASSWORD, REQUEST_URI));
        assertEquals("Credentials provided by the user are not valid.", exception.getMessage());
    }

    @Test
    void testLoginUserNotActive(){
        // Given
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        UserStatus userStatus = UserStatus.builder()
                .name("VERIFICATION_PENDING")
                .build();
        inputUser.setStatus(userStatus);
        when(authentication.getPrincipal()).thenReturn(inputUser);

        // When
        InvalidCredentialsException exception = assertThrows(InvalidCredentialsException.class, () -> authService.login(USER_TEST, USER_PASSWORD, REQUEST_URI));
        assertEquals("Credentials provided by the user are not valid.", exception.getMessage());
    }

    @Test
    void testLoginSuccessful() throws Exception {
        // Given
        AutoCloseable ac = mockStatic(Instant.class);
        when(Instant.now()).thenReturn(Instant.MAX);
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(inputUser);
        AuthServiceImpl.Tokens mockTokens = mockTokenGeneration(inputUser);

        // When y Given
        AuthServiceImpl.Tokens tokens = authService.login(USER_TEST, USER_PASSWORD, REQUEST_URI);
        assertEquals(tokens, mockTokens);

        ac.close();
    }

    private AuthServiceImpl.Tokens mockTokenGeneration(User user) {
        AccessToken accessToken = new AccessToken();
        when(jwtService.generateAccessToken(user)).thenReturn(accessToken);
        RefreshToken refreshToken = new RefreshToken();
        when(refreshTokenService.createRefreshToken(user)).thenReturn(refreshToken);
        return new AuthServiceImpl.Tokens(accessToken, refreshToken);
    }

    @Test
    void testLogoutUserNotFound() {
        // When y Then
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> authService.logout(null));
        assertEquals("User not found.", exception.getMessage());
    }

    @Test
    void testLogoutSuccessful() {
        // When
        authService.logout(inputUser);
        // Then
        verify(refreshTokenService).deleteByUser(inputUser);
    }

    @Test
    void testRefreshTokenNotFound() {
        // Given
        when(refreshTokenService.findByToken(REFRESH_TOKEN)).thenReturn(Optional.empty());

        // When y Then
        RefreshTokenNotFoundException exception = assertThrows(RefreshTokenNotFoundException.class, () -> authService.refreshToken(REFRESH_TOKEN));
        assertEquals("Refresh token not found.", exception.getMessage());
    }

    @Test
    void testRefreshTokenInvalid() {
        // Given
        RefreshToken databaseRefreshToken = new RefreshToken();
        when(refreshTokenService.findByToken(REFRESH_TOKEN)).thenReturn(Optional.of(databaseRefreshToken));
        when(refreshTokenService.isRefreshTokenValid(databaseRefreshToken)).thenReturn(false);

        // When y Then
        RefreshTokenExpiredException exception = assertThrows(RefreshTokenExpiredException.class, () -> authService.refreshToken(REFRESH_TOKEN));
        assertEquals("Refresh token has expired.", exception.getMessage());
    }

    @Test
    void testRefreshTokenSuccessful() {
        // Given
        RefreshToken databaseRefreshToken = new RefreshToken();
        databaseRefreshToken.setUser(inputUser);
        when(refreshTokenService.findByToken(REFRESH_TOKEN)).thenReturn(Optional.of(databaseRefreshToken));
        when(refreshTokenService.isRefreshTokenValid(databaseRefreshToken)).thenReturn(true);
        AuthServiceImpl.Tokens mockTokens = mockTokenGeneration(inputUser);

        // When
        AuthServiceImpl.Tokens tokens = authService.refreshToken(REFRESH_TOKEN);

        // Then
        assertEquals(tokens, mockTokens);
    }

    @Test
    void testActivateUserSuccessful() {
        // When
        authService.activateUser(USER_MAIL, VERIFICATION_CODE, USER_PASSWORD);

        // Then
        verify(userService, times(1)).activateUser(USER_MAIL, VERIFICATION_CODE, USER_PASSWORD);
    }

    @Test
    void testChangePasswordInvalidOldPassword() {
        // Given
        doThrow(InvalidPasswordException.class).when(userService).changePassword(USER_MAIL, USER_PASSWORD, OLD_PASSWORD);

        // When y Then
        InvalidCredentialsException exception = assertThrows(InvalidCredentialsException.class, () -> authService.changePassword(USER_MAIL, USER_PASSWORD, OLD_PASSWORD));
        assertEquals("Credentials provided by the user are not valid.", exception.getMessage());
    }

    @Test
    void testChangePasswordSuccessful() {
        // When
        authService.changePassword(USER_MAIL, USER_PASSWORD, OLD_PASSWORD);

        // Then
        verify(userService, times(1)).changePassword(USER_MAIL, USER_PASSWORD, OLD_PASSWORD);
    }

    @Test
    void testRecoveryPasswordSuccessful() throws Exception {
        // Given
        AutoCloseable ac = mockStatic(EmailFormatterFactory.class);
        User serviceUser = User.builder()
                .email(USER_MAIL)
                .verificationCode(VERIFICATION_CODE)
                .build();
        when(userService.enableResetPassword(USER_MAIL)).thenReturn(serviceUser);
        UserRecoveryPasswordEmailFormatter emailFormatter = mock(UserRecoveryPasswordEmailFormatter.class);
        when(EmailFormatterFactory.createUserRecoveryPasswordEmailFormatter(serviceUser, null)).thenReturn(emailFormatter);

        // When
        authService.recoveryPassword(USER_MAIL);

        // Then
        verify(userService, times(1)).enableResetPassword(USER_MAIL);
        verify(emailService, times(1)).sendEmail(USER_MAIL, emailFormatter);

        ac.close();
    }

    @Test
    void testRecoveryPasswordActivateUserNotFound(){
        // Given
        doThrow(UserNotFoundException.class).when(userService).recoveryPasswordActivate(USER_MAIL, VERIFICATION_CODE, USER_PASSWORD);

        // When y Then
        InvalidCredentialsException exception = assertThrows(InvalidCredentialsException.class, () -> authService.recoveryPasswordActivate(USER_MAIL, VERIFICATION_CODE, USER_PASSWORD));
        assertEquals("Credentials provided by the user are not valid.", exception.getMessage());
    }

    @Test
    void testRecoveryPasswordActivateInvalidCode(){
        // Given
        doThrow(InvalidVerificationCodeException.class).when(userService).recoveryPasswordActivate(USER_MAIL, VERIFICATION_CODE, USER_PASSWORD);

        // When y Then
        InvalidCredentialsException exception = assertThrows(InvalidCredentialsException.class, () -> authService.recoveryPasswordActivate(USER_MAIL, VERIFICATION_CODE, USER_PASSWORD));
        assertEquals("Credentials provided by the user are not valid.", exception.getMessage());
    }

    @Test
    void testRecoveryPasswordActivateSuccessful(){
        // When
        authService.recoveryPasswordActivate(USER_MAIL, VERIFICATION_CODE, USER_PASSWORD);

        // Then
        verify(userService, times(1)).recoveryPasswordActivate(USER_MAIL, VERIFICATION_CODE, USER_PASSWORD);
    }
}
