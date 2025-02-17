package dev.auth.lib.data.repository;

import dev.auth.lib.data.model.ExchangeSessionCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ExchangeSessionCodeRepository extends JpaRepository<ExchangeSessionCode, Long> {
    Optional<ExchangeSessionCode> findByCode(String code);
}
