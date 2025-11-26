package com.alsharif.shipchandling.exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResourceAlreadyExistsExceptionTest {

    @Test
    void testResourceAlreadyExistsException_Constructor() {
        // Arrange
        String fieldName = "email";
        String fieldValue = "test@example.com";

        // Act
        ResourceAlreadyExistsException ex = new ResourceAlreadyExistsException(fieldName, fieldValue);

        // Assert
        assertNotNull(ex);
        assertEquals(fieldName, ex.getFieldName());
        assertEquals(fieldValue, ex.getFieldValue());
        assertTrue(ex.getMessage().contains(fieldName));
        assertTrue(ex.getMessage().contains(fieldValue));
    }

    @Test
    void testResourceAlreadyExistsException_MessageFormat() {
        // Arrange
        String fieldName = "username";
        String fieldValue = "john_doe";

        // Act
        ResourceAlreadyExistsException ex = new ResourceAlreadyExistsException(fieldName, fieldValue);

        // Assert
        String expectedMessage = String.format("%s already exists with value: %s", fieldName, fieldValue);
        assertEquals(expectedMessage, ex.getMessage());
    }

    @Test
    void testResourceAlreadyExistsException_WithNullValue() {
        // Arrange
        String fieldName = "code";
        String fieldValue = null;

        // Act
        ResourceAlreadyExistsException ex = new ResourceAlreadyExistsException(fieldName, fieldValue);

        // Assert
        assertEquals(fieldName, ex.getFieldName());
        assertNull(ex.getFieldValue());
    }

    @Test
    void testResourceAlreadyExistsException_Getters() {
        // Arrange
        String fieldName = "id";
        String fieldValue = "12345";

        // Act
        ResourceAlreadyExistsException ex = new ResourceAlreadyExistsException(fieldName, fieldValue);

        // Assert
        assertEquals(fieldName, ex.getFieldName());
        assertEquals(fieldValue, ex.getFieldValue());
    }
}

