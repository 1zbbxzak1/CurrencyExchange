package ru.julia.currencyexchange.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.julia.currencyexchange.domain.model.Settings;
import ru.julia.currencyexchange.infrastructure.repository.jpa.SettingsRepository;
import ru.julia.currencyexchange.utils.annotation.ActiveProfile;
import ru.julia.currencyexchange.utils.annotation.PostgresTestcontainers;
import ru.julia.currencyexchange.utils.configuration.DatabaseCleaner;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfile
@PostgresTestcontainers
@Transactional
class SettingsServiceIntegrationTest {
    @Autowired
    private SettingsService settingsService;
    @Autowired
    private SettingsRepository settingsRepository;
    @Autowired
    private DatabaseCleaner databaseCleaner;

    @BeforeEach
    void cleanDb() {
        databaseCleaner.resetDatabase();
    }

    @Test
    @DisplayName("Установка и получение глобальной комиссии, когда настроек не было")
    void setAndGetGlobalConversionFee_noSettings() {
        settingsService.setGlobalConversionFee(0.12);
        Settings settings = settingsRepository.findFirst().orElseThrow();
        assertThat(settings.getConversionFeePercent()).isEqualTo(0.12);
        assertThat(settings.getUser()).isNull();
        double fee = settingsService.getGlobalConversionFeePercent();
        assertThat(fee).isEqualTo(0.12);
    }

    @Test
    @DisplayName("Обновление глобальной комиссии, когда настройки уже есть")
    void updateGlobalConversionFee_existingSettings() {
        Settings settings = new Settings();
        settings.setConversionFeePercent(0.05);
        settingsRepository.save(settings);
        settingsService.setGlobalConversionFee(0.25);
        Settings updated = settingsRepository.findFirst().orElseThrow();
        assertThat(updated.getConversionFeePercent()).isEqualTo(0.25);
        assertThat(updated.getUser()).isNull();
    }

    @Test
    @DisplayName("Получение глобальной комиссии, когда настроек нет")
    void getGlobalConversionFeePercent_noSettings() {
        double fee = settingsService.getGlobalConversionFeePercent();
        assertThat(fee).isEqualTo(0.0);
    }
} 