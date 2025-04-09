package ru.julia.currencyexchange.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import ru.julia.currencyexchange.entity.Settings;

import java.util.Optional;

@RepositoryRestResource(path = "settings")
public interface SettingsRepository extends CrudRepository<Settings, String> {
    Optional<Settings> findByUserId(String userId);
}
