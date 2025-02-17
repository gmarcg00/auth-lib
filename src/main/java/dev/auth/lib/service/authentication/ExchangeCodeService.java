package dev.auth.lib.service.authentication;

import dev.auth.lib.data.model.ExchangeSessionCode;
import dev.auth.lib.data.model.User;

public interface ExchangeCodeService {
    ExchangeSessionCode create(User user);
    ExchangeSessionCode validate(String code);
    void delete(ExchangeSessionCode exchangeSessionCode);
}
