package ru.julia.currencyexchange.infrastructure.repository.jpa;

import org.springframework.data.repository.CrudRepository;
import ru.julia.currencyexchange.domain.model.Settings;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;

public interface SettingsRepository extends CrudRepository<Settings, String> {
    @Query("SELECT s FROM Settings s")
    Optional<Settings> findFirst();
}
