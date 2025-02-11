package dev.auth.lib.integration;

import dev.auth.lib.integration.utils.RequestValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class LoginIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private RequestValidator requestValidator;

    @BeforeEach
    public void setUp(){
        MockMvc mvc = MockMvcBuilders.webAppContextSetup(context)
                .build();
        requestValidator = new RequestValidator(mvc);
    }

    @Test
    void testBodyWithoutUser() throws Exception {
        requestValidator.validatePostRequest("/auth/sessions", status().isBadRequest(),
                "integration/login/request/without_user.json","integration/login/response/without_user.json");
    }

    @Test
    void testInvalidCredentials() throws Exception {
        requestValidator.validatePostRequest("/auth/sessions", status().isUnauthorized(),
                "integration/login/request/invalid_credentials.json", "integration/login/response/invalid_credentials.json");
    }

    @Test
    void testUserNotActive() throws Exception {
        requestValidator.validatePostRequest("/auth/sessions", status().isUnauthorized(),
                "integration/login/request/user_not_active.json", "integration/login/response/user_not_active.json");
    }

    @Test
    void testLoginSuccessful() throws Exception {
        requestValidator.validateCustomPostRequest("/auth/sessions", status().isOk(),
                "integration/login/request/login_successful.json", "integration/login/response/login_successful.json",
                new String[]{"expiration_date", "refresh_token", "token"}
        );
    }

}
