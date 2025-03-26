package ru.julia.currencyexchange.controller;

import org.springframework.web.bind.annotation.*;
import ru.julia.currencyexchange.entity.User;
import ru.julia.currencyexchange.service.TransactionalUserService;

import java.util.List;

@RestController
@RequestMapping("/users")
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

    @GetMapping
    public List<User> getAllUsers() {
        return transactionalUserService.findAllUsers();
    }

    @GetMapping("/{id}")
    public User findUserById(@PathVariable String id) {
        return transactionalUserService.findUserById(id);
    }

    @DeleteMapping("/{id}")
    public User deleteUserById(@PathVariable String id) {
        return transactionalUserService.deleteUserById(id);
    }
}
