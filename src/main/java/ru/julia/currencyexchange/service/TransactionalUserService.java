package ru.julia.currencyexchange.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import ru.julia.currencyexchange.entity.*;
import ru.julia.currencyexchange.entity.enums.RoleEnum;
import ru.julia.currencyexchange.repository.*;

import java.util.List;

@Service
public class TransactionalUserService {
    private final UserRepository userRepository;
    private final SettingsRepository settingsRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final CurrencyRepository currencyRepository;

    public TransactionalUserService(UserRepository userRepository,
                                    SettingsRepository settingsRepository,
                                    RoleRepository roleRepository,
                                    UserRoleRepository userRoleRepository,
                                    CurrencyRepository currencyRepository) {
        this.userRepository = userRepository;
        this.settingsRepository = settingsRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.currencyRepository = currencyRepository;
    }

    @Transactional(rollbackOn = Exception.class)
    public User createUserWithSettings(String username, String password, String preferredCurrencyCode) {
        User user = new User(username, password);
        userRepository.save(user);

        Role role = roleRepository.findByRoleName(RoleEnum.USER)
                .orElseGet(() -> roleRepository.save(new Role(RoleEnum.USER)));

        userRoleRepository.save(new UserRole(user, role));

        Currency preferredCurrency = currencyRepository.findByCode(preferredCurrencyCode)
                .orElseThrow(() -> new IllegalArgumentException("Currency " + preferredCurrencyCode + " not found"));

        settingsRepository.save(new Settings(user, preferredCurrency));

        return userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("User not found after creation"));
    }

    @Transactional
    public User findUserById(String id) {
        return userRepository.findById(id).orElse(null);
    }

    @Transactional
    public User deleteUserById(String userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            userRepository.delete(user);
        }
        return user;
    }

    @Transactional
    public List<User> findAllUsers() {
        return (List<User>) userRepository.findAll();
    }
}
