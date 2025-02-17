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
class ExternalLoginIntegrationTest {

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
    void testBodyWithoutCode() throws Exception {
        requestValidator.validatePostRequest("/auth/sessions/external", status().isBadRequest(),
                "integration/external_login/request/without_code.json","integration/external_login/response/without_code.json");
    }

    @Test
    void testExpiredCode() throws Exception {
        requestValidator.validatePostRequest("/auth/sessions/external", status().isBadRequest(),
                "integration/external_login/request/expired_code.json", "integration/external_login/response/expired_code.json");
    }

    @Test
    void testExternalLoginSuccessful() throws Exception {
        requestValidator.validateCustomPostRequest("/auth/sessions/external", status().isOk(),
                "integration/external_login/request/external_login_successful.json", "integration/external_login/response/external_login_successful.json",
                new String[]{"expiration_date", "refresh_token", "token"}
        );
    }
}
