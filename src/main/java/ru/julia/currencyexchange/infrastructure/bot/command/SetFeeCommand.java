package ru.julia.currencyexchange.infrastructure.bot.command;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import org.springframework.stereotype.Component;
import ru.julia.currencyexchange.application.bot.messages.converter.interfaces.MessageConverter;
import ru.julia.currencyexchange.application.bot.settings.enums.SetFeeState;
import ru.julia.currencyexchange.application.service.SettingsService;
import ru.julia.currencyexchange.application.service.UserService;
import ru.julia.currencyexchange.application.service.bot.SetFeeStateService;
import ru.julia.currencyexchange.domain.model.User;
import ru.julia.currencyexchange.infrastructure.bot.command.abstracts.AbstractCommandHandler;
import ru.julia.currencyexchange.infrastructure.bot.command.builder.SetFeeKeyboardBuilder;
import ru.julia.currencyexchange.infrastructure.bot.command.handler.SetFeeCallbackHandler;

import java.util.Map;

@Component
public class SetFeeCommand extends AbstractCommandHandler {
    private final UserService userService;
    private final SettingsService settingsService;
    private final SetFeeStateService setFeeStateService;
    private final SetFeeKeyboardBuilder keyboardBuilder;
    private final SetFeeCallbackHandler callbackHandler;

    public SetFeeCommand(MessageConverter messageConverter,
                         UserService userService,
                         SettingsService settingsService,
                         SetFeeStateService setFeeStateService,
                         SetFeeKeyboardBuilder keyboardBuilder,
                         SetFeeCallbackHandler callbackHandler) {
        super(messageConverter);
        this.userService = userService;
        this.settingsService = settingsService;
        this.setFeeStateService = setFeeStateService;
        this.keyboardBuilder = keyboardBuilder;
        this.callbackHandler = callbackHandler;
    }

    @Override
    public SendMessage handle(Update update) {
        Long chatId = update.message().chat().id();
        String text = update.message().text();
        User user = userService.findUserByChatId(chatId);
        
        if (!isAccessible(user)) {
            return new SendMessage(chatId, messageConverter.resolve("command.setFee.no_access"));
        }

        SetFeeState currentState = setFeeStateService.getState(chatId);
        if (currentState == SetFeeState.WAITING_MANUAL_FEE) {
            return handleManualFeeInput(chatId, text);
        }

        double currentFee = settingsService.getGlobalConversionFeePercent();
        String messageText = messageConverter.resolve("command.setFee.current_fee", 
                Map.of("fee", String.valueOf(currentFee)));
        
        var keyboard = keyboardBuilder.buildFeeSelectionKeyboard(currentFee);
        
        return new SendMessage(chatId, messageText)
                .parseMode(ParseMode.Markdown)
                .replyMarkup(keyboard);
    }

    @Override
    public boolean matches(Update update) {
        if (update.message() == null || update.message().text() == null) {
            return false;
        }

        String text = update.message().text();

        if (text.equals("/setFee")) {
            return true;
        }

        Long chatId = update.message().chat().id();
        return setFeeStateService.getState(chatId) != SetFeeState.NONE;
    }

    @Override
    public String getCommand() {
        return "/setFee";
    }

    @Override
    public String getDescription() {
        return "command.setFee.description";
    }

    @Override
    public boolean isAccessible(User user) {
        return user != null && "ADMIN".equals(getUserRole(user)) && !user.isDeleted() && user.isVerified();
    }

    private SendMessage handleManualFeeInput(Long chatId, String message) {
        try {
            double fee = Double.parseDouble(message.replace(",", "."));
            if (fee < 0 || fee > 100) {
                return new SendMessage(chatId, messageConverter.resolve("command.setFee.invalid_value"));
            }

            settingsService.setGlobalConversionFee(fee);
            setFeeStateService.clearState(chatId);

            String successMessage = messageConverter.resolve("command.setFee.success",
                    Map.of("fee", String.valueOf(fee)));
            return new SendMessage(chatId, successMessage);

        } catch (NumberFormatException e) {
            return new SendMessage(chatId, messageConverter.resolve("command.setFee.invalid_value"));
        }
    }

    public SetFeeCallbackHandler getCallbackHandler() {
        return callbackHandler;
    }
} 