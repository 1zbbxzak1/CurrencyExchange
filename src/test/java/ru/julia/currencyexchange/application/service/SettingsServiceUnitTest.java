package ru.julia.currencyexchange.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.julia.currencyexchange.domain.model.Settings;
import ru.julia.currencyexchange.infrastructure.repository.jpa.SettingsRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SettingsServiceUnitTest {
    @Mock
    private SettingsRepository settingsRepository;

    @InjectMocks
    private SettingsService settingsService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Установка глобальной комиссии, когда настройки уже есть")
    void setGlobalConversionFee_existingSettings() {
        Settings settings = new Settings();
        when(settingsRepository.findFirst()).thenReturn(Optional.of(settings));
        when(settingsRepository.save(any(Settings.class))).thenAnswer(inv -> inv.getArgument(0));

        settingsService.setGlobalConversionFee(0.15);

        assertThat(settings.getConversionFeePercent()).isEqualTo(0.15);
        assertThat(settings.getUser()).isNull();
        verify(settingsRepository).save(settings);
    }

    @Test
    @DisplayName("Установка глобальной комиссии, когда настроек нет")
    void setGlobalConversionFee_noSettings() {
        when(settingsRepository.findFirst()).thenReturn(Optional.empty());
        when(settingsRepository.save(any(Settings.class))).thenAnswer(inv -> inv.getArgument(0));

        settingsService.setGlobalConversionFee(0.25);

        verify(settingsRepository).save(argThat(s -> s.getConversionFeePercent() == 0.25 && s.getUser() == null));
    }

    @Test
    @DisplayName("Получение глобальной комиссии, когда настройки есть")
    void getGlobalConversionFeePercent_existingSettings() {
        Settings settings = new Settings();
        settings.setConversionFeePercent(0.33);
        when(settingsRepository.findFirst()).thenReturn(Optional.of(settings));

        double fee = settingsService.getGlobalConversionFeePercent();
        assertThat(fee).isEqualTo(0.33);
    }

    @Test
    @DisplayName("Получение глобальной комиссии, когда настроек нет")
    void getGlobalConversionFeePercent_noSettings() {
        when(settingsRepository.findFirst()).thenReturn(Optional.empty());
        double fee = settingsService.getGlobalConversionFeePercent();
        assertThat(fee).isEqualTo(0.0);
    }
} 