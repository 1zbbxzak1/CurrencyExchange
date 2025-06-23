package ru.julia.currencyexchange.infrastructure.repository.jpa;

import org.springframework.data.repository.CrudRepository;
import ru.julia.currencyexchange.domain.model.UserRole;

public interface UserRoleRepository extends CrudRepository<UserRole, String> {
}