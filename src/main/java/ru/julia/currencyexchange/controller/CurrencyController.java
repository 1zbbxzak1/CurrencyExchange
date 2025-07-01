package ru.julia.currencyexchange.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.julia.currencyexchange.application.dto.common.ApiResponseDto;
import ru.julia.currencyexchange.application.dto.currency.ConvertRequest;
import ru.julia.currencyexchange.application.dto.currency.CurrencyConversionResponse;
import ru.julia.currencyexchange.application.dto.currency.CurrencyHistoryRequest;
import ru.julia.currencyexchange.application.dto.currency.CurrencyResponse;
import ru.julia.currencyexchange.application.service.CurrencyExchangeService;
import ru.julia.currencyexchange.application.service.UserService;
import ru.julia.currencyexchange.application.util.DtoMapper;
import ru.julia.currencyexchange.application.util.ValidationUtil;
import ru.julia.currencyexchange.domain.model.Currency;
import ru.julia.currencyexchange.domain.model.CurrencyConversion;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/currency")
@Validated
@Tag(name = "Currency Controller", description = "API для работы с валютными операциями")
public class CurrencyController {
    private final CurrencyExchangeService converterService;
    private final UserService userService;

    public CurrencyController(CurrencyExchangeService converterService, UserService userService) {
        this.converterService = converterService;
        this.userService = userService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    @Operation(summary = "Обновить курсы валют", description = "Получает актуальные курсы валют с внешнего API")
    @ApiResponse(responseCode = "200", description = "Курсы валют успешно обновлены")
    public ResponseEntity<ApiResponseDto<List<CurrencyResponse>>> updateCurrencyRates(
            @Parameter(description = "Chat ID пользователя Telegram", example = "123456789")
            @RequestParam Long chatId,
            @Parameter(description = "Username пользователя Telegram", example = "telegram_user")
            @RequestParam(required = false) String username) {
        ValidationUtil.validateChatId(chatId);
        ValidationUtil.validateUsername(username);

        userService.updateUsernameIfChanged(chatId, username);

        String userId = userService.getUserIdByChatId(chatId);

        List<Currency> currencies = converterService.updateCurrencyRates(userId);

        List<CurrencyResponse> currencyResponses = currencies.stream()
                .map(DtoMapper::mapToCurrencyResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponseDto.success("Курсы валют успешно обновлены", currencyResponses));
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/convert")
    @Operation(summary = "Конвертировать валюту", description = "Конвертирует указанную сумму из одной валюты в другую")
    @ApiResponse(responseCode = "200", description = "Конвертация выполнена успешно")
    public ResponseEntity<ApiResponseDto<CurrencyConversionResponse>> convertCurrency(
            @Parameter(description = "Chat ID пользователя Telegram", example = "123456789")
            @RequestParam Long chatId,
            @Parameter(description = "Username пользователя Telegram", example = "telegram_user")
            @RequestParam String username,
            @Valid @RequestBody ConvertRequest request) {
        ValidationUtil.validateChatId(chatId);
        ValidationUtil.validateUsername(username);

        userService.updateUsernameIfChanged(chatId, username);
        String userId = userService.getUserIdByChatId(chatId);

        CurrencyConversion conversion = converterService.convert(
                userId,
                request.getFromCurrency().toUpperCase(),
                request.getToCurrency().toUpperCase(),
                request.getAmount()
        );
        CurrencyConversionResponse response = DtoMapper.mapToCurrencyConversionResponse(conversion);

        return ResponseEntity.ok(ApiResponseDto.success("Конвертация выполнена успешно", response));
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/history")
    @Operation(summary = "История конвертаций пользователя", description = "Получает историю всех конвертаций для указанного пользователя")
    @ApiResponse(responseCode = "200", description = "История конвертаций получена")
    public ResponseEntity<ApiResponseDto<List<CurrencyConversionResponse>>> getUserHistory(
            @Parameter(description = "Chat ID пользователя Telegram", example = "123456789")
            @RequestParam Long chatId,
            @Parameter(description = "Username пользователя Telegram", example = "telegram_user")
            @RequestParam String username) {
        ValidationUtil.validateChatId(chatId);
        ValidationUtil.validateUsername(username);

        userService.updateUsernameIfChanged(chatId, username);
        String userId = userService.getUserIdByChatId(chatId);

        List<CurrencyConversion> conversions = converterService.getUserHistory(userId);

        List<CurrencyConversionResponse> responses = conversions.stream()
                .map(DtoMapper::mapToCurrencyConversionResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponseDto.success("История конвертаций получена", responses));
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/history/find")
    @Operation(summary = "Поиск конвертаций по дате", description = "Находит конвертаций по дате для пользователя")
    @ApiResponse(responseCode = "200", description = "Конвертации найдены")
    public ResponseEntity<ApiResponseDto<List<CurrencyConversionResponse>>> findByCurrencyDate(
            @Parameter(description = "Chat ID пользователя Telegram", example = "123456789")
            @RequestParam Long chatId,
            @Parameter(description = "Username пользователя Telegram", example = "telegram_user")
            @RequestParam String username,
            @Valid @RequestBody CurrencyHistoryRequest request) {
        ValidationUtil.validateChatId(chatId);
        ValidationUtil.validateUsername(username);

        userService.updateUsernameIfChanged(chatId, username);

        ValidationUtil.validateTimestamp(request.getTimestamp());
        String userId = userService.getUserIdByChatId(chatId);

        List<CurrencyConversion> conversions = converterService.findByCurrencyDate(
                userId,
                request.getTimestamp()
        );

        List<CurrencyConversionResponse> responses = conversions.stream()
                .map(DtoMapper::mapToCurrencyConversionResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponseDto.success("Конвертации найдены", responses));
    }

    @PreAuthorize("permitAll()")
    @GetMapping("/all")
    @Operation(summary = "Получить все валюты", description = "Возвращает список всех валют")
    @ApiResponse(responseCode = "200", description = "Список валют получен")
    public ResponseEntity<ApiResponseDto<List<CurrencyResponse>>> getAllCurrencies() {
        List<Currency> currencies = converterService.getAllCurrencies();

        List<CurrencyResponse> currencyResponses = currencies.stream()
                .map(DtoMapper::mapToCurrencyResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponseDto.success("Список валют получен", currencyResponses));
    }

    @PreAuthorize("permitAll()")
    @GetMapping("/to-rub")
    @Operation(summary = "Получить курс валюты к рублю", description = "Возвращает курс указанной валюты к рублю (RUB)")
    @ApiResponse(responseCode = "200", description = "Курс валюты к рублю получен")
    public ResponseEntity<ApiResponseDto<CurrencyResponse>> getCurrencyToRub(
            @Parameter(description = "Код валюты", example = "USD")
            @RequestParam String currencyCode) {
        Currency currency = converterService.getCurrencyByCode(currencyCode.toUpperCase());
        Currency rub = converterService.getCurrencyByCode("RUB");

        if (currency == null || rub == null) {
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Валюта не найдена", 404));
        }

        CurrencyResponse response = DtoMapper.mapToCurrencyResponse(currency);

        response.setExchangeRate(currency.getExchangeRate().divide(rub.getExchangeRate(), 6, java.math.RoundingMode.HALF_UP));
        response.setCode(currency.getCode());
        response.setName(currency.getName());

        return ResponseEntity.ok(ApiResponseDto.success("Курс валюты к рублю получен", response));
    }
}
