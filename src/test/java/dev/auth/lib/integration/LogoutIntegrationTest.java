package dev.auth.lib.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.auth.lib.controller.model.response.LoginResponse;
import dev.auth.lib.data.model.RefreshToken;
import dev.auth.lib.data.repository.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class LogoutIntegrationTest {

    private MockMvc mvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private RefreshTokenRepository repository;

    @BeforeEach
    public void setup() {
        this.mvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    void testUnauthenticatedLogout() throws Exception {
        this.mvc.perform(get("/auth/sessions")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void testLogoutSuccessful() throws Exception {
        String request = TestUtils.getStringFromFile("integration/login/request/login_successful.json");
        MvcResult result = this.mvc.perform(post("/auth/sessions")
                        .contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        LoginResponse loginResponse = new ObjectMapper().readValue(response, LoginResponse.class);
        String refreshToken = loginResponse.getRefreshToken();

        this.mvc.perform(get("/auth/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + loginResponse.getToken()))
                .andExpect(status().isOk());

        Optional<RefreshToken> oRefreshToken = this.repository.findByToken(refreshToken);
        assertTrue(oRefreshToken.isEmpty());
    }
}
