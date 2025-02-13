package dev.auth.lib.service.email;

import dev.auth.lib.data.model.User;
import dev.auth.lib.service.email.types.EmailFormatter;
import dev.auth.lib.service.email.types.EmailFormatterFactory;
import dev.auth.lib.service.email.types.UserRecoveryPasswordEmailFormatter;
import dev.auth.lib.service.email.types.UserRegistrationEmailFormatter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class EmailFormatterFactoryTest {

    @Test
    void testCreateUserRegistrationEmailFormatter() {
        // When
        EmailFormatter emailFormatter = EmailFormatterFactory.createUserRegistrationEmailFormatter(new User(), "");

        // Then
        Assertions.assertEquals(emailFormatter.getClass(), UserRegistrationEmailFormatter.class);
    }

    @Test
    void testUserRecoveryPasswordEmailFormatter() {
        // When
        EmailFormatter emailFormatter = EmailFormatterFactory.createUserRecoveryPasswordEmailFormatter(new User(), "");

        // Then
        Assertions.assertEquals(emailFormatter.getClass(), UserRecoveryPasswordEmailFormatter.class);
    }
}
