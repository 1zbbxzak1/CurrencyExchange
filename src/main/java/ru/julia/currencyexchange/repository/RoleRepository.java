package ru.julia.currencyexchange.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import ru.julia.currencyexchange.entity.Role;
import ru.julia.currencyexchange.entity.enums.RoleEnum;

import java.util.Optional;

@RepositoryRestResource(path = "roles")
public interface RoleRepository extends CrudRepository<Role, String> {
    Optional<Role> findByRoleName(RoleEnum roleName);
}
