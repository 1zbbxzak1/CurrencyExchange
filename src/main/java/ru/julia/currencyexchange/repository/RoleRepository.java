package ru.julia.currencyexchange.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.julia.currencyexchange.entity.Role;
import ru.julia.currencyexchange.entity.enums.RoleEnum;

import java.util.Optional;

@Repository
public interface RoleRepository extends CrudRepository<Role, String> {
    Optional<Role> findByRoleName(RoleEnum roleName);
}
