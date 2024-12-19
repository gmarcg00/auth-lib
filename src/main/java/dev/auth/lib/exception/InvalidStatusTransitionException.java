package dev.auth.lib.exception;

import dev.auth.lib.data.model.UserStatusEnum;

public class InvalidStatusTransitionException extends Exception{

    public InvalidStatusTransitionException(UserStatusEnum source, UserStatusEnum target) {
        this(String.format("Transition from %s to %s is not valid.", source, target));
    }

    public InvalidStatusTransitionException(String message) {
        super(message);
    }
}
