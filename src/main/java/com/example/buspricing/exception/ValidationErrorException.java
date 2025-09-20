package com.example.buspricing.exception;

import lombok.*;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class ValidationErrorException extends RuntimeException {
    private String field;
    private Object rejectedValue;
    private HttpStatus httpStatus;
    public ValidationErrorException(String field,
                                    String message,
                                    Object rejectedValue,
                                    HttpStatus httpStatus) {
        super(message);
        this.field = field;
        this.rejectedValue = rejectedValue;
        this.httpStatus = httpStatus;
    }
}
