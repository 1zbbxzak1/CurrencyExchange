package ru.julia.currencyexchange.infrastructure.repository.jpa;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.julia.currencyexchange.domain.model.Settings;

@Repository
public interface SettingsRepository extends CrudRepository<Settings, String> {
}
