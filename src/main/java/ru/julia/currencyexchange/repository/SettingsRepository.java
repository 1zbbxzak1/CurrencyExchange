package ru.julia.currencyexchange.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.julia.currencyexchange.entity.Settings;

import java.util.Optional;

@Repository
public interface SettingsRepository extends CrudRepository<Settings, String> {
    Optional<Settings> findByUserId(String userId);
}
