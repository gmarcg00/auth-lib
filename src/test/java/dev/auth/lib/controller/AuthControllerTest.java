package dev.auth.lib.controller;

import dev.auth.lib.controller.mappers.UsersMapper;
import dev.auth.lib.controller.model.SignUpRequest;
import dev.auth.lib.data.model.User;
import dev.auth.lib.service.authentication.AuthService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private static final String EMAIL = "email";
    private static final String PASSWORD = "password";


    private AuthController authController;

    @Mock
    private AuthService authService;

    private AutoCloseable closeable;

    @BeforeEach
    public void setUp(){
        this.authController = new AuthController(authService);
        closeable = mockStatic(UsersMapper.class);
    }

    @AfterEach
    public void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void testSignUp() {
        // Given
        SignUpRequest request = new SignUpRequest(EMAIL, PASSWORD);
        User user = mock(User.class);
        when(UsersMapper.requestToEntity(request)).thenReturn(user);

        // When
        ResponseEntity<Void> responseEntity = authController.signUp(request);

        // Then
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
    }
}
