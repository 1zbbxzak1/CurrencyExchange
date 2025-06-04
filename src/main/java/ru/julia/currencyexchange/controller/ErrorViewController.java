package ru.julia.currencyexchange.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ErrorViewController implements ErrorController {
    @RequestMapping("/view/error")
    public String handleError() {
        return "error";
    }
}
