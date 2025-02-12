package dev.auth.lib.exception;

public class UserAlreadyValidatedException extends RuntimeException{
    public UserAlreadyValidatedException(String message) {
        super(message);
    }
}
