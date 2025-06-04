package ru.julia.currencyexchange.infrastructure.repository.jpa;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.julia.currencyexchange.domain.model.UserRole;

@Repository
public interface UserRoleRepository extends CrudRepository<UserRole, String> {
}