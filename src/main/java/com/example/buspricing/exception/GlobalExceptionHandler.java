package com.example.buspricing.exception;

import com.example.buspricing.controller.response.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                 HttpServletRequest request) {
        List<ApiError.FieldErrorItem> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::toFieldItem)
                .toList();

        return buildError(HttpStatus.BAD_REQUEST, request, "Validation failed", fieldErrors);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiError> handleBindException(BindException ex, HttpServletRequest request) {
        List<ApiError.FieldErrorItem> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::toFieldItem)
                .toList();

        return buildError(HttpStatus.BAD_REQUEST, request, "Validation failed", fieldErrors);
    }

    @ExceptionHandler(ValidationErrorException.class)
    public ResponseEntity<ApiError> handleValidationError(ValidationErrorException ex,
                                                      HttpServletRequest request) {
        List<ApiError.FieldErrorItem> fieldErrors = List.of(
                ApiError.FieldErrorItem.builder()
                        .field(ex.getField())
                        .message(ex.getMessage())
                        .rejectedValue(ex.getRejectedValue())
                        .build()
        );
        return buildError(ex.getHttpStatus(), request, "Validation error", fieldErrors);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleAny(Exception ex, HttpServletRequest request) {
        log.error("Something went wrong", ex);
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, request, "Unexpected error", null);
    }

    private ApiError.FieldErrorItem toFieldItem(FieldError fe) {
        return ApiError.FieldErrorItem.builder()
                .field(fe.getField())
                .message(fe.getDefaultMessage())
                .rejectedValue(fe.getRejectedValue())
                .build();
    }

    private ResponseEntity<ApiError> buildError(HttpStatus status,
                                                HttpServletRequest request,
                                                String error,
                                                List<ApiError.FieldErrorItem> fieldErrors) {
        ApiError body = ApiError.builder()
                .timestamp(OffsetDateTime.now())
                .status(status.value())
                .error(error)
                .path(request.getRequestURI())
                .errors(fieldErrors)
                .build();

        return ResponseEntity.status(status).body(body);
    }
}
