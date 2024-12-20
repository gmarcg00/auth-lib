package dev.auth.lib.exception;

public class UserWithSameUsernameException extends RuntimeException{
    public UserWithSameUsernameException(String message) {
        super(message);
    }
}
