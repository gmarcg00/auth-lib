package dev.auth.lib.data.repository;


import dev.auth.lib.data.model.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserStatusRepository extends JpaRepository<UserStatus, Long> {
    UserStatus findByName(String name);
}
