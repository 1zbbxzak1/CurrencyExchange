package ru.julia.currencyexchange.application.util;

import org.junit.jupiter.api.Test;
import ru.julia.currencyexchange.application.dto.currency.CurrencyConversionResponse;
import ru.julia.currencyexchange.application.dto.currency.CurrencyResponse;
import ru.julia.currencyexchange.application.dto.user.UserResponse;
import ru.julia.currencyexchange.domain.model.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class DtoMapperTest {
    @Test
    void mapToCurrencyResponse_mapsAllFields() {
        Currency currency = new Currency("USD", "Доллар", BigDecimal.valueOf(75.5));
        currency.setExchangeRate(BigDecimal.valueOf(75.5));
        LocalDateTime now = LocalDateTime.now();
        currency.setName("Доллар");

        CurrencyResponse response = DtoMapper.mapToCurrencyResponse(currency);

        assertThat(response.getCode()).isEqualTo("USD");
        assertThat(response.getName()).isEqualTo("Доллар");
        assertThat(response.getExchangeRate()).isEqualByComparingTo(BigDecimal.valueOf(75.5));
        assertThat(response.getLastUpdated()).isNotNull();
    }

    @Test
    void mapToCurrencyConversionResponse_mapsAllFields() {
        User user = new User();
        user.setChatId(123L);
        user.setUsername("testuser");
        user.setEmail("test@mail.com");

        Currency from = new Currency("USD", "Доллар", BigDecimal.valueOf(75.5));
        Currency to = new Currency("RUB", "Рубль", BigDecimal.valueOf(1.0));
        CurrencyConversion conversion = new CurrencyConversion(user, from, to, BigDecimal.TEN, BigDecimal.valueOf(750), BigDecimal.valueOf(75));
        CurrencyConversionResponse response = DtoMapper.mapToCurrencyConversionResponse(conversion);

        assertThat(response.getUserId()).isEqualTo(user.getId());
        assertThat(response.getSourceCurrency()).isEqualTo("USD");
        assertThat(response.getTargetCurrency()).isEqualTo("RUB");
        assertThat(response.getAmount()).isEqualByComparingTo(BigDecimal.TEN);
        assertThat(response.getConvertedAmount()).isEqualByComparingTo(BigDecimal.valueOf(750));
        assertThat(response.getConversionRate()).isEqualByComparingTo(BigDecimal.valueOf(75));
        assertThat(response.getTimestamp()).isNotNull();
    }

    @Test
    void mapToUserResponse_mapsAllFields() {
        User user = new User();
        user.setChatId(123L);
        user.setUsername("testuser");
        user.setEmail("test@mail.com");
        user.setVerified(true);
        user.setBanned(false);
        Role role = new Role("USER");
        UserRole userRole = new UserRole(user, role);
        user.getRoles().add(userRole);

        UserResponse response = DtoMapper.mapToUserResponse(user);
        
        assertThat(response.getId()).isEqualTo(user.getId());
        assertThat(response.getChatId()).isEqualTo(123L);
        assertThat(response.getUsername()).isEqualTo("testuser");
        assertThat(response.getEmail()).isEqualTo("test@mail.com");
        assertThat(response.isVerified()).isTrue();
        assertThat(response.isBanned()).isFalse();
        assertThat(response.getRoles()).containsExactly("USER");
        assertThat(response.getCreatedAt()).isNotNull();
    }
} 