package dev.auth.lib.controller.model.response;

import lombok.Data;

@Data
public class ExceptionResponse {
    private final String code;
    private final String message;

    public ExceptionResponse(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
