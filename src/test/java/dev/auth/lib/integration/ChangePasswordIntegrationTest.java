package dev.auth.lib.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.auth.lib.controller.model.request.LoginRequest;
import dev.auth.lib.controller.model.response.LoginResponse;
import dev.auth.lib.data.model.User;
import dev.auth.lib.data.repository.UserRepository;
import dev.auth.lib.integration.security.WithMockCustomUser;
import dev.auth.lib.integration.utils.RequestValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Optional;

import static dev.auth.lib.integration.TestUtils.asJsonString;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ChangePasswordIntegrationTest {

    private MockMvc mvc;

    private RequestValidator requestValidator;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserRepository repository;

    @BeforeEach
    public void setup() {
        this.mvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
        requestValidator = new RequestValidator(mvc);
    }

    @WithMockCustomUser()
    @Test
    void testBodyWithoutPasswordAndOldPassword() throws Exception {
        requestValidator.validatePostRequest("/auth/change-password", status().isBadRequest(),
                "integration/change_password/request/without_password_and_old_password.json", "integration/change_password/response/without_password_and_old_password.json");
    }

    @WithMockCustomUser()
    @Test
    void testBadOldPassword() throws Exception {
        LoginResponse loginResponse = login();

        String request = TestUtils.getStringFromFile("integration/change_password/request/bad_old_password.json");
        this.mvc.perform(post("/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + loginResponse.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isUnauthorized());
    }

    private LoginResponse login() throws Exception {
        MvcResult result = this.mvc.perform(post("/auth/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(new LoginRequest("fake.user@tenthman.com", "fakepassword"))))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        return new ObjectMapper().readValue(response, LoginResponse.class);
    }

    @Test
    void testChangePasswordSuccessful() throws Exception {
        Optional<User> previousUser = repository.findByEmail("fake.user@tenthman.com");

        LoginResponse loginResponse = login();

        String request = TestUtils.getStringFromFile("integration/change_password/request/change_password_successful.json");
        this.mvc.perform(post("/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + loginResponse.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk());

        Optional<User> oUser = repository.findByEmail("fake.user@tenthman.com");
        assertTrue(new BCryptPasswordEncoder().matches("newPassword", oUser.get().getPassword()));
        assertNotEquals(previousUser.get().getLastPasswordChange(), oUser.get().getLastPasswordChange());
    }
}
