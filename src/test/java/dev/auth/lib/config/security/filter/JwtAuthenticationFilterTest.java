package dev.auth.lib.config.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.auth.lib.data.model.Role;
import dev.auth.lib.data.model.User;
import dev.auth.lib.service.authentication.JwtService;
import dev.auth.lib.service.users.UserService;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    private JwtAuthenticationFilter authenticationFilter;

    @Mock
    private JwtService jwtService;
    @Mock
    private UserService userService;
    @Mock
    private ObjectMapper objectMapper;

    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain filterChain;

    @BeforeEach
    public void setUp() {
        this.request = Mockito.mock(HttpServletRequest.class);
        this.response = Mockito.mock(HttpServletResponse.class);
        this.filterChain = Mockito.mock(FilterChain.class);

        this.authenticationFilter = new JwtAuthenticationFilter(this.jwtService, this.userService, this.objectMapper);
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    void testAuthenticationWithoutToken() throws ServletException, IOException {
        testGenericAuthenticationWithoutToken("");
    }

    private void testGenericAuthenticationWithoutToken(String value) throws ServletException, IOException {
        // Given
        Mockito.when(this.request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(value);
        // When
        this.authenticationFilter.doFilterInternal(this.request, this.response, this.filterChain);
        // Then
        Mockito.verify(this.filterChain, Mockito.times(1)).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void testAuthenticationWithoutTokenBearer() throws ServletException, IOException {
        // Given
        testGenericAuthenticationWithoutToken("Basic aaaa");
    }

    @Test
    void testAuthenticationWithTokenBearerButWithoutToken() throws ServletException, IOException {
        // Given
        testGenericAuthenticationWithoutToken("Bearer ");
    }

    @Test
    void testAuthenticationExceptionInProcess() throws ServletException, IOException {
        // Given
        Mockito.when(this.request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer 1234");
        Mockito.when(this.jwtService.extractUsername("1234")).thenThrow(MalformedJwtException.class);

        PrintWriter writer = Mockito.mock(PrintWriter.class);
        Mockito.when(this.response.getWriter()).thenReturn(writer);

        Mockito.when(this.objectMapper.writeValueAsString(Mockito.any())).thenReturn("result mapper");

        // When
        this.authenticationFilter.doFilterInternal(this.request, this.response, this.filterChain);

        // Then
        Mockito.verify(this.response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        Mockito.verify(writer, Mockito.times(1)).write("result mapper");
        Mockito.verify(this.filterChain, Mockito.never()).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void testAuthenticationSuccessful() throws IOException, ServletException {
        // Given
        Mockito.when(this.request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer 1234");
        Mockito.when(this.jwtService.extractUsername("1234")).thenReturn("user");
        User user = User.builder()
                .email("user@test.com")
                .roles(Set.of(new Role("test", null)))
                .build();
        Mockito.when(this.userService.findByEmail("user")).thenReturn(Optional.of(user));
        // When
        this.authenticationFilter.doFilterInternal(this.request, this.response, this.filterChain);

        // Then
        Mockito.verify(this.filterChain, Mockito.times(1)).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
