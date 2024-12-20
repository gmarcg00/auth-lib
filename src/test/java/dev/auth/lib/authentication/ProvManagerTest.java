package dev.auth.lib.authentication;

import dev.auth.lib.authentication.model.AuthenticationRequestDetails;
import dev.auth.lib.authentication.provider.BasicAuthProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProvManagerTest {

    private ProvManager provManager;

    @Mock
    private BasicAuthProvider basicAuthProvider;

    @Mock
    private Authentication authentication;

    @Mock
    private AuthenticationRequestDetails authenticationRequestDetails;

    @BeforeEach
    public void setUp() {
        this.provManager = new ProvManager(basicAuthProvider);
        when(authentication.getDetails()).thenReturn(authenticationRequestDetails);
    }

    @Test
    void testAuthenticateNotExpectedURL() {
        // Given
        when(authenticationRequestDetails.getUri()).thenReturn("/other/url");

        // When
        Authentication result = provManager.authenticate(authentication);

        // Then
        assertNull(result);
    }

    @Test
    void testExpectedUrl() {
        // Given
        when(authenticationRequestDetails.getUri()).thenReturn(ProvManager.LOGIN_PATH);
        Authentication expectedAuth = Mockito.mock(Authentication.class);
        when(basicAuthProvider.authenticate(authentication)).thenReturn(expectedAuth);

        // When
        Authentication result = provManager.authenticate(authentication);

        // Then
        assertEquals(expectedAuth, result);
    }
}
