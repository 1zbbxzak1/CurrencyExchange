package ru.julia.currencyexchange.utils.configuration;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;
import ru.julia.currencyexchange.application.service.emails.EmailService;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class TestMailConfig {
    @Bean
    public EmailService emailService() {
        return new EmailService(javaMailSender());
    }

    @Bean
    public JavaMailSender javaMailSender() {
        return mock(JavaMailSender.class);
    }
}
