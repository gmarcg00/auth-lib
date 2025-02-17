package dev.auth.lib.authentication;

import dev.auth.lib.data.model.ExchangeSessionCode;
import dev.auth.lib.data.model.User;
import dev.auth.lib.service.authentication.AuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;


import java.io.IOException;
import java.time.Instant;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomAuthenticationSuccessHandlerTest {

    private static final String USER_MAIL = "test@mail.com";

    private CustomAuthenticationSuccessHandler authenticationSuccessHandler;

    @Mock
    private AuthService authService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private Authentication authentication;

    @BeforeEach
    public void setUp() {
        this.authenticationSuccessHandler = new CustomAuthenticationSuccessHandler(authService);
    }

    @Test
    void testOnAuthenticationSuccessUserFound() throws Exception {
        //Given
        ExchangeSessionCode exchangeSessionCode = ExchangeSessionCode.builder()
                .code("code")
                .user(User.builder().email(USER_MAIL).build())
                .expirationDate(Instant.now())
                .build();
        OAuth2User oAuth2User = mock(OAuth2User.class);
        when(oAuth2User.getAttribute("email")).thenReturn(USER_MAIL);
        when(authService.externalAccess(USER_MAIL)).thenReturn(java.util.Optional.of(exchangeSessionCode));
        when(authentication.getPrincipal()).thenReturn(oAuth2User);

        //When
        authenticationSuccessHandler.onAuthenticationSuccess(request, response, authentication);

        //Then
        verify(authService, times(1)).externalAccess(USER_MAIL);
        verify(response, times(1)).setStatus(HttpServletResponse.SC_OK);

    }

    @Test
    void testOnAuthenticationSuccessUserNotFound() throws ServletException, IOException {
        //Given
        OAuth2User oAuth2User = mock(OAuth2User.class);
        when(oAuth2User.getAttribute("email")).thenReturn(USER_MAIL);
        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(authService.externalAccess(USER_MAIL)).thenReturn(java.util.Optional.empty());

        //When
        authenticationSuccessHandler.onAuthenticationSuccess(request, response, authentication);

        //Then
        verify(authService, times(1)).externalAccess(USER_MAIL);
        verify(response, times(1)).setStatus(HttpServletResponse.SC_CREATED);
    }
}
