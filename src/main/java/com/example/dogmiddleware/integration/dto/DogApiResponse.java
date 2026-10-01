package com.example.dogmiddleware.integration.dto;

public record DogApiResponse<T>(T message, String status) {
}
