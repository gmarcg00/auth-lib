package dev.auth.lib.integration;

import dev.auth.lib.data.model.User;
import dev.auth.lib.data.model.UserStatusEnum;
import dev.auth.lib.data.repository.UserRepository;
import dev.auth.lib.integration.utils.RequestValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class RecoveryPasswordIntegrationTest {

    private static final String EMAIL = "fake.user@tenthman.com";

    private RequestValidator requestValidator;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    public void setup() {
        MockMvc mvc = MockMvcBuilders.webAppContextSetup(context)
                .build();
        requestValidator = new RequestValidator(mvc);
    }

    @Test
    void testWithoutEmail() throws Exception {
        requestValidator.validatePostRequest("/auth/recovery-password", status().isBadRequest(),
                "integration/recovery_password/request/bad_request.json", "integration/recovery_password/response/bad_request.json");
    }

    @Test
    void testUserInactive() throws Exception {
        requestValidator.validatePostRequest("/auth/recovery-password", status().isForbidden(),
                "integration/recovery_password/request/user_inactive.json", "integration/recovery_password/response/user_inactive.json");
    }

    @Test
    void testRecoveryPasswordSuccessful() throws Exception {
        requestValidator.validatePostRequest("/auth/recovery-password", status().isOk(),
                "integration/recovery_password/request/recovery_password_successful.json", "integration/recovery_password/response/recovery_password_successful.json");

        // Test datos BBDD
        Optional<User> oUser = userRepository.findByEmail(EMAIL);
        assertTrue(oUser.isPresent());
        User user = oUser.get();
        assertEquals(UserStatusEnum.ACTIVE.getStatusCode(), user.getStatus().getName());
        assertNotNull(user.getPassword());
        assertNotNull(user.getVerificationCode());

        //TO DO: CHECK MAIL SERVICE

    }
}
