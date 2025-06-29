package ru.julia.currencyexchange.infrastructure.repository.jpa;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.julia.currencyexchange.domain.model.UserRole;
import ru.julia.currencyexchange.utils.annotation.ActiveProfile;
import ru.julia.currencyexchange.utils.annotation.PostgresTestcontainers;
import ru.julia.currencyexchange.utils.configuration.DatabaseCleaner;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfile
@PostgresTestcontainers
class UserRoleRepositoryTest {
    @Autowired
    private UserRoleRepository userRoleRepository;
    @Autowired
    private ru.julia.currencyexchange.infrastructure.repository.jpa.UserRepository userRepository;
    @Autowired
    private ru.julia.currencyexchange.infrastructure.repository.jpa.RoleRepository roleRepository;
    @Autowired
    private DatabaseCleaner databaseCleaner;

    @BeforeEach
    void cleanDb() {
        databaseCleaner.resetDatabase();
    }

    @Test
    @DisplayName("Сохранение и поиск по id")
    void saveAndFindById() {
        // Создаем пользователя
        ru.julia.currencyexchange.domain.model.User user = new ru.julia.currencyexchange.domain.model.User();
        user.setUsername("userroleuser");
        user.setEmail("userrole@example.com");
        user.setChatId(33333L);
        user.setPassword("testpass");
        userRepository.save(user);

        // Создаем роль
        ru.julia.currencyexchange.domain.model.Role role = new ru.julia.currencyexchange.domain.model.Role();
        role.setRoleName("USER_ROLE");
        roleRepository.save(role);

        UserRole userRole = new UserRole(user, role);
        userRoleRepository.save(userRole);

        Optional<UserRole> found = userRoleRepository.findById(userRole.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getUser().getUsername()).isEqualTo("userroleuser");
        assertThat(found.get().getRole().getRoleName()).isEqualTo("USER_ROLE");
    }

    @Test
    @DisplayName("Поиск несуществующего UserRole возвращает empty")
    void findByIdNotFound() {
        Optional<UserRole> found = userRoleRepository.findById("nonexistent-id");
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Удаление UserRole")
    void deleteUserRole() {
        ru.julia.currencyexchange.domain.model.User user = new ru.julia.currencyexchange.domain.model.User();
        user.setUsername("userrole1");
        user.setEmail("userrole1@example.com");
        user.setChatId(88888L);
        user.setPassword("testpass");
        userRepository.save(user);
        ru.julia.currencyexchange.domain.model.Role role = new ru.julia.currencyexchange.domain.model.Role();
        role.setRoleName("ROLE1");
        roleRepository.save(role);
        UserRole userRole = new UserRole(user, role);
        userRoleRepository.save(userRole);
        userRoleRepository.deleteById(userRole.getId());
        assertThat(userRoleRepository.findById(userRole.getId())).isEmpty();
    }

    @Test
    @DisplayName("Обновление UserRole")
    void updateUserRole() {
        ru.julia.currencyexchange.domain.model.User user = new ru.julia.currencyexchange.domain.model.User();
        user.setUsername("userrole2");
        user.setEmail("userrole2@example.com");
        user.setChatId(99999L);
        user.setPassword("testpass");
        userRepository.save(user);
        ru.julia.currencyexchange.domain.model.Role role = new ru.julia.currencyexchange.domain.model.Role();
        role.setRoleName("ROLE2");
        roleRepository.save(role);
        UserRole userRole = new UserRole(user, role);
        userRoleRepository.save(userRole);
        // Обновление: создадим новую роль и присвоим userRole
        ru.julia.currencyexchange.domain.model.Role newRole = new ru.julia.currencyexchange.domain.model.Role();
        newRole.setRoleName("ROLE3");
        roleRepository.save(newRole);
        userRole.setRole(newRole);
        userRoleRepository.save(userRole);
        Optional<UserRole> found = userRoleRepository.findById(userRole.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getRole().getRoleName()).isEqualTo("ROLE3");
    }

    @Test
    @DisplayName("findAll возвращает все UserRole")
    void findAllReturnsAllUserRoles() {
        ru.julia.currencyexchange.domain.model.User user = new ru.julia.currencyexchange.domain.model.User();
        user.setUsername("userrole1");
        user.setEmail("userrole1@example.com");
        user.setChatId(88888L);
        user.setPassword("testpass");
        userRepository.save(user);
        ru.julia.currencyexchange.domain.model.Role role = new ru.julia.currencyexchange.domain.model.Role();
        role.setRoleName("ROLE1");
        roleRepository.save(role);
        UserRole userRole = new UserRole(user, role);
        userRoleRepository.save(userRole);
        Iterable<UserRole> userRoles = userRoleRepository.findAll();
        assertThat(userRoles).extracting(ur -> ur.getUser().getUsername()).contains("userrole1");
    }
}