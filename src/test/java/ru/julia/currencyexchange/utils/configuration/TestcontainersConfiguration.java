package ru.julia.currencyexchange.utils.configuration;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.springframework.mail.javamail.JavaMailSender;
import static org.mockito.Mockito.mock;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {
    @Bean
    public DatabaseCleaner databaseCleaner() {
        return new DatabaseCleaner();
    }

    @Bean
    @ServiceConnection
    PostgreSQLContainer<?> postgresContainer() {
        return new PostgreSQLContainer<>("postgres:17-alpine")
                .withExposedPorts(5432)
                .withDatabaseName("local")
                .withUsername("postgres")
                .withPassword("test")
                .withReuse(true);
    }

    @Bean
    public JavaMailSender javaMailSender() {
        return mock(JavaMailSender.class);
    }
}
