package ru.julia.currencyexchange.infrastructure.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.julia.currencyexchange.domain.model.UserRole;

public interface UserRoleRepository extends JpaRepository<UserRole, String> {
}