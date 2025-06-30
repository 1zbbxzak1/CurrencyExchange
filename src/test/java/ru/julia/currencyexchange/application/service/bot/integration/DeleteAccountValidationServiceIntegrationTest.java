package ru.julia.currencyexchange.application.service.bot.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.julia.currencyexchange.application.bot.settings.ValidationResult;
import ru.julia.currencyexchange.application.service.bot.DeleteAccountValidationService;
import ru.julia.currencyexchange.domain.model.User;
import ru.julia.currencyexchange.infrastructure.repository.jpa.UserRepository;
import ru.julia.currencyexchange.utils.annotation.ActiveProfile;
import ru.julia.currencyexchange.utils.annotation.PostgresTestcontainers;
import ru.julia.currencyexchange.utils.configuration.DatabaseCleaner;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfile
@PostgresTestcontainers
@Transactional
class DeleteAccountValidationServiceIntegrationTest {
    @Autowired
    private DeleteAccountValidationService service;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private DatabaseCleaner databaseCleaner;

    @BeforeEach
    void cleanDb() {
        databaseCleaner.resetDatabase();
    }

    @Test
    @DisplayName("Пользователь не найден (not_registered)")
    void userNotFound() {
        ValidationResult result = service.validateUserForDeletion(100L);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrorMessage()).startsWith("❌ Вы не зарегистрированы");
        assertThat(result.getUser()).isNull();
    }

    @Test
    @DisplayName("Пользователь найден, не забанен, не удалён (успех)")
    void userFoundSuccess() {
        User user = new User("test@mail.com", "pass");
        user.setChatId(101L);
        user.setUsername("testuser");
        user.setBanned(false);
        user.setDeleted(false);
        userRepository.save(user);

        ValidationResult result = service.validateUserForDeletion(101L);

        assertThat(result.isValid()).isTrue();
        assertThat(result.getUser()).isNotNull();
        assertThat(result.getUser().getChatId()).isEqualTo(101L);
        assertThat(result.getErrorMessage()).isNull();
    }

    @Test
    @DisplayName("Пользователь забанен (banned)")
    void userBanned() {
        User user = new User("banned@mail.com", "pass");
        user.setChatId(102L);
        user.setUsername("banneduser");
        user.setBanned(true);
        user.setDeleted(false);
        userRepository.save(user);

        ValidationResult result = service.validateUserForDeletion(102L);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrorMessage()).startsWith("❌ Ваш аккаунт заблокирован");
        assertThat(result.getUser()).isNull();
    }

    @Test
    @DisplayName("Пользователь удалён (already_deleted)")
    void userDeleted() {
        User user = new User("deleted@mail.com", "pass");
        user.setChatId(103L);
        user.setUsername("deleteduser");
        user.setBanned(false);
        user.setDeleted(true);
        userRepository.save(user);

        ValidationResult result = service.validateUserForDeletion(103L);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrorMessage()).startsWith("❌ Ваш аккаунт уже помечен как удаленный");
        assertThat(result.getUser()).isNull();
    }

    @Test
    @DisplayName("UserService кидает исключение (error)")
    void userServiceThrows() {
        ValidationResult result = service.validateUserForDeletion(null);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrorMessage()).startsWith("⚠️ Произошла ошибка при удалении аккаунта");
        assertThat(result.getUser()).isNull();
    }
} 