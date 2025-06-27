package ru.julia.currencyexchange.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.julia.currencyexchange.application.exceptions.UserNotFoundException;
import ru.julia.currencyexchange.domain.model.User;
import ru.julia.currencyexchange.infrastructure.repository.jpa.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User findUserById(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("User id cannot be null or empty");
        }

        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User with ID " + id + " not found"));
    }

    public User findUserByChatId(Long chatId) {
        if (chatId == null) {
            throw new IllegalArgumentException("ChatId cannot be null");
        }
        return userRepository.findByChatId(chatId)
                .orElseThrow(() -> new UserNotFoundException("User with chatId " + chatId + " not found"));
    }

    public boolean existsByChatId(Long chatId) {
        if (chatId == null) {
            throw new IllegalArgumentException("ChatId cannot be null");
        }
        return userRepository.existsByChatId(chatId);
    }

    @Transactional
    public User deleteUserById(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("User id cannot be null or empty");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with ID " + userId + " not found"));

        userRepository.delete(user);
        return user;
    }

    public List<User> findAllUsers() {
        List<User> users = new ArrayList<>();
        userRepository.findAll().forEach(users::add);

        return users;
    }

    @Transactional
    public boolean verifyUserCode(String email, String code) {
        Optional<User> user = userRepository.findByEmail(email);

        if (user.isPresent() && user.get().getVerificationCode().equals(code)) {
            user.get().setVerified(true);
            userRepository.save(user.get());
            return true;
        }
        return false;
    }

    public String getUserIdByChatId(Long chatId) {
        return userRepository.findByChatId(chatId)
            .orElseThrow(() -> new UserNotFoundException("User with chatId " + chatId + " not found"))
            .getId();
    }

    public void updateUsernameIfChanged(Long chatId, String newUsername) {
        userRepository.findByChatId(chatId).ifPresent(user -> {
            if (!java.util.Objects.equals(user.getUsername(), newUsername)) {
                user.setUsername(newUsername);
                userRepository.save(user);
            }
        });
    }
}
