package dev.auth.lib.authentication;

import dev.auth.lib.controller.mappers.UsersMapper;
import dev.auth.lib.controller.model.response.LoginResponse;
import dev.auth.lib.service.authentication.AuthService;
import dev.auth.lib.service.authentication.impl.AuthServiceImpl;
import dev.auth.lib.utils.GlobalObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;

@RequiredArgsConstructor
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private static final String RESPONSE_CONTENT_TYPE = "application/json";
    private static final String RESPONSE_CHARACTER_ENCODING = "UTF-8";

    private final AuthService authService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        var opTokens = authService.externalLogin(email);

        if(opTokens.isPresent()){
            AuthServiceImpl.Tokens tokens = opTokens.get();
            LoginResponse loginResponse  = UsersMapper.toLoginResponse(tokens);

            response.setContentType(RESPONSE_CONTENT_TYPE);
            response.setCharacterEncoding(RESPONSE_CHARACTER_ENCODING);

            String jsonResponse = GlobalObjectMapper.getInstance().writeValueAsString(loginResponse);
            response.getWriter().write(jsonResponse);
            response.setStatus(HttpServletResponse.SC_OK);
        }else{
            response.setStatus(HttpServletResponse.SC_CREATED);
        }
    }
}
