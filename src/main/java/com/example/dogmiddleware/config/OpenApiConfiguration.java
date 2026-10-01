package com.example.dogmiddleware.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

    @Bean
    OpenAPI dogMiddlewareOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Dog Middleware API")
                        .version("1.0.0")
                        .description("Consumer API for random dog images and available breeds."));
    }
}
