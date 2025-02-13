package dev.auth.lib.exception;

public class PasswordAlreadyAddedException extends RuntimeException{
    public PasswordAlreadyAddedException(String message) {
        super(message);
    }
}
