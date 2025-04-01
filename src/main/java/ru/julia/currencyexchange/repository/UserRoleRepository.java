package ru.julia.currencyexchange.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import ru.julia.currencyexchange.entity.UserRole;

import java.util.List;

@RepositoryRestResource(path = "user_roles")
public interface UserRoleRepository extends CrudRepository<UserRole, String> {
    List<UserRole> findByUserId(String userId);
}