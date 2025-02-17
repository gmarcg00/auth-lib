package dev.auth.lib.authentication;

import dev.auth.lib.controller.model.response.ExceptionResponse;
import dev.auth.lib.utils.GlobalObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final String RESPONSE_CONTENT_TYPE = "application/json";
    private static final String FORBIDDEN_RESPONSE_CODE = "FORBIDDEN";
    private static final String FORBIDDEN_RESPONSE_MESSAGE = "Access denied.";

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        response.setContentType(RESPONSE_CONTENT_TYPE);
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        String jsonResponse = GlobalObjectMapper.getInstance().writeValueAsString(new ExceptionResponse(FORBIDDEN_RESPONSE_CODE, FORBIDDEN_RESPONSE_MESSAGE));
        response.getWriter().write(jsonResponse);
    }
}
