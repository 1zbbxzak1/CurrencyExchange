package ru.julia.currencyexchange.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.julia.currencyexchange.entity.User;

import java.util.Optional;

@Repository
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
