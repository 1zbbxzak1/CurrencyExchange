package ru.julia.currencyexchange.infrastructure.repository.jpa;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.julia.currencyexchange.domain.model.User;
import ru.julia.currencyexchange.utils.annotation.ActiveProfile;
import ru.julia.currencyexchange.utils.annotation.PostgresTestcontainers;
import ru.julia.currencyexchange.utils.configuration.DatabaseCleaner;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfile
@PostgresTestcontainers
class UserRepositoryIntegrationTest {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @BeforeEach
    void cleanDb() {
        databaseCleaner.resetDatabase();
    }

    @Test
    @DisplayName("Сохранение и поиск по id")
    void saveAndFindById() {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setChatId(12345L);
        user.setPassword("testpass");
        userRepository.save(user);

        Optional<User> found = userRepository.findById(user.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("testuser");
    }

    @Test
    @DisplayName("Поиск по email")
    void findByEmail() {
        User user = new User();
        user.setUsername("anotheruser");
        user.setEmail("another@example.com");
        user.setChatId(54321L);
        user.setPassword("testpass");
        userRepository.save(user);

        Optional<User> found = userRepository.findByEmail("another@example.com");

        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("anotheruser");
    }

    @Test
    @DisplayName("Проверка existsByUsername")
    void existsByUsername() {
        User user = new User();
        user.setUsername("uniqueuser");
        user.setEmail("unique@example.com");
        user.setChatId(11111L);
        user.setPassword("testpass");
        userRepository.save(user);

        Boolean exists = userRepository.existsByUsername("uniqueuser");

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Проверка existsByChatId")
    void existsByChatId() {
        User user = new User();
        user.setUsername("chatuser");
        user.setEmail("chat@example.com");
        user.setChatId(22222L);
        user.setPassword("testpass");
        userRepository.save(user);

        Boolean exists = userRepository.existsByChatId(22222L);

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Поиск несуществующего пользователя возвращает empty")
    void findByIdNotFound() {
        Optional<User> found = userRepository.findById("nonexistent-id");
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Удаление пользователя")
    void deleteUser() {
        User user = new User();
        user.setUsername("todelete");
        user.setEmail("todelete@example.com");
        user.setChatId(99999L);
        user.setPassword("pass");
        userRepository.save(user);
        userRepository.deleteById(user.getId());

        assertThat(userRepository.findById(user.getId())).isEmpty();
    }

    @Test
    @DisplayName("Обновление пользователя")
    void updateUser() {
        User user = new User();
        user.setUsername("toupdate");
        user.setEmail("toupdate@example.com");
        user.setChatId(88888L);
        user.setPassword("pass");
        userRepository.save(user);
        user.setEmail("updated@example.com");
        userRepository.save(user);

        Optional<User> found = userRepository.findById(user.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("updated@example.com");
    }

    @Test
    @DisplayName("findAll возвращает всех пользователей")
    void findAllReturnsAllUsers() {
        User user1 = new User();
        user1.setUsername("user1");
        user1.setEmail("user1@example.com");
        user1.setChatId(10001L);
        user1.setPassword("pass1");

        User user2 = new User();
        user2.setUsername("user2");
        user2.setEmail("user2@example.com");
        user2.setChatId(10002L);
        user2.setPassword("pass2");

        userRepository.save(user1);
        userRepository.save(user2);

        Iterable<User> users = userRepository.findAll();

        assertThat(users).extracting(User::getUsername).contains("user1", "user2");
    }
}