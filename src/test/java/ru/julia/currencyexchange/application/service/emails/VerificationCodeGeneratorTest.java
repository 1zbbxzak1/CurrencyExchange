package ru.julia.currencyexchange.application.service.emails;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VerificationCodeGeneratorTest {
    private final VerificationCodeGenerator generator = new VerificationCodeGenerator();

    @Test
    void generateCode_returns6CharAlphanumeric() {
        String code = generator.generateCode();
        assertThat(code).hasSize(6);
        assertThat(code).matches("[A-Za-z0-9]{6}");
    }

    @Test
    void generateCode_isRandom() {
        String code1 = generator.generateCode();
        String code2 = generator.generateCode();

        assertThat(code1).isNotEqualTo(code2);
    }
} 