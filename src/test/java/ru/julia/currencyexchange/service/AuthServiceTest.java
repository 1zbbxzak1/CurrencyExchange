package ru.julia.currencyexchange.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.julia.currencyexchange.application.exceptions.CurrencyNotFoundException;
import ru.julia.currencyexchange.application.exceptions.UserCreationException;
import ru.julia.currencyexchange.application.service.AuthService;
import ru.julia.currencyexchange.domain.enums.RoleEnum;
import ru.julia.currencyexchange.domain.model.Currency;
import ru.julia.currencyexchange.domain.model.Role;
import ru.julia.currencyexchange.domain.model.User;
import ru.julia.currencyexchange.infrastructure.repository.jpa.CurrencyRepository;
import ru.julia.currencyexchange.infrastructure.repository.jpa.RoleRepository;
import ru.julia.currencyexchange.infrastructure.repository.jpa.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private CurrencyRepository currencyRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createUserWithSettingsSuccess() {
        String username = "testUser";
        String rawPassword = "password";
        String encodedPassword = "encodedPassword";
        String currencyCode = "USD";

        Currency currency = new Currency();
        Role role = new Role("ROLE_" + RoleEnum.USER);
        User savedUser = new User(username, encodedPassword);

        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
        when(currencyRepository.findByCode(currencyCode)).thenReturn(Optional.of(currency));
        when(roleRepository.findByRoleName("ROLE_" + RoleEnum.USER)).thenReturn(Optional.of(role));
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        User result = authService.createUserWithSettings(username, rawPassword, currencyCode);

        assertNotNull(result);
        assertEquals(username, result.getUsername());
        assertEquals(encodedPassword, result.getPassword());
        assertEquals(currency, result.getSettings().getPreferredCurrency());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void currencyNotFoundThrowsException() {
        String username = "testUser";
        String rawPassword = "password";
        String currencyCode = "INVALID";

        when(passwordEncoder.encode(rawPassword)).thenReturn("encodedPassword");
        when(currencyRepository.findByCode(currencyCode)).thenReturn(Optional.empty());

        assertThrows(CurrencyNotFoundException.class, () ->
                authService.createUserWithSettings(username, rawPassword, currencyCode));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void roleNotFoundSavesNewRole() {
        String username = "testUser";
        String rawPassword = "password";
        String encodedPassword = "encodedPassword";
        String currencyCode = "EUR";

        Currency currency = new Currency();
        Role newRole = new Role("ROLE_" + RoleEnum.USER);
        User user = new User(username, encodedPassword);

        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
        when(currencyRepository.findByCode(currencyCode)).thenReturn(Optional.of(currency));
        when(roleRepository.findByRoleName("ROLE_" + RoleEnum.USER)).thenReturn(Optional.empty());
        when(roleRepository.save(any(Role.class))).thenReturn(newRole);
        when(userRepository.save(any(User.class))).thenReturn(user);

        User result = authService.createUserWithSettings(username, rawPassword, currencyCode);

        assertNotNull(result);
        assertEquals(username, result.getUsername());
        verify(roleRepository).save(any(Role.class));
    }

    @Test
    void passwordEncodingCalled() {
        String username = "testUser";
        String rawPassword = "password";
        String currencyCode = "USD";

        Currency currency = new Currency();
        Role role = new Role("ROLE_" + RoleEnum.USER);

        when(passwordEncoder.encode(rawPassword)).thenReturn("encodedPassword");
        when(currencyRepository.findByCode(currencyCode)).thenReturn(Optional.of(currency));
        when(roleRepository.findByRoleName(anyString())).thenReturn(Optional.of(role));
        when(userRepository.save(any(User.class))).thenReturn(new User(username, "encodedPassword"));

        authService.createUserWithSettings(username, rawPassword, currencyCode);

        verify(passwordEncoder).encode(rawPassword);
    }

    @Test
    void duplicateUsernameThrowsUserCreationException() {
        String username = "existingUser";
        String password = "password";
        String currencyCode = "USD";

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(new User()));

        UserCreationException exception = assertThrows(UserCreationException.class, () ->
                authService.createUserWithSettings(username, password, currencyCode));

        assertEquals("User with username existingUser already exists", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }
}
