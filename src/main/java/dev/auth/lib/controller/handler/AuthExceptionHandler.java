package dev.auth.lib.controller.handler;

import dev.auth.lib.controller.model.response.BadRequestResponse;
import dev.auth.lib.controller.model.response.ExceptionResponse;
import dev.auth.lib.exception.InvalidCredentialsException;
import dev.auth.lib.exception.UserWithSameUsernameException;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Iterator;
import java.util.List;

@RestControllerAdvice
public class AuthExceptionHandler {

    private static final String INVALID_CREDENTIALS = "INVALID_CREDENTIALS";
    private static final String USERNAME_ALREADY_EXISTS = "USERNAME_ALREADY_EXISTS";
    private static final String INPUT_VALIDATION_ERROR_MESSAGE = "INPUT_VALIDATION_ERROR";

    @ExceptionHandler(UserWithSameUsernameException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ExceptionResponse handleUserWithSameUsernameException(UserWithSameUsernameException ex) {
        return new ExceptionResponse(USERNAME_ALREADY_EXISTS, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public BadRequestResponse handleValidationErrors(MethodArgumentNotValidException ex) {
        return new BadRequestResponse(HttpStatus.BAD_REQUEST.name(), INPUT_VALIDATION_ERROR_MESSAGE, this.extractViolations(ex));
    }

    private List<String> extractViolations(MethodArgumentNotValidException ex) {
        return ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + " " + e.getDefaultMessage()).toList();
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public BadRequestResponse handleHandlerMethodValidationException(ConstraintViolationException ex) {
        return new BadRequestResponse(HttpStatus.BAD_REQUEST.name(), INPUT_VALIDATION_ERROR_MESSAGE, this.extractViolations(ex));
    }

    private List<String> extractViolations(ConstraintViolationException ex) {
        return ex.getConstraintViolations().stream()
                .map(v -> cleanPropertyPath(v.getPropertyPath()) + " " + v.getMessage()).toList();
    }

    private String cleanPropertyPath(Path propertyPath) {
        Iterator<Path.Node> it = propertyPath.iterator();
        Path.Node lastNode = null;
        while (it.hasNext()) {
            lastNode = it.next();
        }
        return (lastNode != null) ? lastNode.getName() : "";
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ExceptionResponse handleLoginAuthenticationException(InvalidCredentialsException ex) {
        return new ExceptionResponse(INVALID_CREDENTIALS, ex.getMessage());
    }
}
