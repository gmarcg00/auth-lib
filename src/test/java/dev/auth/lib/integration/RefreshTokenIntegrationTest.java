package dev.auth.lib.integration;

import dev.auth.lib.data.model.RefreshToken;
import dev.auth.lib.data.repository.RefreshTokenRepository;
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

import java.util.Date;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class RefreshTokenIntegrationTest {

    private static final String EMAIL = "fake.user@tenthman.com";

    private RequestValidator requestValidator;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    public void setup() {
        MockMvc mvc = MockMvcBuilders.webAppContextSetup(context)
                .build();
        requestValidator = new RequestValidator(mvc);
    }

    @Test
    void testRefreshTokenNotFound() throws Exception {
        requestValidator.validatePostRequest("/auth/refresh-token", status().isUnauthorized(),
                "integration/refresh_token/request/token_not_found.json", "integration/refresh_token/response/token_not_found.json");
    }

    @Test
    void testInvalidRefreshToken() throws Exception {
        RefreshToken refreshToken = RefreshToken.builder()
                .user(this.userRepository.findByEmail(EMAIL).get())
                .token("7a3228c4-0952-4a8a-b480-592879c05838")
                .expirationDate(new Date(0))
                .build();

        this.refreshTokenRepository.save(refreshToken);

        requestValidator.validatePostRequest("/auth/refresh-token", status().isUnauthorized(),
                "integration/refresh_token/request/invalid_refresh_token.json", "integration/refresh_token/response/invalid_refresh_token.json");
    }

    @Test
    void testRefreshTokenSuccessful() throws Exception {
        RefreshToken refreshToken = RefreshToken.builder()
                .user(this.userRepository.findByEmail(EMAIL).get())
                .token("7a3228c4-0952-4a8a-b480-592879c05838")
                .expirationDate(new Date(System.currentTimeMillis() + 60_000))
                .build();

        this.refreshTokenRepository.save(refreshToken);

        requestValidator.validateCustomPostRequest("/auth/refresh-token", status().isOk(),
                "integration/refresh_token/request/refresh_token_successful.json", "integration/refresh_token/response/refresh_token_successful.json",
                new String[]{"expiration_date", "refresh_token", "token"});
    }
}
