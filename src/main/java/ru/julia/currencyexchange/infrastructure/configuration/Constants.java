package ru.julia.currencyexchange.infrastructure.configuration;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Set;

public class Constants {
    
    public static final String LINE_SEPARATOR = System.lineSeparator();
    public static final String PLACEHOLDER_SYMBOL = "%";

    public static final Set<String> POPULAR_CURRENCIES = Set.of("USD", "EUR", "GBP", "JPY", "CNY", "CHF", "CAD", "AUD", "RUB");
    public static final BigDecimal MAX_AMOUNT = new BigDecimal("999999999");
    public static final int CONVERSION_SCALE = 4;
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    public static final int POPULAR_BUTTONS_PER_ROW = 4;
    public static final int ALL_BUTTONS_PER_ROW = 3;
    public static final String[] AMOUNT_VALUES_ROW1 = {"1", "10", "100", "1000"};
    public static final String[] AMOUNT_VALUES_ROW2 = {"0.1", "0.5", "5", "50"};

    public static final int CURRENCIES_PER_PAGE = 10;
    public static final int CURRENCIES_PER_PAGE_COMPACT = 15;

    public static final String CONVERT_PREFIX = "convert_";
    public static final String CURRENCY_PREFIX = "currency_";
    public static final String FROM_SHOW_ALL = "from_show_all";
    public static final String TO_SHOW_ALL_PREFIX = "to_show_all_";
    public static final String FROM_SHOW_POPULAR = "from_show_popular";
    public static final String TO_SHOW_POPULAR_PREFIX = "to_show_popular_";
    public static final String AMOUNT_PREFIX = "amount_";
    public static final String MANUAL_AMOUNT_PREFIX = "manual_amount_";
    public static final String BACK_TO_CURRENCY_SELECTION = "back_to_currency_selection";
    public static final String BACK_TO_SELECTION = "back_to_selection";
    public static final String FALLBACK_CURRENCY = "USD";

    public static final int DEFAULT_CONVERSIONS_PER_PAGE = 5;
    public static final int COMPACT_CONVERSIONS_PER_PAGE = 8;
    public static final int COMPACT_FORMAT_THRESHOLD = 20;
}
