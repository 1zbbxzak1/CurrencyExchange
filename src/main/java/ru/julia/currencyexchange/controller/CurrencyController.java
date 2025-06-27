package ru.julia.currencyexchange.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.julia.currencyexchange.application.dto.common.ApiResponseDto;
import ru.julia.currencyexchange.application.dto.currency.ConvertRequest;
import ru.julia.currencyexchange.application.dto.currency.CurrencyConversionResponse;
import ru.julia.currencyexchange.application.dto.currency.CurrencyResponse;
import ru.julia.currencyexchange.application.service.CurrencyExchangeService;
import ru.julia.currencyexchange.application.util.DtoMapper;
import ru.julia.currencyexchange.application.util.ValidationUtil;
import ru.julia.currencyexchange.domain.model.Currency;
import ru.julia.currencyexchange.domain.model.CurrencyConversion;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/currency")
@Validated
@Tag(name = "Currency Controller", description = "API для работы с валютными операциями")
public class CurrencyController {
    private final CurrencyExchangeService converterService;

    public CurrencyController(CurrencyExchangeService converterService) {
        this.converterService = converterService;
    }

    @GetMapping
    @Operation(summary = "Обновить курсы валют", description = "Получает актуальные курсы валют с внешнего API")
    @ApiResponse(responseCode = "200", description = "Курсы валют успешно обновлены")
    public ResponseEntity<ApiResponseDto<List<CurrencyResponse>>> updateCurrencyRates() {
        List<Currency> currencies = converterService.updateCurrencyRates();
        List<CurrencyResponse> currencyResponses = currencies.stream()
                .map(DtoMapper::mapToCurrencyResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponseDto.success("Курсы валют успешно обновлены", currencyResponses));
    }

    @PostMapping("/convert")
    @Operation(summary = "Конвертировать валюту", description = "Конвертирует указанную сумму из одной валюты в другую")
    @ApiResponse(responseCode = "200", description = "Конвертация выполнена успешно")
    public ResponseEntity<ApiResponseDto<CurrencyConversionResponse>> convertCurrency(
            @Parameter(description = "ID пользователя", example = "550e8400-e29b-41d4-a716-446655440000")
            @RequestParam String userId,
            @Valid @RequestBody ConvertRequest request) {
        
        ValidationUtil.validateUserId(userId);
        
        CurrencyConversion conversion = converterService.convert(
                userId,
                request.getFromCurrency().toUpperCase(),
                request.getToCurrency().toUpperCase(),
                request.getAmount()
        );

        CurrencyConversionResponse response = DtoMapper.mapToCurrencyConversionResponse(conversion);
        return ResponseEntity.ok(ApiResponseDto.success("Конвертация выполнена успешно", response));
    }

    @GetMapping("/history")
    @Operation(summary = "История конвертаций пользователя", description = "Получает историю всех конвертаций для указанного пользователя")
    @ApiResponse(responseCode = "200", description = "История конвертаций получена")
    public ResponseEntity<ApiResponseDto<List<CurrencyConversionResponse>>> getUserHistory(
            @Parameter(description = "ID пользователя", example = "550e8400-e29b-41d4-a716-446655440000")
            @RequestParam String userId) {
        
        ValidationUtil.validateUserId(userId);
        
        List<CurrencyConversion> conversions = converterService.getUserHistory(userId);
        List<CurrencyConversionResponse> responses = conversions.stream()
                .map(DtoMapper::mapToCurrencyConversionResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponseDto.success("История конвертаций получена", responses));
    }

    @GetMapping("/history/find")
    @Operation(summary = "Поиск конвертаций по валюте и дате", description = "Находит конвертаций по коду валюты и дате")
    @ApiResponse(responseCode = "200", description = "Конвертации найдены")
    public ResponseEntity<ApiResponseDto<List<CurrencyConversionResponse>>> findByCurrencyCodeAndDate(
            @Parameter(description = "Код валюты", example = "USD")
            @RequestParam String currencyCode,
            @Parameter(description = "Дата в формате YYYY-MM-DD", example = "2024-01-01")
            @RequestParam String timestamp) {
        
        ValidationUtil.validateCurrencyCode(currencyCode);
        ValidationUtil.validateTimestamp(timestamp);
        
        List<CurrencyConversion> conversions = converterService.findByCurrencyCodeAndDate(currencyCode.toUpperCase(), timestamp);
        List<CurrencyConversionResponse> responses = conversions.stream()
                .map(DtoMapper::mapToCurrencyConversionResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponseDto.success("Конвертации найдены", responses));
    }
}
