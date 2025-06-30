package ru.julia.currencyexchange.application.service.emails;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.openMocks;

class EmailServiceUnitTest {
    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        openMocks(this);
    }

    @Test
    @DisplayName("Отправка кода верификации: корректное сообщение")
    void sendVerificationCode_sendsCorrectMessage() {
        String toEmail = "test@example.com";
        String code = "123456";

        emailService.sendVerificationCode(toEmail, code);

        ArgumentCaptor<SimpleMailMessage> captor = forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        SimpleMailMessage sent = captor.getValue();
        assertThat(sent.getTo()).containsExactly(toEmail);
        assertThat(sent.getSubject()).isEqualTo("Код подтверждения регистрации");
        assertThat(sent.getText()).contains(code);
    }

    @Test
    @DisplayName("MailSender выбрасывает исключение: ошибка пробрасывается")
    void sendVerificationCode_mailSenderThrows_exceptionPropagates() {
        String toEmail = "fail@example.com";
        String code = "fail";
        doThrow(new RuntimeException("Mail error"))
                .when(mailSender).send(any(SimpleMailMessage.class));

        assertThrows(RuntimeException.class, () -> {
            emailService.sendVerificationCode(toEmail, code);
        });
    }
} 