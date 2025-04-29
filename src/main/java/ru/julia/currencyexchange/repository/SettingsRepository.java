package ru.julia.currencyexchange.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.julia.currencyexchange.entity.Settings;

@Repository
public interface SettingsRepository extends CrudRepository<Settings, String> {
}
