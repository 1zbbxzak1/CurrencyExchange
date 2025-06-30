package ru.julia.currencyexchange.application.bot.messages.converter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.support.ResourceBundleMessageSource;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class YamlMessageConverterTest {
    @Mock
    private ResourceBundleMessageSource messageSource;
    private YamlMessageConverter converter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        converter = new YamlMessageConverter(messageSource);
    }

    @Test
    @DisplayName("Успешное получение сообщения без параметров")
    void resolve_success_noParams() {
        when(messageSource.getMessage(eq("greeting"), any(), any())).thenReturn("Привет!");
        String result = converter.resolve("greeting", Map.of());
        assertThat(result).isEqualTo("Привет!");
    }

    @Test
    @DisplayName("Подстановка параметров в сообщение")
    void resolve_success_withParams() {
        when(messageSource.getMessage(eq("welcome"), any(), any())).thenReturn("Добро пожаловать, %name%!");
        Map<String, String> params = new HashMap<>();
        params.put("name", "Юлия");
        String result = converter.resolve("welcome", params);
        assertThat(result).isEqualTo("Добро пожаловать, Юлия!");
    }

    @Test
    @DisplayName("Если сообщение не найдено — возвращается id")
    void resolve_messageNotFound_returnsId() {
        when(messageSource.getMessage(eq("not_found"), any(), any())).thenThrow(new NoSuchMessageException("not_found"));
        String result = converter.resolve("not_found", Map.of());
        assertThat(result).isEqualTo("not_found");
    }

    @Test
    @DisplayName("messageSource возвращает null — результат null")
    void resolve_messageSourceReturnsNull() {
        when(messageSource.getMessage(eq("null_msg"), any(), any())).thenReturn(null);
        String result = converter.resolve("null_msg", Map.of());
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Плейсхолдеры не заменяются, если params пустой")
    void resolve_placeholderNotReplacedIfParamsEmpty() {
        when(messageSource.getMessage(eq("with_placeholder"), any(), any())).thenReturn("Hello, %name%!");
        String result = converter.resolve("with_placeholder", Map.of());
        assertThat(result).isEqualTo("Hello, %name%!");
    }

    @Test
    @DisplayName("Если params содержит null-значения — выбрасывается NullPointerException")
    void resolve_placeholderWithNullValue() {
        when(messageSource.getMessage(eq("with_placeholder"), any(), any())).thenReturn("Hello, %name%!");
        Map<String, String> params = new HashMap<>();
        params.put("name", null);
        org.junit.jupiter.api.Assertions.assertThrows(NullPointerException.class, () -> converter.resolve("with_placeholder", params));
    }
} 