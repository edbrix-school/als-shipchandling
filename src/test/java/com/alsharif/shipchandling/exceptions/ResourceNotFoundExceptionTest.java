package com.alsharif.shipchandling.exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResourceNotFoundExceptionTest {

    @Test
    void testResourceNotFoundException_Constructor() {
        // Arrange
        String resourceName = "User";
        String fieldName = "id";
        Object fieldValue = 123L;

        // Act
        ResourceNotFoundException ex = new ResourceNotFoundException(resourceName, fieldName, fieldValue);

        // Assert
        assertNotNull(ex);
        assertEquals(resourceName, ex.getResourceName());
        assertEquals(fieldName, ex.getFieldName());
        assertEquals(fieldValue, ex.getFieldValue());
        assertTrue(ex.getMessage().contains(resourceName));
        assertTrue(ex.getMessage().contains(fieldName));
        assertTrue(ex.getMessage().contains(fieldValue.toString()));
    }

    @Test
    void testResourceNotFoundException_WithStringValue() {
        // Arrange
        String resourceName = "Product";
        String fieldName = "code";
        String fieldValue = "PROD-001";

        // Act
        ResourceNotFoundException ex = new ResourceNotFoundException(resourceName, fieldName, fieldValue);

        // Assert
        assertEquals(resourceName, ex.getResourceName());
        assertEquals(fieldName, ex.getFieldName());
        assertEquals(fieldValue, ex.getFieldValue());
    }

    @Test
    void testResourceNotFoundException_WithNullValue() {
        // Arrange
        String resourceName = "Order";
        String fieldName = "orderId";
        Object fieldValue = null;

        // Act
        ResourceNotFoundException ex = new ResourceNotFoundException(resourceName, fieldName, fieldValue);

        // Assert
        assertEquals(resourceName, ex.getResourceName());
        assertEquals(fieldName, ex.getFieldName());
        assertNull(ex.getFieldValue());
    }

    @Test
    void testResourceNotFoundException_MessageFormat() {
        // Arrange
        String resourceName = "Item";
        String fieldName = "itemId";
        Object fieldValue = 999;

        // Act
        ResourceNotFoundException ex = new ResourceNotFoundException(resourceName, fieldName, fieldValue);

        // Assert
        String expectedMessage = String.format("%s not found with %s : '%s'", resourceName, fieldName, fieldValue);
        assertEquals(expectedMessage, ex.getMessage());
    }
}

