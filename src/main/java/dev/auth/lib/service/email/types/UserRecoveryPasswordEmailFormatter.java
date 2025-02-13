package dev.auth.lib.service.email.types;

import dev.auth.lib.data.model.User;
import lombok.RequiredArgsConstructor;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@RequiredArgsConstructor
public class UserRecoveryPasswordEmailFormatter implements EmailFormatter{

    private static final String SUBJECT = "%s - recuperación de contraseña";
    private static final String VERIFICATION_CODE_VARIABLE = "verificationCode";
    private static final String LINK_VARIABLE = "link";
    private static final String EMAIL_TEMPLATE = "email/recovery-password";

    private final User user;
    private final String hostFrontend;

    @Override
    public String getSubject(String applicationTitle) {
        return SUBJECT.formatted(applicationTitle);
    }

    @Override
    public String getContent(SpringTemplateEngine templateEngine) {
        Context context = new Context();
        context.setVariable(VERIFICATION_CODE_VARIABLE, user.getVerificationCode());
        context.setVariable(LINK_VARIABLE, generateLink());
        return templateEngine.process(EMAIL_TEMPLATE, context);
    }

    private String generateLink() {
        return String.format("%s/sign-up?email=%s&verificationCode=%s&showpass=true", hostFrontend, user.getEmail(), user.getVerificationCode());
    }
}
