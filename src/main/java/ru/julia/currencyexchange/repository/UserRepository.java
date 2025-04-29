package ru.julia.currencyexchange.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.julia.currencyexchange.entity.User;

@Repository
public interface UserRepository extends CrudRepository<User, String> {
}
