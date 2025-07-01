package ru.julia.currencyexchange.infrastructure.bot.command.builder;

import org.springframework.stereotype.Component;
import ru.julia.currencyexchange.application.bot.messages.converter.interfaces.MessageConverter;
import ru.julia.currencyexchange.domain.model.User;
import ru.julia.currencyexchange.infrastructure.configuration.Constants;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserMessageBuilder {
    private final MessageConverter messageConverter;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public UserMessageBuilder(MessageConverter messageConverter) {
        this.messageConverter = messageConverter;
    }

    public String buildUsersMessage(List<User> users, int page, boolean useCompactFormat, int usersPerPage) {
        StringBuilder message = new StringBuilder();
        message.append(messageConverter.resolve("command.users.title"))
                .append(Constants.LINE_SEPARATOR)
                .append(Constants.LINE_SEPARATOR);

        if (useCompactFormat) {
            message.append(messageConverter.resolve("command.users.pagination.compact_mode"))
                    .append(Constants.LINE_SEPARATOR)
                    .append(Constants.LINE_SEPARATOR);
        }

        int startIndex = page * usersPerPage;
        int endIndex = Math.min(startIndex + usersPerPage, users.size());
        int totalPages = (users.size() - 1) / usersPerPage + 1;
        for (int i = startIndex; i < endIndex; i++) {
            User user = users.get(i);
            if (useCompactFormat) {
                message.append(messageConverter.resolve("command.users.user_format_compact",
                        java.util.Map.of(
                                "username", user.getUsername(),
                                "email", user.getEmail(),
                                "chatId", String.valueOf(user.getChatId()),
                                "status", getAccountStatus(user)
                        )));
            } else {
                message.append(messageConverter.resolve("command.users.user_format_full",
                        java.util.Map.of(
                                "username", user.getUsername(),
                                "email", user.getEmail(),
                                "id", user.getId(),
                                "chatId", String.valueOf(user.getChatId()),
                                "roles", formatRoles(user),
                                "createdAt", formatDate(user.getCreatedAt()),
                                "verified", formatBoolean(user.isVerified()),
                                "banned", formatBoolean(user.isBanned()),
                                "deleted", formatBoolean(user.isDeleted())
                        )));
            }
            message.append(Constants.LINE_SEPARATOR);
            message.append(Constants.LINE_SEPARATOR);
        }
        message.append(Constants.LINE_SEPARATOR).append(Constants.LINE_SEPARATOR);
        message.append(messageConverter.resolve("command.users.pagination.page_info",
                java.util.Map.of("current", String.valueOf(page + 1), "total", String.valueOf(totalPages))));
        message.append(" | ").append(messageConverter.resolve("command.users.pagination.total_users",
                java.util.Map.of("count", String.valueOf(users.size()))));
        return message.toString();
    }

    private String getAccountStatus(User user) {
        if (user.isDeleted()) {
            return "🗑️ Удален";
        } else if (user.isBanned()) {
            return "🚫 Заблокирован";
        } else if (!user.isVerified()) {
            return "⏳ Не верифицирован";
        } else {
            return "✅ Рабочий";
        }
    }

    private String formatRoles(User user) {
        return user.getRoles().stream()
                .map(userRole -> userRole.getRole().getRoleName().replace("ROLE_", ""))
                .collect(Collectors.joining(", "));
    }

    private String formatDate(java.time.LocalDateTime dateTime) {
        return dateTime.format(DATE_FORMATTER);
    }

    private String formatBoolean(boolean value) {
        return value ? "Да" : "Нет";
    }
} 