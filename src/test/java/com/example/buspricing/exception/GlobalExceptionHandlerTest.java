package com.example.buspricing.exception;

import com.example.buspricing.controller.response.ApiError;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();

    /**
     * Test for the handleMethodArgumentNotValid method in GlobalExceptionHandler.
     * This method handles MethodArgumentNotValidException and returns a response containing validation errors.
     */
    @Test
    void testHandleMethodArgumentNotValid() {
        // Mock the MethodArgumentNotValidException and its BindingResult
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        FieldError fieldError1 = new FieldError("TestObject", "field1", "Invalid value");
        FieldError fieldError2 = new FieldError("TestObject", "field2", "Cannot be null");

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError1, fieldError2));

        // Mock the HttpServletRequest
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/test-endpoint");

        // Call the method under test
        ResponseEntity<ApiError> response = globalExceptionHandler.handleMethodArgumentNotValid(exception, request);

        // Assert the response
        assertNotNull(response);
        assertEquals(400, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().getErrors().size());

        ApiError.FieldErrorItem error1 = response.getBody().getErrors().get(0);
        ApiError.FieldErrorItem error2 = response.getBody().getErrors().get(1);

        assertEquals("field1", error1.getField());
        assertEquals("Invalid value", error1.getMessage());
        assertEquals("field2", error2.getField());
        assertEquals("Cannot be null", error2.getMessage());
    }

    @Test
    void testHandleBindException() {
        // Mock BindException and its BindingResult
        BindException exception = mock(BindException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        FieldError fieldError1 = new FieldError("TestObject", "amount", -5, false, null, null, "must be positive");
        FieldError fieldError2 = new FieldError("TestObject", "currency", "must not be blank");

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError1, fieldError2));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/bind-endpoint");

        ResponseEntity<ApiError> response = globalExceptionHandler.handleBindException(exception, request);

        assertNotNull(response);
        assertEquals(400, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("/bind-endpoint", response.getBody().getPath());
        assertEquals(2, response.getBody().getErrors().size());

        ApiError.FieldErrorItem e1 = response.getBody().getErrors().get(0);
        ApiError.FieldErrorItem e2 = response.getBody().getErrors().get(1);

        assertEquals("amount", e1.getField());
        assertEquals("must be positive", e1.getMessage());
        assertEquals(-5, e1.getRejectedValue());

        assertEquals("currency", e2.getField());
        assertEquals("must not be blank", e2.getMessage());
    }

    @Test
    void testHandleCustomError_ValidationErrorException() {
        ValidationErrorException ex = new ValidationErrorException(
                "age",
                "too young",
                12,
                HttpStatus.UNPROCESSABLE_ENTITY
        );
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/custom-error");

        ResponseEntity<ApiError> response = globalExceptionHandler.handleValidationError(ex, request);

        assertNotNull(response);
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("/custom-error", response.getBody().getPath());
        assertEquals("Validation error", response.getBody().getError());
        assertNotNull(response.getBody().getErrors());
        assertEquals(1, response.getBody().getErrors().size());

        ApiError.FieldErrorItem item = response.getBody().getErrors().getFirst();
        assertEquals("age", item.getField());
        assertEquals("too young", item.getMessage());
        assertEquals(12, item.getRejectedValue());
    }

    @Test
    void testHandleAny_GenericException() {
        Exception ex = new RuntimeException("boom");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/any-error");

        ResponseEntity<ApiError> response = globalExceptionHandler.handleAny(ex, request);

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("/any-error", response.getBody().getPath());
        assertEquals("Unexpected error", response.getBody().getError());
        // errors may be null for generic error
    }

}