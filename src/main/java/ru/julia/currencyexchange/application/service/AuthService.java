package ru.julia.currencyexchange.application.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.julia.currencyexchange.application.exceptions.CurrencyNotFoundException;
import ru.julia.currencyexchange.application.exceptions.UserCreationException;
import ru.julia.currencyexchange.domain.enums.RoleEnum;
import ru.julia.currencyexchange.domain.model.*;
import ru.julia.currencyexchange.infrastructure.repository.jpa.CurrencyRepository;
import ru.julia.currencyexchange.infrastructure.repository.jpa.RoleRepository;
import ru.julia.currencyexchange.infrastructure.repository.jpa.UserRepository;

@Service
@Transactional(readOnly = true)
public class AuthService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CurrencyRepository currencyRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       CurrencyRepository currencyRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.currencyRepository = currencyRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(rollbackFor = UserCreationException.class)
    public User createUserWithSettings(String username, String password, String preferredCurrencyCode) {
        String encodedPassword = passwordEncoder.encode(password);
        User user = new User(username, encodedPassword);

        Currency preferredCurrency = currencyRepository.findByCode(preferredCurrencyCode)
                .orElseThrow(() -> new CurrencyNotFoundException("Currency " + preferredCurrencyCode + " not found"));

        Settings settings = new Settings(user, preferredCurrency);
        user.setSettings(settings);

        Role userRole = roleRepository.findByRoleName("ROLE_" + RoleEnum.USER)
                .orElseGet(() -> roleRepository.save(new Role("ROLE_" + RoleEnum.USER.name())));

        user.getRoles().add(new UserRole(user, userRole));

        userRepository.save(user);

        return user;
    }
}
