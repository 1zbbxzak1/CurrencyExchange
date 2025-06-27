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

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    @Operation(summary = "Получить всех пользователей", description = "Возвращает список всех зарегистрированных пользователей")
    @ApiResponse(responseCode = "200", description = "Список пользователей получен")
    public ResponseEntity<ApiResponseDto<List<UserResponse>>> getAllUsers() {
        List<User> users = userService.findAllUsers();

        List<UserResponse> userResponses = users.stream()
                .map(DtoMapper::mapToUserResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponseDto.success("Список пользователей получен", userResponses));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/by-chat-id/{chatId}")
    @Operation(summary = "Найти пользователя по chatId", description = "Возвращает пользователя по chatId Telegram")
    @ApiResponse(responseCode = "200", description = "Пользователь найден")
    @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    public ResponseEntity<ApiResponseDto<UserResponse>> findUserByChatId(
            @Parameter(description = "Chat ID пользователя Telegram", example = "123456789")
            @PathVariable Long chatId) {
        User user = userService.findUserByChatId(chatId);
        UserResponse userResponse = DtoMapper.mapToUserResponse(user);

        return ResponseEntity.ok(ApiResponseDto.success("Пользователь найден", userResponse));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    @Operation(summary = "Найти пользователя по ID", description = "Возвращает пользователя по указанному ID")
    @ApiResponse(responseCode = "200", description = "Пользователь найден")
    @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    public ResponseEntity<ApiResponseDto<UserResponse>> findUserById(
            @Parameter(description = "ID пользователя", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable String id) {

        ValidationUtil.validateUserId(id);

        User user = userService.findUserById(id);
        UserResponse userResponse = DtoMapper.mapToUserResponse(user);

        return ResponseEntity.ok(ApiResponseDto.success("Пользователь найден", userResponse));
    }

    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить пользователя", description = "Удаляет пользователя по указанному ID")
    @ApiResponse(responseCode = "200", description = "Пользователь удален")
    @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    public ResponseEntity<ApiResponseDto<UserResponse>> deleteUserById(
            @Parameter(description = "ID пользователя", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable String id) {

        ValidationUtil.validateUserId(id);

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
}
