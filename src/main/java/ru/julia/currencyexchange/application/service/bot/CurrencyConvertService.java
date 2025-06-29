package ru.julia.currencyexchange.application.service.bot;

import org.springframework.stereotype.Service;
import ru.julia.currencyexchange.application.bot.messages.converter.interfaces.MessageConverter;
import ru.julia.currencyexchange.application.bot.settings.enums.ConversionState;
import ru.julia.currencyexchange.application.service.CurrencyExchangeService;
import ru.julia.currencyexchange.application.service.SettingsService;
import ru.julia.currencyexchange.application.service.UserService;
import ru.julia.currencyexchange.domain.model.Currency;
import ru.julia.currencyexchange.domain.model.CurrencyConversion;
import ru.julia.currencyexchange.domain.model.User;
import ru.julia.currencyexchange.infrastructure.bot.command.utils.CurrencyEmojiUtils;
import ru.julia.currencyexchange.infrastructure.bot.command.utils.CurrencyFormatUtils;
import ru.julia.currencyexchange.infrastructure.configuration.Constants;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CurrencyConvertService {
    private final CurrencyExchangeService currencyExchangeService;
    private final UserService userService;
    private final SettingsService settingsService;
    private final CurrencyEmojiUtils currencyEmojiUtils;
    private final CurrencyFormatUtils currencyFormatUtils;
    private final MessageConverter messageConverter;
    
    private final Map<Long, ConversionState> userStates = new ConcurrentHashMap<>();
    private final Map<Long, ConversionData> userData = new ConcurrentHashMap<>();

    public CurrencyConvertService(CurrencyExchangeService currencyExchangeService,
                                  UserService userService,
                                  SettingsService settingsService,
                                  CurrencyEmojiUtils currencyEmojiUtils,
                                  CurrencyFormatUtils currencyFormatUtils,
                                  MessageConverter messageConverter) {
        this.currencyExchangeService = currencyExchangeService;
        this.userService = userService;
        this.settingsService = settingsService;
        this.currencyEmojiUtils = currencyEmojiUtils;
        this.currencyFormatUtils = currencyFormatUtils;
        this.messageConverter = messageConverter;
    }

    public boolean hasCurrencies() {
        return !getAllCurrencies().isEmpty();
    }

    public List<Currency> getAllCurrencies() {
        return currencyExchangeService.getAllCurrencies();
    }

    public List<Currency> getPopularCurrencies() {
        return getAllCurrencies().stream()
                .filter(currency -> Constants.POPULAR_CURRENCIES.contains(currency.getCode()))
                .toList();
    }

    public CurrencyConversion convertCurrency(Long chatId, String fromCurrencyCode, String toCurrencyCode, BigDecimal amount) {
        User user = userService.findUserByChatId(chatId);
        if (user == null) {
            throw new IllegalArgumentException(messageConverter.resolve("command.convert.validation.user_not_found"));
        }

        return currencyExchangeService.convert(user.getId().toString(), fromCurrencyCode, toCurrencyCode, amount);
    }

    public Currency getCurrencyByCode(String currencyCode) {
        return currencyExchangeService.getCurrencyByCode(currencyCode);
    }

    public String buildConversionMessage(CurrencyConversion conversion) {
        Currency fromCurrency = conversion.getSourceCurrency();
        Currency toCurrency = conversion.getTargetCurrency();
        double feePercent = settingsService.getGlobalConversionFeePercent();

        String message = messageConverter.resolve("command.convert.result.title") + Constants.LINE_SEPARATOR +
                Constants.LINE_SEPARATOR +
                messageConverter.resolve("command.convert.result.from",
                        Map.of("emoji", currencyEmojiUtils.getCurrencyEmoji(fromCurrency.getCode()),
                                "code", fromCurrency.getCode(),
                                "name", fromCurrency.getName())) +
                Constants.LINE_SEPARATOR +
                messageConverter.resolve("command.convert.result.amount",
                        Map.of("amount", currencyFormatUtils.formatAmount(conversion.getAmount()),
                                "code", fromCurrency.getCode(),
                                "rate", currencyFormatUtils.formatExchangeRate(fromCurrency.getExchangeRate()))) +
                Constants.LINE_SEPARATOR +
                Constants.LINE_SEPARATOR +
                messageConverter.resolve("command.convert.result.to",
                        Map.of("emoji", currencyEmojiUtils.getCurrencyEmoji(toCurrency.getCode()),
                                "code", toCurrency.getCode(),
                                "name", toCurrency.getName())) +
                Constants.LINE_SEPARATOR +
                messageConverter.resolve("command.convert.result.rate",
                        Map.of("code", toCurrency.getCode(),
                                "rate", currencyFormatUtils.formatExchangeRate(toCurrency.getExchangeRate()))) +
                Constants.LINE_SEPARATOR +
                Constants.LINE_SEPARATOR +
                messageConverter.resolve("command.convert.result.result",
                        Map.of("amount", currencyFormatUtils.formatAmount(conversion.getAmount()),
                                "from_code", fromCurrency.getCode(),
                                "result", currencyFormatUtils.formatAmount(conversion.getConvertedAmount()),
                                "to_code", toCurrency.getCode()));

        if (feePercent > 0) {
            message += Constants.LINE_SEPARATOR +
                    messageConverter.resolve("command.convert.result.fee",
                            Map.of("fee", String.valueOf(feePercent)));
        }

        message += Constants.LINE_SEPARATOR +
                Constants.LINE_SEPARATOR +
                messageConverter.resolve("command.convert.result.updated",
                        Map.of("date", fromCurrency.getLastUpdated().format(Constants.DATE_FORMATTER)));

        return message;
    }

    public boolean isRub(String currencyCode) {
        return "RUB".equalsIgnoreCase(currencyCode);
    }

    public boolean isValidAmount(String amountStr) {
        try {
            BigDecimal amount = new BigDecimal(amountStr);
            return amount.compareTo(BigDecimal.ZERO) > 0 && amount.compareTo(Constants.MAX_AMOUNT) <= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public BigDecimal parseAmount(String amountStr) {
        return new BigDecimal(amountStr.replace(",", "."));
    }

    public ConversionState getState(Long chatId) {
        return userStates.getOrDefault(chatId, ConversionState.NONE);
    }

    public void setState(Long chatId, ConversionState state) {
        userStates.put(chatId, state);
    }

    public void setData(Long chatId, String fromCurrency, String toCurrency) {
        userData.put(chatId, new ConversionData(fromCurrency, toCurrency));
    }

    public ConversionData getData(Long chatId) {
        return userData.get(chatId);
    }

    public void clearData(Long chatId) {
        userStates.remove(chatId);
        userData.remove(chatId);
    }

    public record ConversionData(String fromCurrency, String toCurrency) {
    }
} 