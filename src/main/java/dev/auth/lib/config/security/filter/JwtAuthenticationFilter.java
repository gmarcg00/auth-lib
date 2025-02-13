package dev.auth.lib.config.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.auth.lib.controller.model.response.ExceptionResponse;
import dev.auth.lib.data.model.User;
import dev.auth.lib.service.authentication.JwtService;
import dev.auth.lib.service.users.UserService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_TOKEN_PREFIX = "Bearer ";
    private static final String INVALID_TOKEN= "INVALID_TOKEN";

    private final JwtService jwtService;

    private final UserService userService;

    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = this.extractToken(request);
            Optional<User> user = this.getUser(token);
            user.ifPresent(this::saveAuthenticatedUser);
            filterChain.doFilter(request, response);
        } catch (AccessTokenNotFoundException e) {
            filterChain.doFilter(request, response);
        } catch (ExpiredJwtException | SignatureException | MalformedJwtException exception) {
            handleJwtException(exception, response);
        }
    }

    private String extractToken(HttpServletRequest request) throws AccessTokenNotFoundException {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith(BEARER_TOKEN_PREFIX)) {
            throw new AccessTokenNotFoundException("Not bearer token en authorization header.");
        }

        String[] segments = authHeader.split(" ");
        if (segments.length < 2) {
            throw new AccessTokenNotFoundException("Not bearer token en authorization header.");
        }

        return authHeader.split(" ")[1];
    }

    private Optional<User> getUser(String token) {
        String username = jwtService.extractUsername(token);
        return userService.findByEmail(username);
    }

    private void saveAuthenticatedUser(User user) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
    }

    private void handleJwtException(Exception exception, HttpServletResponse response) throws IOException {
        ExceptionResponse exceptionResponse = new ExceptionResponse(INVALID_TOKEN, exception.getLocalizedMessage());
        sendResponse(response, exceptionResponse);
    }

    private void sendResponse(HttpServletResponse response, ExceptionResponse exceptionResponse) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write(objectMapper.writeValueAsString(exceptionResponse));
    }

    public static class AccessTokenNotFoundException extends Exception {
        public AccessTokenNotFoundException(String message) {
            super(message);
        }
    }
}
