package ru.julia.currencyexchange.controller;

import org.springframework.web.bind.annotation.*;
import ru.julia.currencyexchange.application.service.UserService;
import ru.julia.currencyexchange.domain.model.User;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<User> getAllUsers() {
        return userService.findAllUsers();
    }

    @GetMapping("/{id}")
    public User findUserById(@PathVariable String id) {
        return userService.findUserById(id);
    }

    @DeleteMapping("/{id}")
    public User deleteUserById(@PathVariable String id) {
        return userService.deleteUserById(id);
    }
}
