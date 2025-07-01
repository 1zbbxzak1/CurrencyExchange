package ru.julia.currencyexchange.infrastructure.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.julia.currencyexchange.domain.model.Role;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, String> {
    Optional<Role> findByRoleName(String roleName);
}
