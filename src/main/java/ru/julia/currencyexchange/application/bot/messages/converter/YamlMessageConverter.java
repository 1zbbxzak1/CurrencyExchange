package ru.julia.currencyexchange.application.bot.messages.converter;

import org.springframework.context.NoSuchMessageException;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Component;
import ru.julia.currencyexchange.application.bot.messages.converter.interfaces.MessageConverter;

import java.util.Locale;
import java.util.Map;

@Component
public class YamlMessageConverter implements MessageConverter {
    private final ResourceBundleMessageSource messageSource;

    public YamlMessageConverter(ResourceBundleMessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    public String resolve(String id, Map<String, String> params) {
        try {
            String message = messageSource.getMessage(id, null, Locale.of("ru"));

            return replacePlaceholders(message, params);
        } catch (NoSuchMessageException e) {
            return id;
        }
    }

    private String replacePlaceholders(String message, Map<String, String> params) {
        String PLACEHOLDER_SYMBOL = "%";

        // Заменяем все плейсхолдеры в формате %ключ% на соответствующие значения из карты
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String placeholder = PLACEHOLDER_SYMBOL + entry.getKey() + PLACEHOLDER_SYMBOL;
            message = message.replace(placeholder, entry.getValue());
        }
        return message;
    }
}