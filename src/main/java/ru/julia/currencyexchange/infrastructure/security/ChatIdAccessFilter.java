package ru.julia.currencyexchange.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.julia.currencyexchange.application.service.UserService;
import ru.julia.currencyexchange.domain.model.User;

import java.io.IOException;

@Component
public class ChatIdAccessFilter extends OncePerRequestFilter {
    private final UserService userService;

    public ChatIdAccessFilter(UserService userService) {
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String chatIdParam = request.getParameter("chatId");
        if (chatIdParam != null) {
            try {
                Long chatId = Long.valueOf(chatIdParam);
                User user = userService.findUserByChatId(chatId);
                if (!user.isVerified() || user.isBanned()) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\": \"User is not verified or is banned\"}");
                    return;
                }
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"User not found or invalid chatId\"}");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
} 