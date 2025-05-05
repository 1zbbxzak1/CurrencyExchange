package ru.julia.currencyexchange.ui;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Sql(scripts = "/data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class AuthUITest {

    private static WebDriver driver;

    @BeforeAll
    static void setUp() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
    }

    @AfterAll
    static void tearDown() {
        driver.quit();
    }

    @Test
    @Order(1)
    void successfulLoginTest() {
        driver.get("http://localhost:8080/auth");

        WebElement usernameField = driver.findElement(By.name("username"));
        WebElement passwordField = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));

        usernameField.sendKeys("testuser");
        passwordField.sendKeys("encoded-password");
        loginButton.click();

        new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(d -> d.getPageSource().contains("Курсы валют"));

        assertTrue(driver.getPageSource().contains("Курсы валют"));
    }

    @Test
    @Order(2)
    void logoutTest() {
        WebElement logoutButton = driver.findElement(By.cssSelector("form[method='post'] button.logout-button"));
        logoutButton.click();

        assertTrue(driver.getPageSource().contains("Добро пожаловать в сервис обмена валют"));
    }
}
