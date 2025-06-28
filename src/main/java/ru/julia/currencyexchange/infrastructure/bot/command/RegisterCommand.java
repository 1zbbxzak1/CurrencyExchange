package ru.julia.currencyexchange.infrastructure.bot.command;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.springframework.stereotype.Component;
import ru.julia.currencyexchange.application.bot.messages.converter.interfaces.MessageConverter;
import ru.julia.currencyexchange.application.bot.settings.enums.RegistrationState;
import ru.julia.currencyexchange.application.service.AuthService;
import ru.julia.currencyexchange.application.service.UserService;
import ru.julia.currencyexchange.application.service.bot.RegistrationStateService;
import ru.julia.currencyexchange.infrastructure.bot.command.abstracts.AbstractCommandHandler;

import java.util.regex.Pattern;

@Component
public class RegisterCommand extends AbstractCommandHandler {
    private final AuthService authService;
    private final UserService userService;
    private final RegistrationStateService registrationStateService;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    public RegisterCommand(MessageConverter messageConverter, AuthService authService,
                           UserService userService, RegistrationStateService registrationStateService) {
        super(messageConverter);
        this.authService = authService;
        this.userService = userService;
        this.registrationStateService = registrationStateService;
    }

    @Override
    public SendMessage handle(Update update) {
        Long chatId = update.message().chat().id();
        String text = update.message().text();
        String username = update.message().chat().username();

        if (text.equals("/register")) {
            registrationStateService.setState(chatId, RegistrationState.WAITING_EMAIL);
            return new SendMessage(chatId, messageConverter.resolve("command.register.email_prompt"));
        }

        RegistrationState currentState = registrationStateService.getState(chatId);

        switch (currentState) {
            case WAITING_EMAIL:
                return handleEmailInput(chatId, text, username);
            case WAITING_PASSWORD:
                return handlePasswordInput(chatId, text, username);
            case WAITING_VERIFICATION_CODE:
                return handleVerificationCodeInput(chatId, text, username);
            default:
                return null;
        }
    }

    private SendMessage handleEmailInput(Long chatId, String email, String username) {
        if (!isValidEmail(email)) {
            return new SendMessage(chatId, messageConverter.resolve("command.register.invalid_email"));
        }

        registrationStateService.setEmail(chatId, email);
        registrationStateService.setState(chatId, RegistrationState.WAITING_PASSWORD);
        return new SendMessage(chatId, messageConverter.resolve("command.register.password_prompt"));
    }

    private SendMessage handlePasswordInput(Long chatId, String password, String username) {
        if (password.length() < 6) {
            return new SendMessage(chatId, messageConverter.resolve("command.register.password_too_short"));
        }

        registrationStateService.setPassword(chatId, password);

        try {
            if (userService.existsByChatId(chatId)) {
                registrationStateService.clearData(chatId);
                return new SendMessage(chatId, messageConverter.resolve("command.register.error"));
            }

            authService.createUserWithVerificationCode(chatId, username,
                    registrationStateService.getData(chatId).getEmail(), password);

            registrationStateService.setState(chatId, RegistrationState.WAITING_VERIFICATION_CODE);
            return new SendMessage(chatId, messageConverter.resolve("command.register.code_sent"));
        } catch (Exception e) {
            registrationStateService.clearData(chatId);
            String errorMessage = messageConverter.resolve("command.register.error") + ": " + e.getMessage();
            System.err.println("Registration error for chatId " + chatId + ": " + e.getMessage());
            e.printStackTrace();
            return new SendMessage(chatId, errorMessage);
        }
    }

    private SendMessage handleVerificationCodeInput(Long chatId, String code, String username) {
        try {
            boolean verified = userService.verifyUserCode(chatId,
                    registrationStateService.getData(chatId).getEmail(), code);

            if (verified) {
                registrationStateService.clearData(chatId);
                return new SendMessage(chatId, messageConverter.resolve("command.register.success"));
            } else {
                return new SendMessage(chatId, messageConverter.resolve("command.register.invalid_code"));
            }
        } catch (Exception e) {
            registrationStateService.clearData(chatId);
            return new SendMessage(chatId, messageConverter.resolve("command.register.error"));
        }
    }

    private boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    @Override
    public boolean matches(Update update) {
        if (update.message() == null || update.message().text() == null) {
            return false;
        }

        String text = update.message().text();

        if (text.equals("/register")) {
            return true;
        }

        Long chatId = update.message().chat().id();
        return registrationStateService.getState(chatId) != RegistrationState.NONE;
    }

    @Override
    public String getCommand() {
        return "/register";
    }

    @Override
    public String getDescription() {
        return "command.register.description";
    }
} 