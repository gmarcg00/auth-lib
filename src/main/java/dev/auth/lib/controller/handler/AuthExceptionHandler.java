package dev.auth.lib.controller.handler;

import dev.auth.lib.controller.model.response.ExceptionResponse;
import dev.auth.lib.exception.UserWithSameUsernameException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AuthExceptionHandler {

    private static final String USERNAME_ALREADY_EXISTS = "USERNAME_ALREADY_EXISTS";

    @ExceptionHandler(UserWithSameUsernameException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ExceptionResponse handleUserWithSameUsernameException(UserWithSameUsernameException ex) {
        return new ExceptionResponse(USERNAME_ALREADY_EXISTS, ex.getMessage());
    }
}
