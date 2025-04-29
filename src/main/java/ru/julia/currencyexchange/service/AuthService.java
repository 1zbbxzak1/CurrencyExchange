package ru.julia.currencyexchange.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.julia.currencyexchange.entity.*;
import ru.julia.currencyexchange.entity.enums.RoleEnum;
import ru.julia.currencyexchange.repository.CurrencyRepository;
import ru.julia.currencyexchange.repository.RoleRepository;
import ru.julia.currencyexchange.repository.UserRepository;
import ru.julia.currencyexchange.service.exceptions.CurrencyNotFoundException;
import ru.julia.currencyexchange.service.exceptions.UserCreationException;

@Service
@Transactional(readOnly = true)
public class AuthService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CurrencyRepository currencyRepository;

    public AuthService(UserRepository userRepository, RoleRepository roleRepository, CurrencyRepository currencyRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.currencyRepository = currencyRepository;
    }

    @Transactional(rollbackFor = UserCreationException.class)
    public User createUserWithSettings(String username, String password, String preferredCurrencyCode) {
        User user = new User(username, password);

        Currency preferredCurrency = currencyRepository.findByCode(preferredCurrencyCode)
                .orElseThrow(() -> new CurrencyNotFoundException("Currency " + preferredCurrencyCode + " not found"));

        Settings settings = new Settings(user, preferredCurrency);
        user.setSettings(settings);

        Role userRole = roleRepository.findByRoleName(RoleEnum.USER)
                .orElseGet(() -> roleRepository.save(new Role(RoleEnum.USER)));

        user.getRoles().add(new UserRole(user, userRole));

        userRepository.save(user);

        return user;
    }
}
