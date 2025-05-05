package ru.julia.currencyexchange.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Sql(scripts = "/data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class AuthControllerTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }

    @Test
    @Order(1)
    void registerUserSuccessfully() {
        String username = "newUser";
        String password = "newPassword123";
        String preferredCurrency = "USD";

        given()
                .contentType(ContentType.URLENC)
                .formParam("username", username)
                .formParam("password", password)
                .formParam("preferredCurrency", preferredCurrency)
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("username", equalTo(username))
                .body("password", not(equalTo(password)))
                .body("settings.preferredCurrency.code", equalTo(preferredCurrency));
    }

    @Test
    @Order(2)
    void registerUserWithoutRequiredParams() {
        given()
                .contentType(ContentType.URLENC)
                .formParam("username", "newUser")
                .formParam("password", "newPassword123")
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(400)
                .body("message", containsString("Preferred currency is required"));
    }

    @Test
    @Order(3)
    void registerUserWithInvalidCurrency() {
        String invalidCurrency = "INVALID_CURRENCY";

        given()
                .contentType(ContentType.URLENC)
                .formParam("username", "newUser")
                .formParam("password", "newPassword123")
                .formParam("preferredCurrency", invalidCurrency)
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(404)
                .body("message", containsString("Currency INVALID_CURRENCY not found"));
    }

    @Test
    @Order(4)
    void registerUserWithoutUsername() {
        given()
                .contentType(ContentType.URLENC)
                .formParam("password", "newPassword123")
                .formParam("preferredCurrency", "USD")
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(400)
                .body("message", containsString("Username is required"));
    }

    @Test
    @Order(5)
    void registerUserWithoutPassword() {
        given()
                .contentType(ContentType.URLENC)
                .formParam("username", "newUser")
                .formParam("preferredCurrency", "USD")
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(400)
                .body("message", containsString("Password is required"));
    }
}
