package ru.julia.currencyexchange.infrastructure.bot.command.builder;

import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import org.springframework.stereotype.Component;
import ru.julia.currencyexchange.application.bot.messages.converter.interfaces.MessageConverter;

@Component
public class SetFeeKeyboardBuilder {
    private final MessageConverter messageConverter;

    public SetFeeKeyboardBuilder(MessageConverter messageConverter) {
        this.messageConverter = messageConverter;
    }

    public InlineKeyboardMarkup buildFeeSelectionKeyboard(double currentFee) {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();

        double[] popularFees = {0.0, 1.0, 2.0, 5.0, 10.0};
        
        for (double fee : popularFees) {
            String buttonText = fee + "%";
            if (fee == currentFee) {
                buttonText += " ✓";
            }
            
            InlineKeyboardButton button = new InlineKeyboardButton(buttonText)
                    .callbackData("set_fee_" + fee);
            keyboardMarkup.addRow(button);
        }

        InlineKeyboardButton manualButton = new InlineKeyboardButton(
                messageConverter.resolve("command.setFee.keyboard.manual_input"))
                .callbackData("set_fee_manual");
        keyboardMarkup.addRow(manualButton);

        return keyboardMarkup;
    }
} 