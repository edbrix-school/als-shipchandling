package com.alsharif.shipchandling.exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CustomExceptionTest {

    @Test
    void testCustomException_WithMessageOnly() {
        // Arrange
        String message = "Test error message";

        // Act
        CustomException ex = new CustomException(message);

        // Assert
        assertEquals(message, ex.getMessage());
        assertEquals(500, ex.getCode());
        assertNull(ex.getCause());
    }

    @Test
    void testCustomException_WithMessageAndCode() {
        // Arrange
        String message = "Test error message";
        int code = 404;

        // Act
        CustomException ex = new CustomException(message, code);

        // Assert
        assertEquals(message, ex.getMessage());
        assertEquals(code, ex.getCode());
        assertNull(ex.getCause());
    }

    @Test
    void testCustomException_WithMessageAndCause() {
        // Arrange
        String message = "Test error message";
        Throwable cause = new RuntimeException("Root cause");

        // Act
        CustomException ex = new CustomException(message, cause);

        // Assert
        assertEquals(message, ex.getMessage());
        assertEquals(500, ex.getCode());
        assertEquals(cause, ex.getCause());
    }

    @Test
    void testCustomException_WithMessageCodeAndCause() {
        // Arrange
        String message = "Test error message";
        int code = 400;
        Throwable cause = new IllegalArgumentException("Root cause");

        // Act
        CustomException ex = new CustomException(message, code, cause);

        // Assert
        assertEquals(message, ex.getMessage());
        assertEquals(code, ex.getCode());
        assertEquals(cause, ex.getCause());
    }

    @Test
    void testCustomException_DefaultCode() {
        // Arrange & Act
        CustomException ex1 = new CustomException("Message 1");
        CustomException ex2 = new CustomException("Message 2", new Exception());

        // Assert
        assertEquals(500, ex1.getCode());
        assertEquals(500, ex2.getCode());
    }

    @Test
    void testCustomException_Getter() {
        // Arrange
        int code = 403;
        CustomException ex = new CustomException("Forbidden", code);

        // Act & Assert
        assertEquals(code, ex.getCode());
        // Test that getter method works (Lombok should generate it, but testing explicitly)
        assertEquals(ex.getCode(), ex.getCode());
    }
}

