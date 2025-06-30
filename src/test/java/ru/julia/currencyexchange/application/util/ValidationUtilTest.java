package ru.julia.currencyexchange.application.util;

import org.junit.jupiter.api.Test;
import ru.julia.currencyexchange.application.exceptions.InvalidParameterException;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ValidationUtilTest {
    @Test
    void validateNotEmpty_throwsOnNullOrEmpty() {
        assertThatThrownBy(() -> ValidationUtil.validateNotEmpty(null, "param"))
                .isInstanceOf(InvalidParameterException.class)
                .hasMessageContaining("param");

        assertThatThrownBy(() -> ValidationUtil.validateNotEmpty("   ", "param"))
                .isInstanceOf(InvalidParameterException.class)
                .hasMessageContaining("param");
    }

    @Test
    void validateNotEmpty_passesOnNonEmpty() {
        assertThatCode(() -> ValidationUtil.validateNotEmpty("abc", "param")).doesNotThrowAnyException();
    }

    @Test
    void validateNotNull_throwsOnNull() {
        assertThatThrownBy(() -> ValidationUtil.validateNotNull(null, "param"))
                .isInstanceOf(InvalidParameterException.class)
                .hasMessageContaining("param");
    }

    @Test
    void validateNotNull_passesOnNonNull() {
        assertThatCode(() -> ValidationUtil.validateNotNull(123, "param")).doesNotThrowAnyException();
    }

    @Test
    void validateUserId_throwsOnEmpty() {
        assertThatThrownBy(() -> ValidationUtil.validateUserId(" "))
                .isInstanceOf(InvalidParameterException.class);
    }

    @Test
    void validateUserId_passesOnValid() {
        assertThatCode(() -> ValidationUtil.validateUserId("id123")).doesNotThrowAnyException();
    }

    @Test
    void validateCurrencyCode_throwsOnEmpty() {
        assertThatThrownBy(() -> ValidationUtil.validateCurrencyCode(" "))
                .isInstanceOf(InvalidParameterException.class);
    }

    @Test
    void validateCurrencyCode_passesOnValid() {
        assertThatCode(() -> ValidationUtil.validateCurrencyCode("USD")).doesNotThrowAnyException();
    }

    @Test
    void validateTimestamp_throwsOnEmpty() {
        assertThatThrownBy(() -> ValidationUtil.validateTimestamp(" "))
                .isInstanceOf(InvalidParameterException.class);
    }

    @Test
    void validateTimestamp_passesOnValid() {
        assertThatCode(() -> ValidationUtil.validateTimestamp("2024-01-01")).doesNotThrowAnyException();
    }

    @Test
    void validateChatId_throwsOnNullOrZeroOrNegative() {
        assertThatThrownBy(() -> ValidationUtil.validateChatId(null))
                .isInstanceOf(InvalidParameterException.class);

        assertThatThrownBy(() -> ValidationUtil.validateChatId(0L))
                .isInstanceOf(InvalidParameterException.class);

        assertThatThrownBy(() -> ValidationUtil.validateChatId(-1L))
                .isInstanceOf(InvalidParameterException.class);
    }

    @Test
    void validateChatId_passesOnPositive() {
        assertThatCode(() -> ValidationUtil.validateChatId(123L)).doesNotThrowAnyException();
    }

    @Test
    void validateUsername_throwsOnNullBlankOrTooLong() {
        assertThatThrownBy(() -> ValidationUtil.validateUsername(null))
                .isInstanceOf(InvalidParameterException.class);

        assertThatThrownBy(() -> ValidationUtil.validateUsername("   "))
                .isInstanceOf(InvalidParameterException.class);

        String longName = "a".repeat(65);

        assertThatThrownBy(() -> ValidationUtil.validateUsername(longName))
                .isInstanceOf(InvalidParameterException.class);
    }

    @Test
    void validateUsername_passesOnValid() {
        assertThatCode(() -> ValidationUtil.validateUsername("user123")).doesNotThrowAnyException();
    }
} 