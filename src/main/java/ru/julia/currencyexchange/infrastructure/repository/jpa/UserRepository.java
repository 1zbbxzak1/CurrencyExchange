package ru.julia.currencyexchange.infrastructure.repository.jpa;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import ru.julia.currencyexchange.domain.model.User;

import java.util.Optional;

public interface UserRepository extends CrudRepository<User, String> {
    Boolean existsByUsername(String username);

    @Query("""
            SELECT u FROM User u 
            LEFT JOIN FETCH u.roles r 
            LEFT JOIN FETCH r.role 
            WHERE u.username = :username
            """)
    Optional<User> findByUsernameWithRoles(String username);
}
