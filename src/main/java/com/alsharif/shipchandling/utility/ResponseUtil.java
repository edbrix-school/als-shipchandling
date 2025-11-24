package com.alsharif.shipchandling.utility;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;

public class ResponseUtil {
    public static <T> ResponseEntity<Map<String, Object>> success(String message, T data) {
        Map<String, Object> body = new HashMap<>();
        body.put("message", message);
        body.put("data", data);
        return ResponseEntity.ok(body);
    }
}
