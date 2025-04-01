package ru.julia.currencyexchange.configuration;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ru.julia.currencyexchange.entity.Role;
import ru.julia.currencyexchange.entity.enums.RoleEnum;
import ru.julia.currencyexchange.repository.RoleRepository;

@Component
public class RoleInitializer implements CommandLineRunner {
    private final RoleRepository roleRepository;

    public RoleInitializer(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        for (RoleEnum roleEnum : RoleEnum.values()) {
            if (roleRepository.findByRoleName(roleEnum).isEmpty()) {
                Role role = new Role(roleEnum);
                roleRepository.save(role);
            }
        }
    }
}
