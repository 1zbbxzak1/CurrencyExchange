package ru.julia.currencyexchange.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import ru.julia.currencyexchange.entity.Role;
import ru.julia.currencyexchange.entity.Settings;
import ru.julia.currencyexchange.entity.User;
import ru.julia.currencyexchange.entity.enums.RoleEnum;
import ru.julia.currencyexchange.repository.RoleRepository;
import ru.julia.currencyexchange.repository.SettingsRepository;
import ru.julia.currencyexchange.repository.UserRepository;

@Service
public class TransactionalUserService {
    private final UserRepository userRepository;
    private final SettingsRepository settingsRepository;
    private final RoleRepository roleRepository;

    public TransactionalUserService(UserRepository userRepository, SettingsRepository settingsRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.settingsRepository = settingsRepository;
        this.roleRepository = roleRepository;
    }

    @Transactional(rollbackOn = Exception.class)
    public User createUserWithSettings(String username, String password, String preferredCurrency) {
        try {
            Role role = roleRepository.findByRole(RoleEnum.USER)
                    .orElseGet(() -> roleRepository.save(new Role(RoleEnum.USER)));

            User user = new User(username, password, role);
            userRepository.save(user);

            Settings settings = new Settings(user, preferredCurrency.toUpperCase());
            settingsRepository.save(settings);

            return user;
        } catch (Exception e) {
            throw new RuntimeException("Error creating user with settings" + e.getMessage());
        }
    }

    @Transactional
    public User findUserById(String id) {
        return userRepository.findById(id).orElse(null);
    }

    @Transactional
    public User deleteUser(String userId) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user != null) {
                userRepository.delete(user);
            }

            return user;
        } catch (Exception e) {
            throw new RuntimeException("Error deleting user with id" + userId);
        }
    }
}
