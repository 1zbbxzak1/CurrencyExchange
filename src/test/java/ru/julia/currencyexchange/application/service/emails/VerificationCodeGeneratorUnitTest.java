package ru.julia.currencyexchange.application.service.emails;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VerificationCodeGeneratorUnitTest {
    private final VerificationCodeGenerator generator = new VerificationCodeGenerator();

    @Test
    @DisplayName("Генерация кода: длина и формат")
    void generateCode_shouldReturnCodeOfCorrectLengthAndFormat() {
        String code = generator.generateCode();

        assertThat(code).hasSize(6);
        assertThat(code).matches("[A-Za-z0-9]{6}");
    }

    @Test
    @DisplayName("Генерация нескольких кодов: уникальность")
    void generateCode_shouldReturnUniqueCodes() {
        String code1 = generator.generateCode();
        String code2 = generator.generateCode();

        assertThat(code1).isNotEqualTo(code2);
    }
} 