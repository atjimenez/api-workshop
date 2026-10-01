package com.example.dogmiddleware.exception;

import org.springframework.http.HttpStatusCode;

public class DogApiException extends RuntimeException {

    private final HttpStatusCode statusCode;

    public DogApiException(HttpStatusCode statusCode, String message, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public HttpStatusCode getStatusCode() {
        return statusCode;
    }
}
