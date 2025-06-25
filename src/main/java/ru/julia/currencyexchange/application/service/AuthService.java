package ru.julia.currencyexchange.application.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.julia.currencyexchange.application.exceptions.UserCreationException;
import ru.julia.currencyexchange.application.service.emails.EmailService;
import ru.julia.currencyexchange.application.service.emails.VerificationCodeGenerator;
import ru.julia.currencyexchange.domain.enums.RoleEnum;
import ru.julia.currencyexchange.domain.model.Role;
import ru.julia.currencyexchange.domain.model.User;
import ru.julia.currencyexchange.domain.model.UserRole;
import ru.julia.currencyexchange.infrastructure.repository.jpa.RoleRepository;
import ru.julia.currencyexchange.infrastructure.repository.jpa.UserRepository;

@Service
@Transactional(readOnly = true)
public class AuthService {

    private final EmailService emailService;
    private final VerificationCodeGenerator verificationCodeGenerator;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(EmailService emailService,
                       VerificationCodeGenerator verificationCodeGenerator,
                       UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder) {
        this.emailService = emailService;
        this.verificationCodeGenerator = verificationCodeGenerator;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(rollbackFor = UserCreationException.class)
    public void createUserWithVerificationCode(String email, String password) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new UserCreationException("User with email " + email + " already exists");
        }

        String verificationCode = verificationCodeGenerator.generateCode();

        String encodedPassword = passwordEncoder.encode(password);
        User user = new User(email, encodedPassword);

//        Currency preferredCurrency = currencyRepository.findByCode(preferredCurrencyCode)
//                .orElseThrow(() -> new CurrencyNotFoundException("Currency " + preferredCurrencyCode + " not found"));
//
//        Settings settings = new Settings(user, preferredCurrency);
//        user.setSettings(settings);

        Role userRole = roleRepository.findByRoleName("ROLE_" + RoleEnum.USER)
                .orElseGet(() -> roleRepository.save(new Role("ROLE_" + RoleEnum.USER.name())));

        user.getRoles().add(new UserRole(user, userRole));
        user.setVerificationCode(verificationCode);

        userRepository.save(user);

        emailService.sendVerificationCode(email, verificationCode);
    }
}
