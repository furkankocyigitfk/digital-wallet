package com.furkan.digitalWallet.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private Logger logger;

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeEach
    void setUp() {
        // Mock the static LoggerFactory.getLogger call to return the mocked logger
        try (var mockedStatic = mockStatic(LoggerFactory.class)) {
            mockedStatic.when(() -> LoggerFactory.getLogger(GlobalExceptionHandler.class)).thenReturn(logger);
        }
    }

    @Test
    void handleNotFound_ReturnsNotFoundResponse() {
        // Arrange
        NotFoundException ex = new NotFoundException("Resource not found");

        // Act
        ResponseEntity<?> response = globalExceptionHandler.handleNotFound(ex);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertNotNull(body);
        assertEquals("Resource not found", body.get("error"));

        verifyNoInteractions(logger);
    }

    @Test
    void handleBadRequest_ReturnsBadRequestResponse() {
        // Arrange
        BadRequestException ex = new BadRequestException("Invalid request");

        // Act
        ResponseEntity<?> response = globalExceptionHandler.handleBadRequest(ex);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertNotNull(body);
        assertEquals("Invalid request", body.get("error"));

        verifyNoInteractions(logger);
    }

    @Test
    void handleValidation_ReturnsBadRequestWithValidationErrors() {
        // Arrange
        FieldError fieldError = new FieldError("objectName", "fieldName", "Field is invalid");
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        // Act
        ResponseEntity<?> response = globalExceptionHandler.handleValidation(ex);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Map<String, String>> body = (Map<String, Map<String, String>>) response.getBody();
        assertNotNull(body);
        Map<String, String> validationErrors = body.get("validationErrors");
        assertNotNull(validationErrors);
        assertEquals("Field is invalid", validationErrors.get("fieldName"));

        verify(bindingResult).getFieldErrors();
        verifyNoInteractions(logger);
    }

    @Test
    void handleValidation_MultipleFieldErrors_ReturnsAllValidationErrors() {
        // Arrange
        FieldError fieldError1 = new FieldError("objectName", "field1", "Field1 is invalid");
        FieldError fieldError2 = new FieldError("objectName", "field2", "Field2 is invalid");
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError1, fieldError2));
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        // Act
        ResponseEntity<?> response = globalExceptionHandler.handleValidation(ex);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Map<String, String>> body = (Map<String, Map<String, String>>) response.getBody();
        assertNotNull(body);
        Map<String, String> validationErrors = body.get("validationErrors");
        assertNotNull(validationErrors);
        assertEquals("Field1 is invalid", validationErrors.get("field1"));
        assertEquals("Field2 is invalid", validationErrors.get("field2"));

        verify(bindingResult).getFieldErrors();
        verifyNoInteractions(logger);
    }

    @Test
    void handleGeneric_LogsErrorAndReturnsInternalServerError() {
        // Arrange
        Exception ex = new RuntimeException("Unexpected error");

        // Act
        ResponseEntity<?> response = globalExceptionHandler.handleGeneric(ex);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertNotNull(body);
        assertEquals("Beklenmeyen hata", body.get("error"));
    }
}