package dev.auth.lib.service.auth;

import dev.auth.lib.data.model.User;
import dev.auth.lib.data.repository.UserRepository;
import dev.auth.lib.integration.utils.RequestValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ActivateUserIntegrationTest {

    private static final String USER_PENDING_EMAIL = "pending.user@tenthman.com";
    public static final String USER_PENDING_WITHOUT_PASSWORD = "pending.user.no.password@tenthman.com";
    public static final String NEW_PASSWORD = "new_password";

    private RequestValidator requestValidator;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setup() {
        MockMvc mvc = MockMvcBuilders.webAppContextSetup(context)
                .build();
        requestValidator = new RequestValidator(mvc);
    }

    @Test
    void testBodyWithoutEmailAndVerificationCode() throws Exception {
        requestValidator.validatePostRequest("/auth/activate", status().isBadRequest(),
                "integration/activate_user/request/without_email_verification_code.json", "integration/activate_user/response/without_email_verification_code.json");
    }

    @Test
    void testBodyWithNotExistingUser() throws Exception {
        requestValidator.validatePostRequest("/auth/activate", status().isUnauthorized(),
                "integration/activate_user/request/not_existing_user.json", "integration/activate_user/response/not_existing_user.json");
    }

    @Test
    void testBodyWithUserAlreadyActivated() throws Exception {
        requestValidator.validatePostRequest("/auth/activate", status().isBadRequest(),
                "integration/activate_user/request/user_already_activated.json", "integration/activate_user/response/user_already_activated.json");
    }

    @Test
    void testBodyWithInvalidVerificationCode() throws Exception {
        requestValidator.validatePostRequest("/auth/activate", status().isUnauthorized(),
                "integration/activate_user/request/invalid_verification_code.json", "integration/activate_user/response/invalid_verification_code.json");
    }

    @Test
    void testBodyWithUserWithoutPasswordAndIsMandatory() throws Exception {
        requestValidator.validatePostRequest("/auth/activate", status().isBadRequest(),
                "integration/activate_user/request/password_mandatory.json", "integration/activate_user/response/password_mandatory.json");
    }

    @Test
    void testBodyWithUserWithPasswordAndCanNotChanged() throws Exception {
        requestValidator.validatePostRequest("/auth/activate", status().isBadRequest(),
                "integration/activate_user/request/user_with_password_can_not_changed.json", "integration/activate_user/response/user_with_password_can_not_changed.json");
    }

    @Test
    void testWithoutPasswordSuccessful() throws Exception {
        requestValidator.validatePostRequest("/auth/activate", status().isOk(),
                "integration/activate_user/request/without_password_successful.json", "integration/activate_user/response/without_password_successful.json");

        // Comprobar los datos de la base de datos
        Optional<User> oUser =  userRepository.findByEmail(USER_PENDING_EMAIL);
        assertTrue(oUser.isPresent());
        User user = oUser.get();
        assertEquals("ACTIVE", user.getStatus().getName());
        assertNull(user.getVerificationCode());
    }

    @Test
    void testWithPasswordSuccessful() throws Exception {
        requestValidator.validatePostRequest("/auth/activate", status().isOk(),
                "integration/activate_user/request/with_password_successful.json", "integration/activate_user/response/with_password_successful.json");

        // Comprobar los datos de la base de datos
        Optional<User> oUser =  userRepository.findByEmail(USER_PENDING_WITHOUT_PASSWORD);
        assertTrue(oUser.isPresent());
        User user = oUser.get();
        assertEquals("ACTIVE", user.getStatus().getName());
        assertNull(user.getVerificationCode());
        assertTrue(new BCryptPasswordEncoder().matches(NEW_PASSWORD, user.getPassword()));
    }
}
