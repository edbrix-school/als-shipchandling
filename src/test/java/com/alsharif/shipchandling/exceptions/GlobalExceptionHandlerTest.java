package com.alsharif.shipchandling.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.xml.bind.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.atLeastOnce;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler exceptionHandler;

    @Mock
    private HttpServletRequest request;

    @Test
    void testHandleCustomException_WithCode() {
        // Arrange
        CustomException ex = new CustomException("Test error", 400);

        // Act
        ResponseEntity<?> response = exceptionHandler.handleAsgException(ex);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.valueOf(400), response.getStatusCode());
    }

    @Test
    void testHandleCustomException_WithoutCode() {
        // Arrange
        CustomException ex = new CustomException("Test error");

        // Act
        ResponseEntity<?> response = exceptionHandler.handleAsgException(ex);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.valueOf(500), response.getStatusCode());
    }

    @Test
    void testHandleValidationException() {
        // Arrange
        ValidationException ex = new ValidationException("Validation failed");

        // Act
        ResponseEntity<?> response = exceptionHandler.handleValidationException(ex);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testHandleIllegalArgumentException() {
        // Arrange
        IllegalArgumentException ex = new IllegalArgumentException("Illegal argument");

        // Act
        ResponseEntity<?> response = exceptionHandler.handleIllegalArgumentException(ex);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testHandleMethodArgumentTypeMismatchException_WithLocalDate() {
        // Arrange
        MethodArgumentTypeMismatchException ex = mock(MethodArgumentTypeMismatchException.class);
        when(ex.getRequiredType()).thenAnswer(invocation -> LocalDate.class);
        when(ex.getValue()).thenReturn("invalid-date");
        when(ex.getPropertyName()).thenReturn("dateParam");
        when(ex.getMessage()).thenReturn("Type mismatch");

        // Act
        ResponseEntity<?> response = exceptionHandler.handleDateFormatException(ex);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(ex).getRequiredType();
        verify(ex, atLeastOnce()).getValue(); // getValue() may be called multiple times in the handler
        verify(ex).getPropertyName();
    }

    @Test
    void testHandleMethodArgumentTypeMismatchException_WithLocalDate_NullValue() {
        // Arrange
        MethodArgumentTypeMismatchException ex = mock(MethodArgumentTypeMismatchException.class);
        when(ex.getRequiredType()).thenAnswer(invocation -> LocalDate.class);
        when(ex.getValue()).thenReturn(null);
        when(ex.getPropertyName()).thenReturn(null);
        when(ex.getMessage()).thenReturn("Type mismatch");

        // Act
        ResponseEntity<?> response = exceptionHandler.handleDateFormatException(ex);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testHandleMethodArgumentTypeMismatchException_WithLocalDate_EmptyValue() {
        // Arrange
        MethodArgumentTypeMismatchException ex = mock(MethodArgumentTypeMismatchException.class);
        when(ex.getRequiredType()).thenAnswer(invocation -> LocalDate.class);
        when(ex.getValue()).thenReturn("");
        when(ex.getPropertyName()).thenReturn("");
        when(ex.getMessage()).thenReturn("Type mismatch");

        // Act
        ResponseEntity<?> response = exceptionHandler.handleDateFormatException(ex);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testHandleMethodArgumentTypeMismatchException_NotLocalDate() {
        // Arrange
        MethodArgumentTypeMismatchException ex = mock(MethodArgumentTypeMismatchException.class);
        when(ex.getRequiredType()).thenAnswer(invocation -> String.class);
        when(ex.getMessage()).thenReturn("Type mismatch");

        // Act
        ResponseEntity<?> response = exceptionHandler.handleDateFormatException(ex);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testHandleMethodArgumentNotValidException() {
        // Arrange
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError1 = new FieldError("object", "field1", "Error 1");
        FieldError fieldError2 = new FieldError("object", "field2", "Error 2");
        List<FieldError> fieldErrors = List.of(fieldError1, fieldError2);

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(fieldErrors);
        when(request.getRequestURI()).thenReturn("/api/test");

        // Act
        ResponseEntity<?> response = exceptionHandler.handleValidationExceptions(ex, request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(request).getRequestURI();
    }

    @Test
    void testHandleMethodArgumentNotValidException_EmptyErrors() {
        // Arrange
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(new ArrayList<>());
        when(request.getRequestURI()).thenReturn("/api/test");

        // Act
        ResponseEntity<?> response = exceptionHandler.handleValidationExceptions(ex, request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(request).getRequestURI();
    }

    @Test
    void testHandleMissingPathVariable() {
        // Arrange
        MissingPathVariableException ex = new MissingPathVariableException("id", null);

        // Act
        ResponseEntity<?> response = exceptionHandler.handleMissingPathVariable(ex);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testHandleMissingRequestHeader() {
        // Arrange
        MissingRequestHeaderException ex = new MissingRequestHeaderException("Authorization", null);

        // Act
        ResponseEntity<?> response = exceptionHandler.handleMissingHeader(ex);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testHandleResourceAlreadyExistsException() {
        // Arrange
        ResourceAlreadyExistsException ex = new ResourceAlreadyExistsException("name", "value");

        // Act
        ResponseEntity<?> response = exceptionHandler.handleResourceAlreadyExists(ex);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void testHandleResourceNotFoundException() {
        // Arrange
        ResourceNotFoundException ex = new ResourceNotFoundException("Resource", "field", "value");

        // Act
        ResponseEntity<?> response = exceptionHandler.handleResourceNotFound(ex);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testHandleNoResourceFoundException() {
        // Arrange
        NoResourceFoundException ex = new NoResourceFoundException(null, "/api/test");

        // Act
        ResponseEntity<?> response = exceptionHandler.handleNoResourceFoundException(ex);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testHandleJsonParseErrors_WithCause() {
        // Arrange
        HttpMessageNotReadableException ex = mock(HttpMessageNotReadableException.class);
        IllegalArgumentException cause = new IllegalArgumentException("Invalid enum value");
        when(ex.getMostSpecificCause()).thenReturn(cause);

        // Act
        ResponseEntity<?> response = exceptionHandler.handleJsonParseErrors(ex);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(ex).getMostSpecificCause();
    }

    @Test
    void testHandleJsonParseErrors_WithoutCause() {
        // Arrange
        HttpMessageNotReadableException ex = mock(HttpMessageNotReadableException.class);
        when(ex.getMostSpecificCause()).thenReturn(null);

        // Act
        ResponseEntity<?> response = exceptionHandler.handleJsonParseErrors(ex);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(ex).getMostSpecificCause();
    }

    @Test
    void testHandleMaxUploadSizeExceededException() {
        // Arrange
        MaxUploadSizeExceededException ex = new MaxUploadSizeExceededException(1000L);

        // Act
        ResponseEntity<?> response = exceptionHandler.handleMaxUploadSize(ex);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testHandleRuntimeException() {
        // Arrange
        RuntimeException ex = new RuntimeException("Runtime error");
        when(request.getRequestURI()).thenReturn("/api/test");

        // Act
        ResponseEntity<?> response = exceptionHandler.handleRuntimeException(ex, request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(request).getRequestURI();
    }

    @Test
    void testHandleGeneralException() {
        // Arrange
        Exception ex = new Exception("General error");
        when(request.getRequestURI()).thenReturn("/api/test");

        // Act
        ResponseEntity<?> response = exceptionHandler.handleGeneralException(ex, request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(request).getRequestURI();
    }
}

