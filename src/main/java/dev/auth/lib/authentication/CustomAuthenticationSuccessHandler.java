package dev.auth.lib.authentication;

import dev.auth.lib.data.model.ExchangeSessionCode;
import dev.auth.lib.service.authentication.AuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;

@RequiredArgsConstructor
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Value("${server.front.host}")
    private String hostFrontend;

    @Value("${server.front.external-login-redirect-uri}")
    private String redirectUri;

    private final AuthService authService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        var opCode = authService.externalAccess(email);

        if(opCode.isPresent()){
            ExchangeSessionCode code = opCode.get();
            response.setStatus(HttpServletResponse.SC_OK);
            response.sendRedirect(String.format("%s%s?code=%s", hostFrontend, redirectUri, code.getCode()));
        }else{
            response.setStatus(HttpServletResponse.SC_CREATED);
            response.sendRedirect(String.format("%s%s", hostFrontend, redirectUri));
        }
    }
}
