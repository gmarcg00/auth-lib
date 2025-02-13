package dev.auth.lib.integration;


import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import dev.auth.lib.controller.model.request.RecoveryPasswordActivateRequest;
import dev.auth.lib.data.model.User;
import dev.auth.lib.data.model.UserStatusEnum;
import dev.auth.lib.data.repository.UserRepository;
import dev.auth.lib.integration.utils.RequestValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ActivateRecoveryPasswordIntegrationTest {

    private static final String EMAIL = "fake.user@tenthman.com";

    private RequestValidator requestValidator;

    private MockMvc mvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserRepository userRepository;

    @RegisterExtension
    private static final GreenMailExtension GREEN_MAIL = new GreenMailExtension(ServerSetupTest.SMTP)
            .withConfiguration(GreenMailConfiguration.aConfig().withUser("spring", "boot"))
            .withPerMethodLifecycle(false);

    @BeforeEach
    public void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(context)
                .build();
        requestValidator = new RequestValidator(mvc);
    }

    @Test
    void testActivateRecoveryPasswordUserNotActive() throws Exception {
        requestValidator.validatePostRequest("/auth/recovery-password/activate", status().isBadRequest(),
                "integration/activate_recovery_password/request/user_inactive.json", "integration/activate_recovery_password/response/user_inactive.json");
    }

    @Test
    void testActivateRecoveryPasswordIncorrectVerificationCode() throws Exception {
        requestValidator.validatePostRequest("/auth/recovery-password/activate", status().isUnauthorized(),
                "integration/activate_recovery_password/request/incorrect_verification_code.json", "integration/activate_recovery_password/response/incorrect_verification_code.json");
    }

    @Test
    void testActivateRecoveryPasswordBadRequest() throws Exception {
        requestValidator.validatePostRequest("/auth/recovery-password/activate", status().isBadRequest(),
                "integration/activate_recovery_password/request/bad_request.json", "integration/activate_recovery_password/response/bad_request.json");
    }

    @Test
    void testActivateRecoveryPassword() throws Exception {
        String request = TestUtils.getStringFromFile("integration/activate_recovery_password/request/request_recovery.json");
        this.mvc.perform(post("/auth/recovery-password")
                        .contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isOk());

        Optional<User> oUser = userRepository.findByEmail(EMAIL);
        assertTrue(oUser.isPresent());
        String verificationCode = oUser.get().getVerificationCode();

        this.mvc.perform(post("/auth/recovery-password/activate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtils.asJsonString(new RecoveryPasswordActivateRequest(EMAIL, verificationCode, "newPassword"))))
                .andExpect(status().isOk());

        // Test datos BBDD
        oUser = userRepository.findByEmail(EMAIL);
        assertTrue(oUser.isPresent());
        User user = oUser.get();

        assertEquals(UserStatusEnum.ACTIVE.getStatusCode(), user.getStatus().getName());
        assertNotNull(user.getPassword());
        assertNull(user.getVerificationCode());
    }
}
