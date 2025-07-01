package ru.julia.currencyexchange.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.julia.currencyexchange.application.exceptions.UserCreationException;
import ru.julia.currencyexchange.application.service.emails.EmailService;
import ru.julia.currencyexchange.application.service.emails.VerificationCodeGenerator;
import ru.julia.currencyexchange.domain.model.Role;
import ru.julia.currencyexchange.domain.model.User;
import ru.julia.currencyexchange.infrastructure.repository.jpa.RoleRepository;
import ru.julia.currencyexchange.infrastructure.repository.jpa.UserRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

class AuthServiceUnitTest {
    @Mock
    private EmailService emailService;
    @Mock
    private VerificationCodeGenerator verificationCodeGenerator;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        openMocks(this);
    }

    @Test
    @DisplayName("Успешное создание пользователя с кодом верификации")
    void createUserWithVerificationCode_success() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.existsByChatId(anyLong())).thenReturn(false);
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(verificationCodeGenerator.generateCode()).thenReturn("CODE123");
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPass");
        when(roleRepository.findByRoleName(anyString())).thenReturn(Optional.of(new Role("ROLE_USER")));

        authService.createUserWithVerificationCode(1L, "user", "user@mail.com", "pass");

        ArgumentCaptor<User> userCaptor = forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User saved = userCaptor.getValue();
        assertThat(saved.getEmail()).isEqualTo("user@mail.com");
        assertThat(saved.getUsername()).isEqualTo("user");
        assertThat(saved.getChatId()).isEqualTo(1L);
        assertThat(saved.getPassword()).isEqualTo("encodedPass");
        assertThat(saved.getVerificationCode()).isEqualTo("CODE123");
        assertThat(saved.getRoles()).anySatisfy(ur -> assertThat(ur.getRole().getRoleName()).isEqualTo("ROLE_USER"));

        verify(emailService).sendVerificationCode("user@mail.com", "CODE123");
    }

    @Test
    @DisplayName("Дублирующий email вызывает исключение")
    void createUserWithVerificationCode_duplicateEmail() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> authService.createUserWithVerificationCode(1L, "user", "user@mail.com", "pass"))
                .isInstanceOf(UserCreationException.class)
                .hasMessageContaining("email");
    }

    @Test
    @DisplayName("Дублирующий chatId вызывает исключение")
    void createUserWithVerificationCode_duplicateChatId() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.existsByChatId(anyLong())).thenReturn(true);

        assertThatThrownBy(() -> authService.createUserWithVerificationCode(1L, "user", "user@mail.com", "pass"))
                .isInstanceOf(UserCreationException.class)
                .hasMessageContaining("chatId");
    }

    @Test
    @DisplayName("Дублирующий username вызывает исключение")
    void createUserWithVerificationCode_duplicateUsername() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.existsByChatId(anyLong())).thenReturn(false);
        when(userRepository.existsByUsername(anyString())).thenReturn(true);
        
        assertThatThrownBy(() -> authService.createUserWithVerificationCode(1L, "user", "user@mail.com", "pass"))
                .isInstanceOf(UserCreationException.class)
                .hasMessageContaining("username");
    }
} 