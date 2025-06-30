package ru.julia.currencyexchange.application.service.bot.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.julia.currencyexchange.application.bot.settings.RegistrationData;
import ru.julia.currencyexchange.application.bot.settings.enums.RegistrationState;
import ru.julia.currencyexchange.application.service.bot.RegistrationStateService;

import static org.assertj.core.api.Assertions.assertThat;

class RegistrationStateServiceUnitTest {
    private RegistrationStateService service;

    @BeforeEach
    void setUp() {
        service = new RegistrationStateService();
    }

    @Test
    @DisplayName("getState/setState/clearData работают корректно для разных chatId")
    void statePerChatId() {
        Long chat1 = 1L, chat2 = 2L;
        assertThat(service.getState(chat1)).isEqualTo(RegistrationState.NONE);

        service.setState(chat1, RegistrationState.WAITING_EMAIL);
        assertThat(service.getState(chat1)).isEqualTo(RegistrationState.WAITING_EMAIL);
        assertThat(service.getState(chat2)).isEqualTo(RegistrationState.NONE);

        service.setState(chat2, RegistrationState.WAITING_PASSWORD);
        assertThat(service.getState(chat2)).isEqualTo(RegistrationState.WAITING_PASSWORD);

        service.clearData(chat1);
        assertThat(service.getState(chat1)).isEqualTo(RegistrationState.NONE);
        assertThat(service.getState(chat2)).isEqualTo(RegistrationState.WAITING_PASSWORD);
    }

    @Test
    @DisplayName("setEmail/setPassword/setVerificationCode и getData работают корректно")
    void setAndGetData() {
        Long chatId = 10L;
        service.setEmail(chatId, "mail@mail.com");
        service.setPassword(chatId, "pass");
        service.setVerificationCode(chatId, "1234");

        RegistrationData data = service.getData(chatId);

        assertThat(data.getEmail()).isEqualTo("mail@mail.com");
        assertThat(data.getPassword()).isEqualTo("pass");
        assertThat(data.getVerificationCode()).isEqualTo("1234");
    }

    @Test
    @DisplayName("clearData очищает и state, и data")
    void clearDataRemovesAll() {
        Long chatId = 20L;
        service.setState(chatId, RegistrationState.WAITING_EMAIL);
        service.setEmail(chatId, "a@a");
        service.clearData(chatId);

        assertThat(service.getState(chatId)).isEqualTo(RegistrationState.NONE);

        RegistrationData data = service.getData(chatId);

        assertThat(data.getEmail()).isNull();
        assertThat(data.getPassword()).isNull();
        assertThat(data.getVerificationCode()).isNull();
    }

    @Test
    @DisplayName("getData возвращает новый RegistrationData, если не было")
    void getDataCreatesIfAbsent() {
        Long chatId = 30L;
        RegistrationData data1 = service.getData(chatId);
        assertThat(data1).isNotNull();

        RegistrationData data2 = service.getData(chatId);
        assertThat(data2).isSameAs(data1);
    }

    @Test
    @DisplayName("setEmail/setPassword/setVerificationCode на несуществующем chatId создаёт data")
    void settersCreateDataIfAbsent() {
        Long chatId = 40L;
        service.setEmail(chatId, "e");
        assertThat(service.getData(chatId).getEmail()).isEqualTo("e");

        service.setPassword(chatId, "p");
        assertThat(service.getData(chatId).getPassword()).isEqualTo("p");

        service.setVerificationCode(chatId, "v");
        assertThat(service.getData(chatId).getVerificationCode()).isEqualTo("v");
    }

    @Test
    @DisplayName("Данные не пересекаются между chatId")
    void dataIsolationBetweenChats() {
        Long chat1 = 50L, chat2 = 51L;

        service.setEmail(chat1, "a");
        service.setEmail(chat2, "b");
        assertThat(service.getData(chat1).getEmail()).isEqualTo("a");
        assertThat(service.getData(chat2).getEmail()).isEqualTo("b");

        service.setPassword(chat1, "p1");
        service.setPassword(chat2, "p2");
        assertThat(service.getData(chat1).getPassword()).isEqualTo("p1");
        assertThat(service.getData(chat2).getPassword()).isEqualTo("p2");
    }

    @Test
    @DisplayName("После clearData getState=NONE и getData новый объект")
    void clearDataResetsData() {
        Long chatId = 60L;
        service.setEmail(chatId, "x");

        RegistrationData before = service.getData(chatId);
        service.clearData(chatId);
        RegistrationData after = service.getData(chatId);

        assertThat(after).isNotSameAs(before);
        assertThat(service.getState(chatId)).isEqualTo(RegistrationState.NONE);
    }
} 