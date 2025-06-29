package ru.julia.currencyexchange.application.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Set;

@Schema(description = "Ответ с информацией о пользователе")
public class UserResponse {
    @Schema(description = "ID пользователя", example = "550e8400-e29b-41d4-a716-446655440000")
    private String id;
    
    @Schema(description = "Chat ID пользователя", example = "123456789")
    private Long chatId;
    
    @Schema(description = "Имя пользователя", example = "john_doe")
    private String username;
    
    @Schema(description = "Email пользователя", example = "user@example.com")
    private String email;
    
    @Schema(description = "Статус верификации", example = "true")
    private boolean verified;
    
    @Schema(description = "Статус блокировки", example = "false")
    private boolean banned;
    
    @Schema(description = "Роли пользователя", example = "[\"USER\", \"ADMIN\"]")
    private Set<String> roles;
    
    @Schema(description = "Дата создания", example = "2024-01-01T12:00:00")
    private LocalDateTime createdAt;

    public UserResponse() {
    }

    public UserResponse(String id, Long chatId, String username, String email, 
                       boolean verified, boolean banned, Set<String> roles, LocalDateTime createdAt) {
        this.id = id;
        this.chatId = chatId;
        this.username = username;
        this.email = email;
        this.verified = verified;
        this.banned = banned;
        this.roles = roles;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public boolean isBanned() {
        return banned;
    }

    public void setBanned(boolean banned) {
        this.banned = banned;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
} 