package ru.julia.currencyexchange.application.service.emails;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

class EmailServiceTest {
    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void sendVerificationCode_sendsCorrectMessage() {
        String toEmail = "test@example.com";
        String code = "123456";

        emailService.sendVerificationCode(toEmail, code);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        SimpleMailMessage sent = captor.getValue();
        assertThat(sent.getTo()).containsExactly(toEmail);
        assertThat(sent.getSubject()).isEqualTo("Код подтверждения регистрации");
        assertThat(sent.getText()).contains(code);
    }

    @Test
    void sendVerificationCode_mailSenderThrows_exceptionPropagates() {
        String toEmail = "fail@example.com";
        String code = "fail";
        Mockito.doThrow(new RuntimeException("Mail error"))
                .when(mailSender).send(Mockito.any(SimpleMailMessage.class));

        Assertions.assertThrows(RuntimeException.class, () -> {
            emailService.sendVerificationCode(toEmail, code);
        });
    }
} 