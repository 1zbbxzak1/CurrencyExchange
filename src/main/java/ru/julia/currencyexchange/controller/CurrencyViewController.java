package ru.julia.currencyexchange.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.julia.currencyexchange.domain.model.Currency;
import ru.julia.currencyexchange.infrastructure.repository.jpa.CurrencyRepository;

@Controller
@RequestMapping("/currency-rates")
public class CurrencyViewController {

    @Autowired
    CurrencyRepository currencyRepository;

    @GetMapping("/list")
    public String viewCurrency(Model model) {
        Iterable<Currency> rates = currencyRepository.findAll();
        model.addAttribute("currencies", rates);

        return "list";
    }
}
