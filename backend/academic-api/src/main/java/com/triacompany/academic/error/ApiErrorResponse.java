package com.triacompany.academic.error;

import java.time.LocalDateTime;
import java.util.List;

public record ApiErrorResponse(
        LocalDateTime timestamp,
        Integer status,
        String error,
        String message,
        String path,
        List<ApiFieldErrorResponse> fieldErrors
) {
    public static ApiErrorResponse of(
            Integer status,
            String error,
            String message,
            String path
    ) {
        return new ApiErrorResponse(
                LocalDateTime.now(),
                status,
                error,
                message,
                path,
                List.of()
        );
    }

    public static ApiErrorResponse withFieldErrors(
            Integer status,
            String error,
            String message,
            String path,
            List<ApiFieldErrorResponse> fieldErrors
    ) {
        return new ApiErrorResponse(
                LocalDateTime.now(),
                status,
                error,
                message,
                path,
                fieldErrors == null ? List.of() : fieldErrors
        );
    }
}