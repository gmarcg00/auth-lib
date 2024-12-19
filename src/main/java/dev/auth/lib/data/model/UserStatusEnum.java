package dev.auth.lib.data.model;

import lombok.Getter;

@Getter
public enum UserStatusEnum {
    ACTIVE("ACTIVE"),
    INACTIVE("INACTIVE"),
    VERIFICATION_PENDING("VERIFICATION_PENDING");

    private final String statusCode;

    UserStatusEnum(String statusCode) {
        this.statusCode = statusCode;
    }

    public static UserStatusEnum findByStatusCode(String statusCode) {
        for (UserStatusEnum status: UserStatusEnum.values()) {
            if (status.statusCode.equals(statusCode)) {
                return status;
            }
        }

        throw new IllegalArgumentException("There is not status: " + statusCode);
    }
}
