package com.alsharif.shipchandling.common;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ApiResponseTest {

    @Test
    void testSuccess_WithData() {
        // Arrange
        String message = "Success message";
        Map<String, String> data = new HashMap<>();
        data.put("key", "value");

        // Act
        ResponseEntity<?> response = ApiResponse.success(message, data);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals(200, body.get("statusCode"));
        assertEquals(true, body.get("success"));
        assertEquals(message, body.get("message"));
        assertNotNull(body.get("result"));
    }

    @Test
    void testSuccess_WithNullData() {
        // Arrange
        String message = "Success message";

        // Act
        ResponseEntity<?> response = ApiResponse.success(message, null);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("", body.get("result"));
    }

    @Test
    void testSuccess_WithoutData() {
        // Arrange
        String message = "Success message";

        // Act
        ResponseEntity<?> response = ApiResponse.success(message);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals(200, body.get("statusCode"));
        assertEquals(true, body.get("success"));
        assertEquals(message, body.get("message"));
    }

    @Test
    void testBadRequest() {
        // Arrange
        String message = "Bad request message";

        // Act
        ResponseEntity<?> response = ApiResponse.badRequest(message);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals(String.valueOf(HttpStatus.BAD_REQUEST.value()), body.get("statusCode"));
        assertEquals(false, body.get("success"));
        assertEquals(message, body.get("message"));
    }

    @Test
    void testInternalServerError() {
        // Arrange
        String message = "Internal server error";

        // Act
        ResponseEntity<?> response = ApiResponse.internalServerError(message);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), body.get("statusCode"));
        assertEquals(false, body.get("success"));
    }

    @Test
    void testNotFound() {
        // Arrange
        String message = "Not found";

        // Act
        ResponseEntity<?> response = ApiResponse.notFound(message);

        // Assert
        assertNotNull(response);
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals(String.valueOf(HttpStatus.NOT_FOUND.value()), body.get("statusCode"));
        assertEquals(false, body.get("success"));
    }

    @Test
    void testUnprocessableEntity() {
        // Arrange
        String message = "Unprocessable entity";

        // Act
        ResponseEntity<?> response = ApiResponse.unprocessableEntity(message);

        // Assert
        assertNotNull(response);
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals(String.valueOf(HttpStatus.UNPROCESSABLE_ENTITY.value()), body.get("statusCode"));
        assertEquals(false, body.get("success"));
    }

    @Test
    void testUnauthorized() {
        // Arrange
        String message = "Unauthorized";

        // Act
        ResponseEntity<?> response = ApiResponse.unauthorized(message);

        // Assert
        assertNotNull(response);
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals(String.valueOf(HttpStatus.UNAUTHORIZED.value()), body.get("statusCode"));
        assertEquals(false, body.get("success"));
    }

    @Test
    void testConflict() {
        // Arrange
        String message = "Conflict";

        // Act
        ResponseEntity<?> response = ApiResponse.conflict(message);

        // Assert
        assertNotNull(response);
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals(String.valueOf(HttpStatus.CONFLICT.value()), body.get("statusCode"));
        assertEquals(false, body.get("success"));
    }

    @Test
    void testError_WithStatusCode() {
        // Arrange
        String message = "Error message";
        int statusCode = 400;

        // Act
        ResponseEntity<?> response = ApiResponse.error(message, statusCode);

        // Assert
        assertNotNull(response);
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals(String.valueOf(statusCode), body.get("statusCode"));
        assertEquals(false, body.get("success"));
        assertEquals(message, body.get("message"));
    }

    @Test
    void testError_WithErrors() {
        // Arrange
        String message = "Validation error";
        int statusCode = 400;
        Map<String, String> errors = new HashMap<>();
        errors.put("field1", "Error 1");
        errors.put("field2", "Error 2");

        // Act
        ResponseEntity<?> response = ApiResponse.error(message, statusCode, errors);

        // Assert
        assertNotNull(response);
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals(String.valueOf(statusCode), body.get("statusCode"));
        assertEquals(false, body.get("success"));
        assertEquals(message, body.get("message"));
        assertEquals(errors, body.get("errors"));
    }

}

