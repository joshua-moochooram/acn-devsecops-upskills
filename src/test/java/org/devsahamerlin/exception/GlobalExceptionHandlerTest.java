package org.devsahamerlin.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;

import java.util.Collections;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeEach
    void setUp() {
        globalExceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void handleGenericException_shouldReturnInternalServerError() {
        Exception ex = new Exception("Something went wrong");

        ErrorDTO response = globalExceptionHandler.handleException(ex);

        assertThat(response.getCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
        assertThat(response.getMessage()).isEqualTo("Unexpected error!");
    }

    @Test
    void handleValidationException_shouldReturnBadRequest() {
        ValidationException ex = new ValidationException("Validation failed");

        ErrorDTO response = globalExceptionHandler.handleException(ex);

        assertThat(response.getCode()).isEqualTo(HttpStatus.BAD_REQUEST.getReasonPhrase());
        assertThat(response.getMessage()).isEqualTo("Validation failed");
    }

    @Test
    void handleConstraintViolationException_shouldReturnJoinedViolations() {
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("Field must not be null");

        Set<ConstraintViolation<?>> violations = Collections.singleton(violation);
        ConstraintViolationException ex = new ConstraintViolationException(violations);

        ErrorDTO response = globalExceptionHandler.handleException(ex);

        assertThat(response.getCode()).isEqualTo(HttpStatus.BAD_REQUEST.getReasonPhrase());
        assertThat(response.getMessage()).isEqualTo("Field must not be null");
    }
}

