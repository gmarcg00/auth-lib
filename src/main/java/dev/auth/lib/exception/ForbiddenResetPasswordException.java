package dev.auth.lib.exception;

public class ForbiddenResetPasswordException extends RuntimeException {
    public ForbiddenResetPasswordException(String message) {
        super(message);
    }
}
