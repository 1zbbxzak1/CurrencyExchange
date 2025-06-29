package ru.julia.currencyexchange.infrastructure.repository.jpa;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import ru.julia.currencyexchange.domain.model.User;

import java.util.Optional;

public interface UserRepository extends CrudRepository<User, String> {
    Boolean existsByUsername(String username);

    Boolean existsByChatId(Long chatId);

    Optional<User> findByEmail(String email);

    Optional<User> findByChatId(Long chatId);

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE u.chatId = :chatId AND u.isDeleted = false")
    Boolean existsActiveByChatId(Long chatId);
}
