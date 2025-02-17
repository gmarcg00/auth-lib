package dev.auth.lib.exception;

public class InvalidExchangeCodeException extends RuntimeException {
    public InvalidExchangeCodeException(String message) {
        super(message);
    }
}
