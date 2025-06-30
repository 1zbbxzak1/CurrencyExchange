package ru.julia.currencyexchange.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.julia.currencyexchange.application.exceptions.UserNotFoundException;
import ru.julia.currencyexchange.domain.model.User;
import ru.julia.currencyexchange.infrastructure.repository.jpa.UserRepository;
import ru.julia.currencyexchange.utils.annotation.ActiveProfile;
import ru.julia.currencyexchange.utils.annotation.PostgresTestcontainers;
import ru.julia.currencyexchange.utils.configuration.DatabaseCleaner;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfile
@PostgresTestcontainers
@Transactional
class UserServiceIntegrationTest {
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private DatabaseCleaner databaseCleaner;

    @BeforeEach
    void cleanDb() {
        databaseCleaner.resetDatabase();
    }

    @Test
    @DisplayName("Сохранение и поиск пользователя по chatId")
    void saveAndFindUserByChatId() {
        User user = new User("ituser@mail.com", "pass");
        user.setChatId(100L);
        user.setUsername("ituser");
        userRepository.save(user);

        User found = userService.findUserByChatId(100L);

        assertThat(found.getEmail()).isEqualTo("ituser@mail.com");
        assertThat(found.getUsername()).isEqualTo("ituser");
    }

    @Test
    @DisplayName("Удаление пользователя по id (soft delete)")
    void deleteUserById_softDelete() {
        User user = new User("deluser@mail.com", "pass");
        user.setChatId(200L);
        user.setUsername("deluser");
        user = userRepository.save(user);
        User deleted = userService.deleteUserById(user.getId());

        assertThat(deleted.isDeleted()).isTrue();
        assertThat(deleted.isVerified()).isFalse();

        Optional<User> fromDb = userRepository.findById(user.getId());

        assertThat(fromDb).isPresent();
        assertThat(fromDb.get().isDeleted()).isTrue();
    }

    @Test
    @DisplayName("Мягкое удаление пользователя по chatId")
    void softDeleteUserByChatId() {
        User user = new User("softdel@mail.com", "pass");
        user.setChatId(300L);
        user.setUsername("softdel");
        userRepository.save(user);
        userService.softDeleteUserByChatId(300L);
        User fromDb = userRepository.findByChatId(300L).orElseThrow();

        assertThat(fromDb.isDeleted()).isTrue();
        assertThat(fromDb.isVerified()).isFalse();
    }

    @Test
    @DisplayName("Верификация пользователя по коду")
    void verifyUserCode_success() {
        User user = new User("verif@mail.com", "pass");
        user.setChatId(400L);
        user.setUsername("verifuser");
        user.setVerificationCode("1234");
        userRepository.save(user);
        boolean result = userService.verifyUserCode(400L, "verif@mail.com", "1234");

        assertThat(result).isTrue();

        User fromDb = userRepository.findByEmail("verif@mail.com").orElseThrow();
        assertThat(fromDb.isVerified()).isTrue();
    }

    @Test
    @DisplayName("Обновление имени пользователя по chatId")
    void updateUsernameIfChanged() {
        User user = new User("upd@mail.com", "pass");
        user.setChatId(500L);
        user.setUsername("oldname");
        userRepository.save(user);
        userService.updateUsernameIfChanged(500L, "newname");
        User fromDb = userRepository.findByChatId(500L).orElseThrow();

        assertThat(fromDb.getUsername()).isEqualTo("newname");
    }

    @Test
    @DisplayName("Получение всех пользователей")
    void findAllUsers() {
        User user1 = new User("a@mail.com", "p1");
        user1.setChatId(600L);
        user1.setUsername("a");

        User user2 = new User("b@mail.com", "p2");
        user2.setChatId(601L);
        user2.setUsername("b");

        userRepository.save(user1);
        userRepository.save(user2);

        List<User> users = userService.findAllUsers(null);
        
        assertThat(users).extracting(User::getEmail).contains("a@mail.com", "b@mail.com");
    }

    @Test
    @DisplayName("Пользователь не найден по chatId")
    void findUserByChatId_notFound() {
        assertThatThrownBy(() -> userService.findUserByChatId(999L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("999");
    }
} 