package dev.auth.lib.service.authentication.impl;

import dev.auth.lib.data.model.ExchangeSessionCode;
import dev.auth.lib.data.model.User;
import dev.auth.lib.data.repository.ExchangeSessionCodeRepository;
import dev.auth.lib.exception.EntityNotFoundException;
import dev.auth.lib.exception.ExchangeCodeExpiredException;
import dev.auth.lib.exception.ExchangeCodeGenerationException;
import dev.auth.lib.exception.InvalidExchangeCodeException;
import dev.auth.lib.service.authentication.ExchangeCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExchangeCodeServiceImpl implements ExchangeCodeService {

    private static final String EXCHANGE_CODE_CREATION_ERROR_MESSAGE = "Error while creating exchange code for user %s. Please try again.";
    private static final String EXCHANGE_CODE_NOT_FOUND_ERROR_MESSAGE = "Exchange code with code %s not found.";
    private static final String EXCHANGE_CODE_EXPIRED_ERROR_MESSAGE = "Exchange code with code %s has expired.";
    private static final Long EXCHANGE_CODE_EXPIRATION_TIME = 120L;

    private final ExchangeSessionCodeRepository exchangeSessionCodeRepository;

    @Override
    public ExchangeSessionCode create(User user) {
        try{
            ExchangeSessionCode exchangeSessionCode = ExchangeSessionCode.builder()
                    .code(UUID.randomUUID().toString())
                    .expirationDate(Instant.now().plusSeconds(EXCHANGE_CODE_EXPIRATION_TIME))
                    .user(user)
                    .build();
            return exchangeSessionCodeRepository.save(exchangeSessionCode);
        }catch (DataIntegrityViolationException e){
            log.error("Error while creating exchange code for user: {}", user.getEmail(), e);
            throw new ExchangeCodeGenerationException(String.format(EXCHANGE_CODE_CREATION_ERROR_MESSAGE, user.getEmail()));
        }
    }

    @Override
    public ExchangeSessionCode validate(String code) {
        try{
            ExchangeSessionCode exchangeSessionCode = get(code);
            checkExpirationDate(exchangeSessionCode);
            return exchangeSessionCode;
        }catch (EntityNotFoundException e){
            throw new InvalidExchangeCodeException(e.getMessage());
        }
    }

    @Override
    public void delete(ExchangeSessionCode exchangeSessionCode){
        exchangeSessionCodeRepository.delete(exchangeSessionCode);
    }

    private ExchangeSessionCode get(String code){
        return exchangeSessionCodeRepository.findByCode(code)
                .orElseThrow(() -> new EntityNotFoundException(String.format(EXCHANGE_CODE_NOT_FOUND_ERROR_MESSAGE, code)));
    }

    private void checkExpirationDate(ExchangeSessionCode exchangeSessionCode){
        if(exchangeSessionCode.getExpirationDate().isBefore(Instant.now())){
            throw new ExchangeCodeExpiredException(String.format(EXCHANGE_CODE_EXPIRED_ERROR_MESSAGE, exchangeSessionCode.getCode()));
        }
    }
}
