package ru.julia.currencyexchange.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.julia.currencyexchange.entity.UserRole;

@Repository
public interface UserRoleRepository extends CrudRepository<UserRole, String> {
}