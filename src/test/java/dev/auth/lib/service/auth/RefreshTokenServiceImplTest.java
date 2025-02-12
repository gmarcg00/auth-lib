package dev.auth.lib.service.auth;

import dev.auth.lib.data.model.RefreshToken;
import dev.auth.lib.data.model.User;
import dev.auth.lib.data.repository.RefreshTokenRepository;
import dev.auth.lib.service.authentication.impl.RefreshTokenServiceImpl;
import dev.auth.lib.utils.DateUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceImplTest {

    public static final String TOKEN = "token";
    private RefreshTokenServiceImpl refreshTokenService;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    private Date dateTest;

    private AutoCloseable closeable;

    @BeforeEach
    public void setUp() {
        refreshTokenService = new RefreshTokenServiceImpl(refreshTokenRepository);
        ReflectionTestUtils.setField(refreshTokenService, "expirationRefreshTokenTime", "360");

        closeable = mockStatic(DateUtils.class);
        dateTest = new Date(Long.MAX_VALUE);
        when(DateUtils.currentTimePlusSeconds(360L)).thenReturn(dateTest);
    }

    @AfterEach
    public void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void testFindByTokenSuccessful() {
        // Given
        String token = TOKEN;
        RefreshToken refreshToken = new RefreshToken();
        when(refreshTokenRepository.findByToken(token)).thenReturn(Optional.of(refreshToken));

        // When
        Optional<RefreshToken> oRefreshToken = refreshTokenService.findByToken(token);

        // Then
        assertEquals(Optional.of(refreshToken), oRefreshToken);
    }

    @Test
    void testCreateRefreshTokenFromDatabase() {
        // Given
        User user = new User();
        user.setId(1L);
        RefreshToken databaseRefreshToken = new RefreshToken();
        databaseRefreshToken.setToken(TOKEN);
        databaseRefreshToken.setUser(user);
        when(refreshTokenRepository.findByUserId(1L)).thenReturn(Optional.of(databaseRefreshToken));

        // When
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        // Then
        assertNotEquals(TOKEN, refreshToken.getToken());
        assertEquals(dateTest, refreshToken.getExpirationDate());
        assertEquals(databaseRefreshToken.getUser(), refreshToken.getUser());
        verify(refreshTokenRepository, times(1)).save(refreshToken);
    }

    @Test
    void testCreateRefreshTokenNewToken() {
        // Given
        User user = new User();
        user.setId(1L);
        when(refreshTokenRepository.findByUserId(1L)).thenReturn(Optional.empty());

        // When
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        // Then
        assertNotNull(refreshToken.getToken());
        assertEquals(refreshToken.getExpirationDate(), dateTest);
        assertEquals(refreshToken.getUser(), user);
        verify(refreshTokenRepository, times(1)).save(refreshToken);
    }

    @Test
    void testIsRefreshTokenValidTrue() {
        // Given
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setExpirationDate(new Date(Long.MAX_VALUE));

        // When
        boolean result = refreshTokenService.isRefreshTokenValid(refreshToken);

        // Then
        assertTrue(result);
        verify(refreshTokenRepository, never()).delete(refreshToken);
    }

    @Test
    void testIsRefreshTokenValidFalse() {
        // Given
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setExpirationDate(new Date(0));

        // When
        boolean result = refreshTokenService.isRefreshTokenValid(refreshToken);

        // Then
        assertFalse(result);
        verify(refreshTokenRepository, times(1)).delete(refreshToken);
    }

    @Test
    void testExistsRefreshTokenTrue() {
        // Given
        User user = User.builder()
                .id(1L)
                .build();
        when(refreshTokenRepository.findByUserId(1L)).thenReturn(Optional.of(new RefreshToken()));

        // When
        boolean result = refreshTokenService.existsRefreshToken(user);

        // Then
        assertTrue(result);
    }

    @Test
    void testExistsRefreshTokenFalse() {
        // Given
        User user = User.builder()
                .id(1L)
                .build();
        when(refreshTokenRepository.findByUserId(1L)).thenReturn(Optional.empty());

        // When
        boolean result = refreshTokenService.existsRefreshToken(user);

        // Then
        assertFalse(result);
    }

    @Test
    void testDeleteByUser() {
        // Given
        User user = User.builder().build();

        // When
        refreshTokenService.deleteByUser(user);

        // Then
        verify(refreshTokenRepository, times(1)).deleteByUser(user);
    }
}
