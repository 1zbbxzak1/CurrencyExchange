package ru.julia.currencyexchange.application.service.bot;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.julia.currencyexchange.application.bot.settings.enums.SetFeeState;

import static org.assertj.core.api.Assertions.assertThat;

class SetFeeStateServiceUnitTest {
    private SetFeeStateService service;

    @BeforeEach
    void setUp() {
        service = new SetFeeStateService();
    }

    @Test
    @DisplayName("getState возвращает NONE по умолчанию")
    void getStateDefaultNone() {
        assertThat(service.getState(1L)).isEqualTo(SetFeeState.NONE);
    }

    @Test
    @DisplayName("setState/getState/clearState работают корректно для разных chatId")
    void statePerChatId() {
        Long chat1 = 1L, chat2 = 2L;
        service.setState(chat1, SetFeeState.WAITING_MANUAL_FEE);
        assertThat(service.getState(chat1)).isEqualTo(SetFeeState.WAITING_MANUAL_FEE);
        assertThat(service.getState(chat2)).isEqualTo(SetFeeState.NONE);

        service.setState(chat2, SetFeeState.WAITING_MANUAL_FEE);
        assertThat(service.getState(chat2)).isEqualTo(SetFeeState.WAITING_MANUAL_FEE);

        service.clearState(chat1);
        assertThat(service.getState(chat1)).isEqualTo(SetFeeState.NONE);
        assertThat(service.getState(chat2)).isEqualTo(SetFeeState.WAITING_MANUAL_FEE);
    }

    @Test
    @DisplayName("clearState сбрасывает только для указанного chatId")
    void clearStateOnlyForOne() {
        Long chat1 = 10L, chat2 = 11L;
        service.setState(chat1, SetFeeState.WAITING_MANUAL_FEE);
        service.setState(chat2, SetFeeState.WAITING_MANUAL_FEE);

        service.clearState(chat1);
        assertThat(service.getState(chat1)).isEqualTo(SetFeeState.NONE);
        assertThat(service.getState(chat2)).isEqualTo(SetFeeState.WAITING_MANUAL_FEE);
    }

    @Test
    @DisplayName("Данные не пересекаются между chatId")
    void dataIsolationBetweenChats() {
        Long chat1 = 20L, chat2 = 21L;
        service.setState(chat1, SetFeeState.WAITING_MANUAL_FEE);
        service.setState(chat2, SetFeeState.NONE);

        assertThat(service.getState(chat1)).isEqualTo(SetFeeState.WAITING_MANUAL_FEE);
        assertThat(service.getState(chat2)).isEqualTo(SetFeeState.NONE);
    }

    @Test
    @DisplayName("После clearState getState=NONE")
    void clearStateResetsState() {
        Long chatId = 30L;
        service.setState(chatId, SetFeeState.WAITING_MANUAL_FEE);
        service.clearState(chatId);
        
        assertThat(service.getState(chatId)).isEqualTo(SetFeeState.NONE);
    }
} 