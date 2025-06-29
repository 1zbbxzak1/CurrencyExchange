package ru.julia.currencyexchange.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import ru.julia.currencyexchange.application.exceptions.UserCreationException;
import ru.julia.currencyexchange.application.service.emails.EmailService;
import ru.julia.currencyexchange.application.service.emails.VerificationCodeGenerator;
import ru.julia.currencyexchange.domain.model.User;
import ru.julia.currencyexchange.infrastructure.repository.jpa.UserRepository;
import ru.julia.currencyexchange.utils.annotation.ActiveProfile;
import ru.julia.currencyexchange.utils.annotation.PostgresTestcontainers;
import ru.julia.currencyexchange.utils.configuration.DatabaseCleaner;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ActiveProfile
@PostgresTestcontainers
@Transactional
class AuthServiceIntegrationTest {
    @Autowired
    private AuthService authService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private VerificationCodeGenerator verificationCodeGenerator;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private DatabaseCleaner databaseCleaner;

    @MockitoBean
    private EmailService emailService;

    @BeforeEach
    void cleanDb() {
        databaseCleaner.resetDatabase();
    }

    @Test
    @DisplayName("Успешная регистрация пользователя")
    void createUserWithVerificationCode_success() {
        String email = "ituser@mail.com";
        String username = "ituser";
        Long chatId = 123456L;
        String password = "itpass";
        String code = verificationCodeGenerator.generateCode();
        Mockito.doNothing().when(emailService).sendVerificationCode(Mockito.anyString(), Mockito.anyString());

        authService.createUserWithVerificationCode(chatId, username, email, password);

        Optional<User> userOpt = userRepository.findByEmail(email);
        assertThat(userOpt).isPresent();
        User user = userOpt.get();
        assertThat(user.getUsername()).isEqualTo(username);
        assertThat(user.getChatId()).isEqualTo(chatId);
        assertThat(passwordEncoder.matches(password, user.getPassword())).isTrue();
        assertThat(user.getVerificationCode()).isNotBlank();
        assertThat(user.getRoles()).anySatisfy(ur -> assertThat(ur.getRole().getRoleName()).isEqualTo("ROLE_USER"));
        verify(emailService).sendVerificationCode(email, user.getVerificationCode());
    }

    @Test
    @DisplayName("Ошибка: дублирование email")
    void createUserWithVerificationCode_duplicateEmail() {
        String email = "ituser2@mail.com";
        String username = "ituser2";
        Long chatId = 123457L;
        String password = "itpass2";
        User user = new User(email, passwordEncoder.encode(password));
        user.setChatId(999999L);
        user.setUsername("someUniqueUsername");
        userRepository.save(user);
        assertThatThrownBy(() -> authService.createUserWithVerificationCode(chatId, username, email, password))
                .isInstanceOf(UserCreationException.class)
                .hasMessageContaining("email");
    }

    @Test
    @DisplayName("Ошибка: дублирование chatId")
    void createUserWithVerificationCode_duplicateChatId() {
        String email = "ituser3@mail.com";
        String username = "ituser3";
        Long chatId = 123458L;
        String password = "itpass3";
        User user = new User("other@mail.com", passwordEncoder.encode("otherpass"));
        user.setChatId(chatId);
        user.setUsername("otheruser");
        userRepository.save(user);
        assertThatThrownBy(() -> authService.createUserWithVerificationCode(chatId, username, email, password))
                .isInstanceOf(UserCreationException.class)
                .hasMessageContaining("chatId");
    }

    @Test
    @DisplayName("Ошибка: дублирование username")
    void createUserWithVerificationCode_duplicateUsername() {
        String email = "ituser4@mail.com";
        String username = "ituser4";
        Long chatId = 123459L;
        String password = "itpass4";
        User user = new User("other2@mail.com", passwordEncoder.encode("otherpass2"));
        user.setUsername(username);
        user.setChatId(999999L);
        userRepository.save(user);
        assertThatThrownBy(() -> authService.createUserWithVerificationCode(chatId, username, email, password))
                .isInstanceOf(UserCreationException.class)
                .hasMessageContaining("username");
    }
} 