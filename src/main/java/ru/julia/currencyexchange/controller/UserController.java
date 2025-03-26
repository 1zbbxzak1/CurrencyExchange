package ru.julia.currencyexchange.controller;

import org.springframework.web.bind.annotation.*;
import ru.julia.currencyexchange.entity.User;
import ru.julia.currencyexchange.service.TransactionalUserService;

@RestController
@RequestMapping("api/user")
public class UserController {
    private final TransactionalUserService transactionalUserService;

    public UserController(TransactionalUserService transactionalUserService) {
        this.transactionalUserService = transactionalUserService;
    }

    @PostMapping("/create")
    public User createUserWithSettings(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String preferredCurrency) {
        return transactionalUserService.createUserWithSettings(username, password, preferredCurrency);
    }

    @GetMapping("/{id}")
    public User findUserById(@PathVariable String id) {
        return transactionalUserService.findUserById(id);
    }

    @DeleteMapping("/{id}")
    public User deleteUser(@PathVariable String id) {
        return transactionalUserService.deleteUser(id);
    }
}
