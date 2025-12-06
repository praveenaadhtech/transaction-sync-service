package com.transactionsync.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponse<T> {
    
    private String status;
    private String message;
    private T data;
    private String errorCode;
    private List<FieldErrorDetail> errors;
    
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
            .status("success")
            .data(data)
            .build();
    }

    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
            .status("success")
            .message(message)
            .build();
    }
    
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
            .status("success")
            .message(message)
            .data(data)
            .build();
    }
    
    public static <T> ApiResponse<T> error(String message, String errorCode) {
        return ApiResponse.<T>builder()
            .status("error")
            .message(message)
            .errorCode(errorCode)
            .build();
    }

    public static <T> ApiResponse<T> validationError(String message, List<FieldErrorDetail> errors) {
        return ApiResponse.<T>builder()
            .status("error")
            .message(message)
            .errors(errors)
            .build();
    }
    
}

