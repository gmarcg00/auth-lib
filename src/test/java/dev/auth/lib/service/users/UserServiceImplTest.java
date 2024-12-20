package dev.auth.lib.service.users;

import dev.auth.lib.data.model.Role;
import dev.auth.lib.data.model.User;
import dev.auth.lib.data.model.UserStatus;
import dev.auth.lib.data.repository.RoleRepository;
import dev.auth.lib.data.repository.UserRepository;
import dev.auth.lib.data.repository.UserStatusRepository;
import dev.auth.lib.exception.UserWithSameUsernameException;
import dev.auth.lib.service.users.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.util.Pair;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    private static final String EMAIL = "username@email.com";
    private static final String PASSWORD = "password";
    private static final String ENCODED_PASSWORD = "encoded_password";
    private static final String DEFAULT_ROLE_NAME = "USER";
    private static final String VERIFICATION_PENDING_STATUS_NAME = "VERIFICATION_PENDING";

    private UserServiceImpl userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserStatusRepository userStatusRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    public void setUp() {
        userService = new UserServiceImpl(userRepository, roleRepository, userStatusRepository, passwordEncoder);
    }

    @Test
    void testCreateUserWithExistingUser() {
        // Given
        User user = User.builder()
                .email(EMAIL)
                .build();
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

        // When
        UserWithSameUsernameException exception = assertThrows(UserWithSameUsernameException.class, () -> userService.createUser(user));

        // Then
        assertEquals("This username exists in system.", exception.getMessage());
    }

    @Test
    void testCreateUserSuccessful() throws Exception {
        // Given
        Pair<User, AutoCloseable> result = mockUserCreation(true ,List.of(DEFAULT_ROLE_NAME));
        User user = result.getFirst();

        // When
        User savedUser = userService.createUser(user);

        // Then
        verify(userRepository, times(1)).save(any(User.class));
        assertEquals(EMAIL, savedUser.getUsername());
        assertEquals(ENCODED_PASSWORD, savedUser.getPassword());
        assertEquals(Instant.MAX, savedUser.getCreationDate());
        assertFalse(savedUser.getExternalUser());
        Collection<String> roles = savedUser.getFlattenRoles();
        assertEquals(1, roles.size());
        assertTrue(roles.contains(DEFAULT_ROLE_NAME));
        assertEquals(VERIFICATION_PENDING_STATUS_NAME, savedUser.getStatus().getName());

        result.getSecond().close();
    }

    private Pair<User, AutoCloseable> mockUserCreation(boolean withPassword, List<String> roles) {
        AutoCloseable closeable = mockStatic(Instant.class);
        String password = (withPassword) ? PASSWORD : null;
        User user = User.builder()
                .email(EMAIL)
                .password(password)
                .build();
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        if (withPassword)
            when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(Instant.now()).thenReturn(Instant.MAX);
        for(String role : roles) {
            Role roleUser = new Role();
            roleUser.setName(role);
            when(roleRepository.findByName(role)).thenReturn(Optional.of(roleUser));
        }
        UserStatus userStatus = UserStatus.builder()
                .name(VERIFICATION_PENDING_STATUS_NAME)
                .build();
        when(userStatusRepository.findByName(VERIFICATION_PENDING_STATUS_NAME)).thenReturn(userStatus);

        return Pair.of(user, closeable);
    }
}
