package ru.julia.currencyexchange.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.julia.currencyexchange.application.dto.common.ApiResponseDto;
import ru.julia.currencyexchange.application.dto.user.UserResponse;
import ru.julia.currencyexchange.application.service.SettingsService;
import ru.julia.currencyexchange.application.service.UserService;
import ru.julia.currencyexchange.application.util.DtoMapper;
import ru.julia.currencyexchange.application.util.ValidationUtil;
import ru.julia.currencyexchange.domain.model.User;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User Controller", description = "API для работы с пользователями")
public class UserController {
    private final UserService userService;
    private final SettingsService settingsService;

    public UserController(UserService userService, SettingsService settingsService) {
        this.userService = userService;
        this.settingsService = settingsService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    @Operation(summary = "Получить всех пользователей", description = "Возвращает список всех зарегистрированных пользователей")
    @ApiResponse(responseCode = "200", description = "Список пользователей получен")
    public ResponseEntity<ApiResponseDto<List<UserResponse>>> getAllUsers(
            @Parameter(description = "Chat ID пользователя Telegram", example = "123456789")
            @RequestParam Long chatId,
            @Parameter(description = "Username пользователя Telegram", example = "telegram_user")
            @RequestParam(required = false) String username) {

        ValidationUtil.validateChatId(chatId);
        ValidationUtil.validateUsername(username);

        userService.updateUsernameIfChanged(chatId, username);

        String id = userService.getUserIdByChatId(chatId);
        List<User> users = userService.findAllUsers(id);

        List<UserResponse> userResponses = users.stream()
                .map(DtoMapper::mapToUserResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponseDto.success("Список пользователей получен", userResponses));
    }

    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/by-chat-id/{chatId}")
    @Operation(summary = "Удалить пользователя", description = "Удаляет пользователя по указанному ID")
    @ApiResponse(responseCode = "200", description = "Пользователь удален")
    @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    public ResponseEntity<ApiResponseDto<UserResponse>> deleteUserById(
            @Parameter(description = "Chat ID пользователя Telegram", example = "123456789")
            @PathVariable Long chatId,
            @Parameter(description = "Username пользователя Telegram", example = "telegram_user")
            @RequestParam(required = false) String username) {

        ValidationUtil.validateChatId(chatId);
        ValidationUtil.validateUsername(username);

        userService.updateUsernameIfChanged(chatId, username);

        String id = userService.getUserIdByChatId(chatId);

        User user = userService.deleteUserById(id);
        UserResponse userResponse = DtoMapper.mapToUserResponse(user);

        return ResponseEntity.ok(ApiResponseDto.success("Пользователь удален", userResponse));
    }

    @PreAuthorize("permitAll()")
    @GetMapping(value = "/role", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Получить роль пользователя по chatId", description = "Возвращает основную роль пользователя по chatId Telegram")
    public ResponseEntity<Map<String, String>> getUserRoleByChatId(
            @Parameter(description = "Chat ID пользователя Telegram", example = "123456789")
            @RequestParam Long chatId) {
        User user = userService.findUserByChatId(chatId);

        Set<String> roles = user.getRoles().stream()
                .map(userRole -> userRole.getRole().getRoleName().replace("ROLE_", ""))
                .collect(Collectors.toSet());

        // Приоритет: ADMIN > USER
        String role = roles.stream()
                .min(Comparator.comparing(r -> "ADMIN".equals(r) ? 0 : 1))
                .orElse("USER");

        Map<String, String> result = new HashMap<>();
        result.put("role", role);

        return ResponseEntity.ok(result);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/ban")
    @Operation(summary = "Заблокировать пользователя по email", description = "Админ блокирует пользователя по email. После этого пользователь не может выполнять запросы.")
    public ResponseEntity<ApiResponseDto<UserResponse>> banUserByEmail(
            @Parameter(description = "Chat ID пользователя Telegram", example = "123456789")
            @RequestParam Long chatId,
            @Parameter(description = "Username пользователя Telegram", example = "telegram_user")
            @RequestParam(required = false) String username,
            @Parameter(description = "Email пользователя", example = "user@example.com")
            @RequestParam String email) {
        ValidationUtil.validateChatId(chatId);
        ValidationUtil.validateUsername(username);

        userService.updateUsernameIfChanged(chatId, username);

        User user = userService.findUserByEmail(email);
        user.setBanned(true);
        userService.saveUser(user);

        UserResponse userResponse = DtoMapper.mapToUserResponse(user);

        return ResponseEntity.ok(ApiResponseDto.success("Пользователь заблокирован", userResponse));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/set-conversion-fee")
    @Operation(summary = "Установить глобальный процент конвертации валют", description = "Устанавливает общий процент комиссии для всех пользователей (только для администратора)")
    public ResponseEntity<ApiResponseDto<Double>> setGlobalConversionFee(
            @Parameter(description = "Chat ID пользователя Telegram", example = "123456789")
            @RequestParam Long chatId,
            @Parameter(description = "Username пользователя Telegram", example = "telegram_user")
            @RequestParam(required = false) String username,
            @Parameter(description = "Процент комиссии (например, 2.5)", example = "2.5")
            @RequestParam Double feePercent) {
        ValidationUtil.validateChatId(chatId);
        ValidationUtil.validateUsername(username);

        userService.updateUsernameIfChanged(chatId, username);

        settingsService.setGlobalConversionFee(feePercent);
        return ResponseEntity.ok(ApiResponseDto.success("Глобальный процент конвертации установлен", feePercent));
    }
}
