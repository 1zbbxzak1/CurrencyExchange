package ru.julia.currencyexchange.configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(title = "Currency Exchange API", version = "v1", description = "API for currency exchange")
)
public class SwaggerConfig {

}
