package ru.julia.currencyexchange.settings;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.julia.currencyexchange.dto.CurrencyConversion;
import ru.julia.currencyexchange.service.CurrencyExchangeService;

import java.util.List;
import java.util.Scanner;

@Configuration
public class StartSettings {
    @Autowired
    private CurrencyExchangeService converterService;

    @Bean
    public CommandLineRunner startConsole() {
        return args -> {
            Scanner scanner = new Scanner(System.in);
            System.out.println("Введите команду ('convert USD RUB 100', 'history' или 'exit'):");

            while (true) {
                System.out.print("> ");
                String input = scanner.nextLine();
                if ("exit".equalsIgnoreCase(input.trim())) break;

                String[] parts = input.split(" ");
                if (parts.length == 4 && "convert".equalsIgnoreCase(parts[0])) {
                    try {
                        double amount = Double.parseDouble(parts[3]);
                        double result = converterService.convert(parts[1], parts[2], amount);
                        System.out.println("Результат: " + result);
                    } catch (NumberFormatException e) {
                        System.out.println("Ошибка в формате числа.");
                    }
                } else if ("history".equalsIgnoreCase(input.trim())) {
                    List<CurrencyConversion> history = converterService.getConversionHistory();
                    if (history.isEmpty()) {
                        System.out.println("История пуста.");
                    } else {
                        System.out.println("История конвертаций:");
                        for (CurrencyConversion conversion : history) {
                            System.out.printf("[%d] %s -> %s: %.2f -> %.2f (Курс %s: %.4f, Время: %s)%n",
                                    conversion.getId(),
                                    conversion.getSourceCurrency(),
                                    conversion.getTargetCurrency(),
                                    conversion.getAmount(),
                                    conversion.getConvertedAmount(),
                                    conversion.getSourceCurrency(),
                                    conversion.getConversionRate(),
                                    conversion.getFormattedTimestamp());
                        }
                    }
                } else {
                    System.out.println("Неверная команда.");
                }
            }
        };
    }
}
