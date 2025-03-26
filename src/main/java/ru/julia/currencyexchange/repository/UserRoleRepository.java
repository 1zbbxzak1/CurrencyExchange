package ru.julia.currencyexchange.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.julia.currencyexchange.entity.UserRole;

import java.util.List;

@Repository
public interface UserRoleRepository extends CrudRepository<UserRole, String> {
    List<UserRole> findByUserId(String userId);
}
