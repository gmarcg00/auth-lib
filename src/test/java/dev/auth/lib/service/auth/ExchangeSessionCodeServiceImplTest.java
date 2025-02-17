package dev.auth.lib.service.auth;

import dev.auth.lib.data.model.ExchangeSessionCode;
import dev.auth.lib.data.model.User;
import dev.auth.lib.data.repository.ExchangeSessionCodeRepository;
import dev.auth.lib.exception.ExchangeCodeExpiredException;
import dev.auth.lib.exception.ExchangeCodeGenerationException;
import dev.auth.lib.exception.InvalidExchangeCodeException;
import dev.auth.lib.service.authentication.ExchangeCodeService;
import dev.auth.lib.service.authentication.impl.ExchangeCodeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExchangeSessionCodeServiceImplTest {

    private static final String USER_MAIL = "test@mail.com";

    private ExchangeCodeService exchangeCodeService;

    @Mock
    private ExchangeSessionCodeRepository exchangeSessionCodeRepository;

    @BeforeEach
    public void setUp(){
        exchangeCodeService = new ExchangeCodeServiceImpl(exchangeSessionCodeRepository);
    }

    @Test
    void testCreateExchangeWithInvalidCode(){
        //Given
        User user = User.builder()
                .email(USER_MAIL)
                .build();
        when(exchangeSessionCodeRepository.save(any())).thenThrow(DataIntegrityViolationException.class);

        //When & Then
        Exception exception = assertThrows(ExchangeCodeGenerationException.class, () -> exchangeCodeService.create(user));
        assertEquals("Error while creating exchange code for user test@mail.com. Please try again.",exception.getMessage());
    }

    @Test
    void testCreateExchangeCodeSuccessfully(){
        //Given
        User user = User.builder()
                .email(USER_MAIL)
                .build();
        ExchangeSessionCode code = ExchangeSessionCode.builder()
                .code("test")
                .user(user)
                .build();
        when(exchangeSessionCodeRepository.save(any())).thenReturn(code);

        //When
        ExchangeSessionCode result = exchangeCodeService.create(user);

        //Then
        assertEquals(result,code);
    }

    @Test
    void testValidateCodeNotFound(){
        //Given
        when(exchangeSessionCodeRepository.findByCode("test")).thenReturn(Optional.empty());

        //When & Then
        Exception exception = assertThrows(InvalidExchangeCodeException.class, () -> exchangeCodeService.validate("test"));
        assertEquals("Exchange code with code test not found.",exception.getMessage());
    }

    @Test
    void testValidateCodeExpired(){
        //Given
        ExchangeSessionCode code = ExchangeSessionCode.builder()
                .code("test")
                .expirationDate(Instant.now().minusSeconds(1))
                .build();
        when(exchangeSessionCodeRepository.findByCode("test")).thenReturn(Optional.of(code));

        //When & Then
        Exception exception = assertThrows(ExchangeCodeExpiredException.class, () -> exchangeCodeService.validate("test"));
        assertEquals("Exchange code with code test has expired.",exception.getMessage());
    }

    @Test
    void testValidateCodeSuccessfully(){
        //Given
        ExchangeSessionCode code = ExchangeSessionCode.builder()
                .code("test")
                .expirationDate(Instant.now().plusSeconds(1))
                .build();
        when(exchangeSessionCodeRepository.findByCode("test")).thenReturn(Optional.of(code));

        //When
        ExchangeSessionCode result = exchangeCodeService.validate("test");

        //Then
        assertEquals(result,code);
    }

    @Test
    void testDeleteExchangeCodeSuccessfully(){
        //Given
        ExchangeSessionCode code = ExchangeSessionCode.builder()
                .code("test")
                .expirationDate(Instant.now().plusSeconds(1))
                .build();

        //When
        exchangeCodeService.delete(code);

        //Then
        verify(exchangeSessionCodeRepository,times(1)).delete(code);
    }

}
