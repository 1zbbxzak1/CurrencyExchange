package ru.julia.currencyexchange.infrastructure.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.julia.currencyexchange.domain.model.Settings;

import java.util.Optional;

public interface SettingsRepository extends JpaRepository<Settings, String> {
    @Query("SELECT s FROM Settings s")
    Optional<Settings> findFirst();
}
