package com.example.dogmiddleware.exception;

import com.example.dogmiddleware.constant.ErrorMessages;
import com.example.dogmiddleware.dto.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class ApiExceptionHandler {

        private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(
                        IllegalArgumentException exception) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(ApiResponse.failure(exception.getMessage()));
        }

        @ExceptionHandler(ResourceNotFoundException.class)
        public ResponseEntity<ApiResponse<Void>> handleResourceNotFoundException(
                        ResourceNotFoundException exception) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(ApiResponse.failure(exception.getMessage()));
        }

        @ExceptionHandler({ ConstraintViolationException.class, HandlerMethodValidationException.class })
        public ResponseEntity<ApiResponse<Void>> handleValidationException(Exception exception) {
                return ResponseEntity.badRequest()
                                .body(ApiResponse.failure(ErrorMessages.INVALID_BREED_FORMAT));
        }

        @ExceptionHandler(DogApiException.class)
        public ResponseEntity<ApiResponse<Void>> handleDogApiException(DogApiException exception) {
                HttpStatus status = exception.getStatusCode().value() == HttpStatus.GATEWAY_TIMEOUT.value()
                                ? HttpStatus.GATEWAY_TIMEOUT
                                : HttpStatus.BAD_GATEWAY;
                String message = status == HttpStatus.GATEWAY_TIMEOUT
                                ? ErrorMessages.DOG_API_TIMEOUT
                                : ErrorMessages.DOG_API_UNAVAILABLE;
                return ResponseEntity.status(status).body(ApiResponse.failure(message));
        }

        @ExceptionHandler(NoResourceFoundException.class)
        public ResponseEntity<ApiResponse<Void>> handleNoResourceFound() {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(ApiResponse.failure(ErrorMessages.RESOURCE_NOT_FOUND));
        }

        @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
        public ResponseEntity<ApiResponse<Void>> handleMethodNotAllowed() {
                return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                                .body(ApiResponse.failure(ErrorMessages.METHOD_NOT_ALLOWED));
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiResponse<Void>> handleUnexpectedException(Exception exception) {
                log.error("Unhandled application error", exception);
                return ResponseEntity.internalServerError()
                                .body(ApiResponse.failure(ErrorMessages.INTERNAL_SERVER_ERROR));
        }
}
