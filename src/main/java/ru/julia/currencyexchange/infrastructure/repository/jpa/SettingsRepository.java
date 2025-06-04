package ru.julia.currencyexchange.infrastructure.repository.jpa;

import org.springframework.data.repository.CrudRepository;
import ru.julia.currencyexchange.domain.model.Settings;

public interface SettingsRepository extends CrudRepository<Settings, String> {
}
