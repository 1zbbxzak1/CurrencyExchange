package ru.julia.currencyexchange.infrastructure.repository.jpa;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.julia.currencyexchange.domain.model.Settings;
import ru.julia.currencyexchange.utils.annotation.ActiveProfile;
import ru.julia.currencyexchange.utils.annotation.PostgresTestcontainers;
import ru.julia.currencyexchange.utils.configuration.DatabaseCleaner;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfile
@PostgresTestcontainers
class SettingsRepositoryTest {
    @Autowired
    private SettingsRepository settingsRepository;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @BeforeEach
    void cleanDb() {
        databaseCleaner.resetDatabase();
    }

    @Test
    @DisplayName("Сохранение и получение Settings")
    void saveAndFindSettings() {
        Settings settings = new Settings();
        settings.setConversionFeePercent(0.05);
        settingsRepository.save(settings);

        Optional<Settings> found = settingsRepository.findById(settings.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getConversionFeePercent()).isEqualTo(0.05);
    }

    @Test
    @DisplayName("findFirst возвращает первый Settings")
    void findFirstReturnsSettings() {
        Settings settings = new Settings();
        settings.setConversionFeePercent(0.1);
        settingsRepository.save(settings);

        Optional<Settings> found = settingsRepository.findFirst();

        assertThat(found).isPresent();
        assertThat(found.get().getConversionFeePercent()).isEqualTo(0.1);
    }

    @Test
    @DisplayName("Поиск несуществующих настроек возвращает empty")
    void findByIdNotFound() {
        Optional<Settings> found = settingsRepository.findById("nonexistent-id");
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Удаление настроек")
    void deleteSettings() {
        Settings settings = new Settings();
        settings.setConversionFeePercent(0.01);

        settingsRepository.save(settings);
        settingsRepository.deleteById(settings.getId());

        assertThat(settingsRepository.findById(settings.getId())).isEmpty();
    }

    @Test
    @DisplayName("Обновление настроек")
    void updateSettings() {
        Settings settings = new Settings();
        settings.setConversionFeePercent(0.01);
        settingsRepository.save(settings);
        settings.setConversionFeePercent(0.02);
        settingsRepository.save(settings);

        Optional<Settings> found = settingsRepository.findById(settings.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getConversionFeePercent()).isEqualTo(0.02);
    }

    @Test
    @DisplayName("findAll возвращает все настройки")
    void findAllReturnsAllSettings() {
        Settings settings1 = new Settings();
        settings1.setConversionFeePercent(0.01);

        Settings settings2 = new Settings();
        settings2.setConversionFeePercent(0.02);
        
        settingsRepository.save(settings1);
        settingsRepository.save(settings2);

        Iterable<Settings> settingsList = settingsRepository.findAll();

        assertThat(settingsList).extracting(Settings::getConversionFeePercent).contains(0.01, 0.02);
    }
}