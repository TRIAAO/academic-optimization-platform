package com.triacompany.academic.error;

public record ApiFieldErrorResponse(
        String field,
        String message,
        Object rejectedValue
) {
}