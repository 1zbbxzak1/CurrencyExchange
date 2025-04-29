package ru.julia.currencyexchange.infrastructure.repository.jpa;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.julia.currencyexchange.domain.model.Role;

import java.util.Optional;

@Repository
public interface RoleRepository extends CrudRepository<Role, String> {
    Optional<Role> findByRoleName(String roleName);
}
