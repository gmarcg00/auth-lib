package dev.auth.lib.exception;

public class ExchangeCodeExpiredException extends RuntimeException {
    public ExchangeCodeExpiredException(String message) {
        super(message);
    }
}
