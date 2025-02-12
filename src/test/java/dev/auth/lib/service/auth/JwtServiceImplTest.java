package dev.auth.lib.service.auth;

import dev.auth.lib.data.model.AccessToken;
import dev.auth.lib.data.model.Role;
import dev.auth.lib.data.model.User;
import dev.auth.lib.service.authentication.JwtService;
import dev.auth.lib.service.authentication.impl.JwtServiceImpl;
import dev.auth.lib.utils.DateUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtServiceImplTest {

    private static final String EMAIL = "test@email.com";
    private static final String SECRET_KEY = "cWkgY3xhdmUgZXMgbXV5IHNlZ4VyYSByMjM0NTY4OCBhZmNkOWZn";
    private static final String ROLE = "TEST";

    private JwtService jwtService;

    @BeforeEach
    public void setUp() {
        jwtService = new JwtServiceImpl();
        ReflectionTestUtils.setField(jwtService, "secret", SECRET_KEY);
        ReflectionTestUtils.setField(jwtService, "expirationTokenTime", "30");
    }

    @Test
    void testGenerateTokenSuccessful() throws Exception {
        // Given
        Role role = new Role();
        role.setName(ROLE);
        User user = User.builder()
                .email(EMAIL)
                .roles(Set.of(role))
                .build();
        AutoCloseable closeable = mockStatic(DateUtils.class);
        Date test = new Date(Long.MAX_VALUE);
        when(DateUtils.currentTimePlusSeconds(30L)).thenReturn(test);

        // When
        AccessToken accessToken = jwtService.generateAccessToken(user);

        // Then
        assertEquals(user, accessToken.getUser());
        assertEquals(test, accessToken.getExpirationDate());
        Jws<Claims> jwtClaims = getJWTClaims(accessToken);
        assertEquals(EMAIL, jwtClaims.getPayload().getSubject());
        assertEquals(test.toInstant().truncatedTo(ChronoUnit.SECONDS), jwtClaims.getPayload().getExpiration().toInstant());
        assertEquals("JWT", jwtClaims.getHeader().getType());
        assertEquals(List.of(ROLE), jwtClaims.getPayload().get("role"));
        assertEquals(List.of(Map.of("authority", "ROLE_" + ROLE)), jwtClaims.getPayload().get("authorities"));

        closeable.close();
    }

    private static Jws<Claims> getJWTClaims(AccessToken accessToken) {
        return Jwts.parser().verifyWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes())).build().parseSignedClaims(accessToken.getToken());
    }

    @Test
    void testExtractUsernameSuccessful() {
        // Given
        String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiUk9MRV9URVNUIiwiYXV0aG9yaXRpZXMiOlt7ImF1dGhvcml0eSI6IlJPTEVfVEVTVCJ9XSwic3ViIjoidGVzdEBlbWFpbC5jb20iLCJpYXQiOjE3MTAzMTkxODQsImV4cCI6OTIyMzM3MjAzNjg1NDc3NX0.Qr8K45nHWug0mTgez-N_KqSdsFmJcCIhlKEQdUPMrwI";

        // When
        String username = jwtService.extractUsername(token);

        // Then
        assertEquals(EMAIL, username);
    }

    @Test
    void testValidationTokenTimeSuccessful() {
        // When
        long result = jwtService.getValidationTokenTime();

        // Then
        assertEquals(30L * 1000, result);
    }
}
