package dev.auth.lib.integration;

import dev.auth.lib.data.model.Role;
import dev.auth.lib.data.model.User;
import dev.auth.lib.data.model.UserStatus;
import dev.auth.lib.data.repository.RoleRepository;
import dev.auth.lib.data.repository.UserRepository;
import dev.auth.lib.data.repository.UserStatusRepository;
import dev.auth.lib.service.users.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class SignUpIntegrationTest {

    private static final String PASSWORD = "Test password";
    private static final String EMAIL = "test@email.com";

    private MockMvc mvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserStatusRepository userStatusRepository;

    @BeforeEach
    public void setup() {
        this.mvc = MockMvcBuilders.webAppContextSetup(context)
                .build();
    }

    @Test
    void testBodyWithoutEmail() throws Exception {
        String request = TestUtils.getJsonFromFile("integration/sign_up/request/without_email.json");
        this.mvc.perform(post("/auth/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUserWithSameUsername() throws Exception {
        String request = TestUtils.getJsonFromFile("integration/sign_up/request/with_same_username.json");
        this.mvc.perform(post("/auth/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("message").value("This username exists in system."));
    }

    @Test
    void testSignupSuccessful() throws Exception {
        String request = TestUtils.getJsonFromFile("integration/sign_up/request/sign_up_successful.json");
        this.mvc.perform(post("/auth/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated());

        // Test datos BBDD
        Optional<User> oUser = this.userRepository.findByEmail(EMAIL);
        assertTrue(oUser.isPresent());
        User user = oUser.get();
        assertEquals(EMAIL, user.getUsername());
        assertTrue(new BCryptPasswordEncoder().matches(PASSWORD, user.getPassword()));
        assertEquals(EMAIL, user.getEmail());
        Optional<Role> role = this.roleRepository.findByName(UserService.DEFAULT_ROLE);
        assertTrue(user.getFlattenRoles().contains(role.get().getName()));
        assertNotNull(user.getCreationDate());
        assertFalse(user.getExternalUser());
        UserStatus userStatus = this.userStatusRepository.findByName(UserService.DEFAULT_STATUS.getStatusCode());
        assertEquals(userStatus.getName(), user.getStatus().getName());
    }
}
