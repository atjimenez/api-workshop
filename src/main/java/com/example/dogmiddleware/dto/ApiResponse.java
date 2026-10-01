package com.example.dogmiddleware.dto;

import com.example.dogmiddleware.constant.ResponseStatus;

public record ApiResponse<T>(T data, String message, String status) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(data, null, ResponseStatus.SUCCESS);
    }

    public static <T> ApiResponse<T> failure(String message) {
        return new ApiResponse<>(null, message, ResponseStatus.FAILED);
    }
}
