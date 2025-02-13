package dev.auth.lib.authentication;

import dev.auth.lib.authentication.provider.BasicAuthProvider;
import dev.auth.lib.data.model.User;
import dev.auth.lib.data.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BasicAuthProviderTest {

    private static final String EMAIL = "test@email.com";
    private static final String PASSWORD_TEST = "Password test";
    private static final String DATABASE_PASSWORD = "Database password";
    private static final String DATABASE_EMAIL = "database@email.com";

    private BasicAuthProvider basicAuthProvider;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private Authentication authentication;

    @BeforeEach
    public void setUp() {
        this.basicAuthProvider = new BasicAuthProvider(userRepository, passwordEncoder);
        User user = User.builder()
                .email(EMAIL)
                .password(PASSWORD_TEST)
                .build();
        when(authentication.getPrincipal()).thenReturn(user);
        when(authentication.getCredentials()).thenReturn(PASSWORD_TEST);
    }

    @Test
    void testAuthenticateUserNotFound() {
        // Given
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        // When
        Authentication result = basicAuthProvider.authenticate(authentication);

        // Then
        Assertions.assertFalse(result.isAuthenticated());
    }

    @Test
    void testAuthenticatePasswordNotMatch() {
        // Given
        User user = User.builder()
                .email(DATABASE_EMAIL)
                .password(DATABASE_PASSWORD)
                .build();
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(PASSWORD_TEST, DATABASE_PASSWORD)).thenReturn(false);

        // When
        Authentication result = basicAuthProvider.authenticate(authentication);

        // Then
        Assertions.assertFalse(result.isAuthenticated());
    }

    @Test
    void testAuthenticatePasswordMatch() {
        // Given
        User user = User.builder()
                .email(DATABASE_EMAIL)
                .password(DATABASE_PASSWORD)
                .build();
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(PASSWORD_TEST, DATABASE_PASSWORD)).thenReturn(true);

        // When
        Authentication result = basicAuthProvider.authenticate(authentication);

        // Then
        Assertions.assertTrue(result.isAuthenticated());
        Assertions.assertEquals(user, result.getPrincipal());
    }
}
