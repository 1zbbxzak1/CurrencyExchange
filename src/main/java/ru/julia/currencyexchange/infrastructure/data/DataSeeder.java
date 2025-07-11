package ru.julia.currencyexchange.infrastructure.data;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import ru.julia.currencyexchange.domain.enums.RoleEnum;
import ru.julia.currencyexchange.domain.model.Role;
import ru.julia.currencyexchange.domain.model.User;
import ru.julia.currencyexchange.domain.model.UserRole;
import ru.julia.currencyexchange.infrastructure.repository.jpa.RoleRepository;
import ru.julia.currencyexchange.infrastructure.repository.jpa.UserRepository;

@Profile("!test")
@Component
public class DataSeeder implements CommandLineRunner {
    @Value("${admin.chat-id}")
    private Long adminChatId;
    @Value("${admin.username}")
    private String adminUsername;
    @Value("${admin.email}")
    private String adminEmail;
    @Value("${admin.password}")
    private String adminPassword;

    @Value("${user.chat-id}")
    private Long userChatId;
    @Value("${user.username}")
    private String userUsername;
    @Value("${user.email}")
    private String userEmail;
    @Value("${user.password}")
    private String userPassword;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (!userRepository.existsByUsername(adminUsername)) {
            Role adminRole = roleRepository.findByRoleName("ROLE_" + RoleEnum.ADMIN)
                    .orElseGet(() -> roleRepository.save(new Role("ROLE_" + RoleEnum.ADMIN)));

            createUsers(adminRole, adminEmail, adminPassword, adminChatId, adminUsername);
        }

//        if (!userRepository.existsByUsername(userUsername)) {
//            Role userRole = roleRepository.findByRoleName("ROLE_" + RoleEnum.USER)
//                    .orElseGet(() -> roleRepository.save(new Role("ROLE_" + RoleEnum.USER)));
//
//            createUsers(userRole, userEmail, userPassword, userChatId, userUsername);
//        }
    }

    private void createUsers(Role role, String email, String password, Long chatId, String username) {
        User user = new User(email, passwordEncoder.encode(password));
        user.setChatId(chatId);
        user.setUsername(username);
        user.setVerified(true);
        user.getRoles().add(new UserRole(user, role));

        userRepository.save(user);
    }
}

