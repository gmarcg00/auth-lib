package dev.auth.lib.service.email;

import dev.auth.lib.data.model.User;
import dev.auth.lib.service.email.types.UserRecoveryPasswordEmailFormatter;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

class UserRecoveryPasswordFormatterTest {

    private static final String TEST_CONTENT = "Test content";
    private static final String VERIFICATION_CODE = "000";

    @Test
    void testSubject() {
        // Given
        UserRecoveryPasswordEmailFormatter formatter = new UserRecoveryPasswordEmailFormatter(new User(), "");

        // When
        String subject = formatter.getSubject("Dr.Grey");

        // Then
        assertEquals("Dr.Grey - recuperación de contraseña", subject);
    }

    @Test
    void testContent() {
        // Given
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));
        User u = mock(User.class);
        when(u.getVerificationCode()).thenReturn(VERIFICATION_CODE);
        UserRecoveryPasswordEmailFormatter formatter = new UserRecoveryPasswordEmailFormatter(u, "");
        SpringTemplateEngine templateEngine = Mockito.mock(SpringTemplateEngine.class);
        try (MockedConstruction<Context> mockedConstruction = mockConstruction(Context.class)) {
            when(templateEngine.process(eq("email/recovery-password"), any(Context.class))).thenReturn(TEST_CONTENT);

            // When
            String content = formatter.getContent(templateEngine);

            // Then
            Context context = mockedConstruction.constructed().get(0);
            verify(context, times(1)).setVariable("verificationCode", VERIFICATION_CODE);
            assertEquals(TEST_CONTENT, content);
        }
    }
}
