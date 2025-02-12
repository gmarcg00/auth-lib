package dev.auth.lib.exception;

public class MandatoryPasswordException extends RuntimeException{
    public MandatoryPasswordException(String message) {
        super(message);
    }
}
