package dev.auth.lib.service.email.types;

import org.thymeleaf.spring6.SpringTemplateEngine;

public interface EmailFormatter {
    String getSubject(String applicationTitle);
    String getContent(SpringTemplateEngine templateEngine);
}
