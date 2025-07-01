package ru.julia.currencyexchange.infrastructure.bot.command.handler;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.EditMessageText;
import org.springframework.stereotype.Component;
import ru.julia.currencyexchange.application.bot.messages.converter.interfaces.MessageConverter;
import ru.julia.currencyexchange.application.bot.settings.enums.SetFeeState;
import ru.julia.currencyexchange.application.service.bot.SetFeeStateService;
import ru.julia.currencyexchange.application.service.SettingsService;
import ru.julia.currencyexchange.application.service.UserService;
import ru.julia.currencyexchange.domain.model.User;

import java.util.Map;

@Component
public class SetFeeCallbackHandler {
    private final MessageConverter messageConverter;
    private final UserService userService;
    private final SettingsService settingsService;
    private final SetFeeStateService setFeeStateService;

    public SetFeeCallbackHandler(MessageConverter messageConverter, 
                                UserService userService, 
                                SettingsService settingsService,
                                SetFeeStateService setFeeStateService) {
        this.messageConverter = messageConverter;
        this.userService = userService;
        this.settingsService = settingsService;
        this.setFeeStateService = setFeeStateService;
    }

    public EditMessageText handleCallback(Update update) {
        if (update == null || update.callbackQuery() == null || update.callbackQuery().message() == null) {
            return createErrorResponse(null, null, "command.setFee.error");
        }

        Long chatId = update.callbackQuery().message().chat().id();
        Integer messageId = update.callbackQuery().message().messageId();
        String callbackData = update.callbackQuery().data();

        try {
            User adminUser = userService.findUserByChatId(chatId);
            if (adminUser == null || !"ADMIN".equals(getUserRole(adminUser))) {
                return createErrorResponse(chatId, messageId, "command.setFee.no_access");
            }

            if (!callbackData.startsWith("set_fee_")) {
                return createErrorResponse(chatId, messageId, "command.setFee.error");
            }

            String feeValue = callbackData.substring("set_fee_".length());
            
            if ("manual".equals(feeValue)) {
                setFeeStateService.setState(chatId, SetFeeState.WAITING_MANUAL_FEE);
                return new EditMessageText(chatId, messageId, 
                        messageConverter.resolve("command.setFee.enter_manual"))
                        .parseMode(ParseMode.Markdown);
            }

            double fee = Double.parseDouble(feeValue);
            if (fee < 0 || fee > 100) {
                return createErrorResponse(chatId, messageId, "command.setFee.invalid_value");
            }

            settingsService.setGlobalConversionFee(fee);
            setFeeStateService.clearState(chatId);

            String successMessage = messageConverter.resolve("command.setFee.success", 
                    Map.of("fee", String.valueOf(fee)));

            return new EditMessageText(chatId, messageId, successMessage)
                    .parseMode(ParseMode.Markdown);

        } catch (NumberFormatException e) {
            return createErrorResponse(chatId, messageId, "command.setFee.invalid_value");
        } catch (Exception e) {
            return createErrorResponse(chatId, messageId, "command.setFee.error");
        }
    }

    private EditMessageText createErrorResponse(Long chatId, Integer messageId, String errorMessageKey) {
        String errorMessage = messageConverter.resolve(errorMessageKey);
        return new EditMessageText(chatId, messageId, errorMessage);
    }

    private String getUserRole(User user) {
        return user.getRoles().stream()
                .map(userRole -> userRole.getRole().getRoleName().replace("ROLE_", ""))
                .findFirst().orElse("USER");
    }
} 