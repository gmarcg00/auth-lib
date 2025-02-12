package dev.auth.lib.controller;

import dev.auth.lib.controller.mappers.UsersMapper;
import dev.auth.lib.controller.model.SignUpRequest;
import dev.auth.lib.controller.model.request.LoginRequest;
import dev.auth.lib.controller.model.request.RefreshTokenRequest;
import dev.auth.lib.controller.model.response.LoginResponse;
import dev.auth.lib.controller.model.response.RefreshTokenResponse;
import dev.auth.lib.data.model.AccessToken;
import dev.auth.lib.data.model.RefreshToken;
import dev.auth.lib.data.model.User;
import dev.auth.lib.service.authentication.AuthService;
import dev.auth.lib.service.authentication.impl.AuthServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private static final String EMAIL = "email";
    private static final String PASSWORD = "password";
    private static final String USERNAME = "username";
    private static final String URI = "/test/uri";
    private static final String ACCESS_TOKEN = "access_token";
    private static final String REFRESH_TOKEN = "refresh_token";
    private static final String NEW_REFRESH_TOKEN = "new_token";
    private static final Date EXPIRATION_DATE = new Date();

    private AuthController authController;

    @Mock
    private AuthService authService;

    private AutoCloseable closeable;

    @BeforeEach
    public void setUp(){
        this.authController = new AuthController(authService);
        closeable = mockStatic(UsersMapper.class);
    }

    @AfterEach
    public void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void testSignUp() {
        // Given
        SignUpRequest request = new SignUpRequest(EMAIL, PASSWORD);
        User user = mock(User.class);
        when(UsersMapper.requestToEntity(request)).thenReturn(user);

        // When
        ResponseEntity<Void> responseEntity = authController.signUp(request);

        // Then
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
    }

    @Test
    void testLogin() {
        // Given
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn(URI);
        LoginRequest loginRequest = new LoginRequest(USERNAME, PASSWORD);
        User user = mock(User.class);
        AccessToken accessToken = new AccessToken(ACCESS_TOKEN, EXPIRATION_DATE, 120L, user);
        RefreshToken refreshToken = RefreshToken.builder()
                .token(REFRESH_TOKEN)
                .build();
        when(authService.login(USERNAME, PASSWORD, URI)).thenReturn(new AuthServiceImpl.Tokens(accessToken, refreshToken));
        when(UsersMapper.toLoginResponse(any())).thenReturn(LoginResponse.builder()
                .token(ACCESS_TOKEN)
                .refreshToken(REFRESH_TOKEN)
                .build());

        // When
        ResponseEntity<LoginResponse> responseEntity = authController.login(request, loginRequest);

        // Then
        LoginResponse response = responseEntity.getBody();
        assertNotNull(response);
        assertEquals(ACCESS_TOKEN, response.getToken());
        assertEquals(REFRESH_TOKEN, response.getRefreshToken());
        assertEquals("Bearer", response.getTokenType());
    }


    @Test
    void testLogout() throws Exception {
        // Given
        try(AutoCloseable ignored = mockStatic(SecurityContextHolder.class)) {
            SecurityContext securityContext = mock(SecurityContext.class);
            when(SecurityContextHolder.getContext()).thenReturn(securityContext);
            Authentication authentication = mock(Authentication.class);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            User user = User.builder()
                    .email(USERNAME)
                    .build();
            when(authentication.getPrincipal()).thenReturn(user);

            // When
            ResponseEntity<Void> responseEntity = authController.logout();
            assertNotNull(responseEntity);
            verify(authService, times(1)).logout(user);
        }
    }

    @Test
    void testRefreshToken() {
        // Given
        RefreshTokenRequest request = new RefreshTokenRequest(REFRESH_TOKEN);
        User user = mock(User.class);
        AccessToken accessToken = new AccessToken(ACCESS_TOKEN, EXPIRATION_DATE, 120L, user);
        RefreshToken refreshToken = RefreshToken.builder()
                .token(NEW_REFRESH_TOKEN)
                .build();
        when(authService.refreshToken(REFRESH_TOKEN)).thenReturn(new AuthServiceImpl.Tokens(accessToken, refreshToken));
        when(UsersMapper.toRefreshTokenResponse(any())).thenReturn(RefreshTokenResponse.builder()
                .token(ACCESS_TOKEN)
                .refreshToken(NEW_REFRESH_TOKEN)
                .expirationDate(EXPIRATION_DATE)
                .build());

        // When
        ResponseEntity<RefreshTokenResponse> responseEntity = authController.refreshToken(request);

        // Then
        RefreshTokenResponse response = responseEntity.getBody();
        assertNotNull(response);
        assertEquals(ACCESS_TOKEN, response.getToken());
        assertEquals(NEW_REFRESH_TOKEN, response.getRefreshToken());
        assertEquals(EXPIRATION_DATE, response.getExpirationDate());
        assertEquals("Bearer", response.getTokenType());
    }
}
