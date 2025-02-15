package dev.auth.lib.authentication;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.auth.lib.data.model.AccessToken;
import dev.auth.lib.data.model.RefreshToken;
import dev.auth.lib.data.model.User;
import dev.auth.lib.service.authentication.AuthService;
import dev.auth.lib.service.authentication.impl.AuthServiceImpl;
import dev.auth.lib.utils.GlobalObjectMapper;
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
import java.io.PrintWriter;
import java.util.Date;

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
        AutoCloseable ac = mockStatic(GlobalObjectMapper.class);
        AccessToken accessToken = AccessToken.builder()
                .token("token")
                .expirationDate(new Date())
                .expiresIn(3600L)
                .user(new User())
                .build();
        RefreshToken refreshToken = RefreshToken.builder()
                .token("token")
                .expirationDate(new Date())
                .user(new User())
                .build();
        OAuth2User oAuth2User = mock(OAuth2User.class);
        PrintWriter writer = mock(PrintWriter.class);
        when(oAuth2User.getAttribute("email")).thenReturn(USER_MAIL);
        AuthServiceImpl.Tokens tokens = new AuthServiceImpl.Tokens(accessToken, refreshToken);
        when(authService.externalLogin(USER_MAIL)).thenReturn(java.util.Optional.of(tokens));
        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(GlobalObjectMapper.getInstance()).thenReturn(new ObjectMapper());
        when(response.getWriter()).thenReturn(writer);

        //When
        authenticationSuccessHandler.onAuthenticationSuccess(request, response, authentication);

        //Then
        verify(authService, times(1)).externalLogin(USER_MAIL);
        verify(response, times(1)).setContentType("application/json");
        verify(response, times(1)).setCharacterEncoding("UTF-8");
        verify(response, times(1)).setStatus(HttpServletResponse.SC_OK);

        ac.close();
    }

    @Test
    void testOnAuthenticationSuccessUserNotFound() throws ServletException, IOException {
        //Given
        OAuth2User oAuth2User = mock(OAuth2User.class);
        when(oAuth2User.getAttribute("email")).thenReturn(USER_MAIL);
        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(authService.externalLogin(USER_MAIL)).thenReturn(java.util.Optional.empty());

        //When
        authenticationSuccessHandler.onAuthenticationSuccess(request, response, authentication);

        //Then
        verify(authService, times(1)).externalLogin(USER_MAIL);
        verify(response, times(1)).setStatus(HttpServletResponse.SC_CREATED);
    }
}
