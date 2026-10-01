package com.example.dogmiddleware.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "external.dog-api")
public record DogApiProperties(
        @NotBlank @URL(protocol = "https")
        String baseUrl,
        @Min(1)
        long connectTimeout,
        @Min(1)
        long readTimeout
) {
}
