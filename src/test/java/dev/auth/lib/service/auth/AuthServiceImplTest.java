package dev.auth.lib.service.auth;

import dev.auth.lib.data.model.Role;
import dev.auth.lib.data.model.User;
import dev.auth.lib.data.model.UserStatus;
import dev.auth.lib.service.authentication.impl.AuthServiceImpl;
import dev.auth.lib.service.users.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    private static final String USER_STATUS_ACTIVE = "ACTIVE";
    private static final String USER_ROLE = "TEST_ROLE";
    private static final String USER_PASSWORD = "User password";
    private static final String USER_MAIL = "test@mail.com";
    private static final String VERIFICATION_CODE = "abcdefgh";

    @InjectMocks
    private AuthServiceImpl authService;

    @Mock
    private UserService userService;

    private User inputUser;

    @BeforeEach
    public void setUp() {
        UserStatus userStatus = UserStatus.builder()
                .name(USER_STATUS_ACTIVE)
                .build();
        inputUser = User.builder()
                .password(USER_PASSWORD)
                .email(USER_MAIL)
                .status(userStatus)
                .build();
    }

    @Test
    void testSignUpSuccessful() {
        // Given
        Role role = new Role();
        role.setName(USER_ROLE);
        User databaseUser = User.builder()
                .password(USER_PASSWORD)
                .email(USER_MAIL)
                .roles(Set.of(role))
                .verificationCode(VERIFICATION_CODE)
                .build();
        when(userService.createUser(inputUser)).thenReturn(databaseUser);
        // When
        authService.signUp(inputUser);

        // Then
        verify(userService, times(1)).createUser(inputUser);
    }
}
