package dev.auth.lib.controller.model.response;

import lombok.Generated;

import java.util.List;

public class BadRequestResponse extends ExceptionResponse{
    private final List<String> violations;

    public BadRequestResponse(String code, String message, List<String> violations) {
        super(code, message);
        this.violations = violations;
    }

    @Generated
    public List<String> getViolations() {
        return this.violations;
    }
}
