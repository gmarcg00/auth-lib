package dev.auth.lib.service.email.types;

import dev.auth.lib.data.model.User;
import lombok.RequiredArgsConstructor;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@RequiredArgsConstructor
public class UserCreationEmailFormatter implements EmailFormatter {

    private static final String SUBJECT = "%s - invitación a participar en %s";
    private static final String VERIFICATION_CODE_VARIABLE = "verificationCode";
    private static final String LINK_VAR = "link";
    private static final String EMAIL_TEMPLATE = "email/user-creation-verification";

    private final User user;
    private final String hostFrontend;

    @Override
    public String getSubject(String applicationTitle) {
        return SUBJECT.formatted(applicationTitle, applicationTitle);
    }

    @Override
    public String getContent(SpringTemplateEngine templateEngine) {
        Context context = new Context();
        context.setVariable(LINK_VAR, generateLink());
        context.setVariable(VERIFICATION_CODE_VARIABLE, user.getVerificationCode());
        return templateEngine.process(EMAIL_TEMPLATE, context);
    }

    private String generateLink() {
        return String.format("%s/sign-up?email=%s&showpass=true&verificationCode=%s", hostFrontend, user.getEmail(), user.getVerificationCode());
    }
}
