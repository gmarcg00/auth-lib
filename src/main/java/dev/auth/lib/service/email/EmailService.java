package dev.auth.lib.service.email;

import dev.auth.lib.service.email.types.EmailFormatter;

public interface EmailService {
    void sendEmail(String to, EmailFormatter emailFormatter);
}
