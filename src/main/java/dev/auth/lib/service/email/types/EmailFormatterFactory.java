package dev.auth.lib.service.email.types;

import dev.auth.lib.data.model.User;

public class EmailFormatterFactory {

    private EmailFormatterFactory() {
        throw new IllegalStateException("Utility class");
    }

    public static EmailFormatter createUserRegistrationEmailFormatter(User user, String hostFront) {
        return new UserRegistrationEmailFormatter(user, hostFront);
    }

    public static EmailFormatter createUserRecoveryPasswordEmailFormatter(User user, String hostFront) {
        return new UserRecoveryPasswordEmailFormatter(user, hostFront);
    }
}
