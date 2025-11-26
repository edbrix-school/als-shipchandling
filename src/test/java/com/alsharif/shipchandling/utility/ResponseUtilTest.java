package com.alsharif.shipchandling.utility;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ResponseUtilTest {

    @Test
    void testSuccess_WithData() {
        // Arrange
        String message = "Success message";
        String data = "Test data";

        // Act
        ResponseEntity<Map<String, Object>> response = ResponseUtil.success(message, data);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        Map<String, Object> body = response.getBody();
        assertEquals(message, body.get("message"));
        assertEquals(data, body.get("data"));
    }

    @Test
    void testSuccess_WithNullData() {
        // Arrange
        String message = "Success message";

        // Act
        ResponseEntity<Map<String, Object>> response = ResponseUtil.success(message, null);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        Map<String, Object> body = response.getBody();
        assertEquals(message, body.get("message"));
        assertNull(body.get("data"));
    }

    @Test
    void testSuccess_WithMapData() {
        // Arrange
        String message = "Success message";
        Map<String, String> data = Map.of("key", "value");

        // Act
        ResponseEntity<Map<String, Object>> response = ResponseUtil.success(message, data);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        Map<String, Object> body = response.getBody();
        assertEquals(message, body.get("message"));
        assertEquals(data, body.get("data"));
    }

    @Test
    void testSuccess_WithIntegerData() {
        // Arrange
        String message = "Success message";
        Integer data = 123;

        // Act
        ResponseEntity<Map<String, Object>> response = ResponseUtil.success(message, data);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        Map<String, Object> body = response.getBody();
        assertEquals(message, body.get("message"));
        assertEquals(data, body.get("data"));
    }

    @Test
    void testSuccess_WithEmptyStringMessage() {
        // Arrange
        String message = "";
        String data = "Test data";

        // Act
        ResponseEntity<Map<String, Object>> response = ResponseUtil.success(message, data);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        Map<String, Object> body = response.getBody();
        assertEquals("", body.get("message"));
        assertEquals(data, body.get("data"));
    }
}

