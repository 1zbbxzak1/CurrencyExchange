package ru.julia.currencyexchange.infrastructure.repository.jpa;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.julia.currencyexchange.domain.model.Role;
import ru.julia.currencyexchange.utils.annotation.ActiveProfile;
import ru.julia.currencyexchange.utils.configuration.DatabaseCleaner;
import ru.julia.currencyexchange.utils.configuration.IntegrationTestBase;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfile
class RoleRepositoryIntegrationTest extends IntegrationTestBase {
    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @BeforeEach
    void cleanDb() {
        databaseCleaner.resetDatabase();
    }

    @Test
    @DisplayName("Сохранение и поиск по id")
    void saveAndFindById() {
        Role role = new Role();
        role.setRoleName("ADMIN");
        roleRepository.save(role);

        Optional<Role> found = roleRepository.findById(role.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getRoleName()).isEqualTo("ADMIN");
    }

    @Test
    @DisplayName("Поиск по имени роли")
    void findByRoleName() {
        Role role = new Role();
        role.setRoleName("USER");
        roleRepository.save(role);

        Optional<Role> found = roleRepository.findByRoleName("USER");

        assertThat(found).isPresent();
        assertThat(found.get().getRoleName()).isEqualTo("USER");
    }

    @Test
    @DisplayName("Поиск несуществующей роли возвращает empty")
    void findByIdNotFound() {
        Optional<Role> found = roleRepository.findById("nonexistent-id");
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Удаление роли")
    void deleteRole() {
        Role role = new Role();
        role.setRoleName("TO_DELETE");

        roleRepository.save(role);
        roleRepository.deleteById(role.getId());

        assertThat(roleRepository.findById(role.getId())).isEmpty();
    }

    @Test
    @DisplayName("Обновление роли")
    void updateRole() {
        Role role = new Role();
        role.setRoleName("TO_UPDATE");
        roleRepository.save(role);
        role.setRoleName("UPDATED");
        roleRepository.save(role);

        Optional<Role> found = roleRepository.findById(role.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getRoleName()).isEqualTo("UPDATED");
    }

    @Test
    @DisplayName("findAll возвращает все роли")
    void findAllReturnsAllRoles() {
        Role role1 = new Role();
        role1.setRoleName("ADMIN");

        Role role2 = new Role();
        role2.setRoleName("USER");

        roleRepository.save(role1);
        roleRepository.save(role2);

        Iterable<Role> roles = roleRepository.findAll();

        assertThat(roles).extracting(Role::getRoleName).contains("ADMIN", "USER");
    }
}