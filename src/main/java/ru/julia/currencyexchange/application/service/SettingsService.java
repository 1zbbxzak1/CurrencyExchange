package ru.julia.currencyexchange.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.julia.currencyexchange.domain.model.Settings;
import ru.julia.currencyexchange.infrastructure.repository.jpa.SettingsRepository;

@Service
public class SettingsService {
    private final SettingsRepository settingsRepository;

    public SettingsService(SettingsRepository settingsRepository) {
        this.settingsRepository = settingsRepository;
    }

    @Transactional
    public void setGlobalConversionFee(double feePercent) {
        Settings settings = settingsRepository.findFirst().orElse(new Settings());
        settings.setUser(null);
        settings.setConversionFeePercent(feePercent);
        settingsRepository.save(settings);
    }

    public double getGlobalConversionFeePercent() {
        return settingsRepository.findFirst()
                .map(Settings::getConversionFeePercent)
                .orElse(0.0);
    }
} 