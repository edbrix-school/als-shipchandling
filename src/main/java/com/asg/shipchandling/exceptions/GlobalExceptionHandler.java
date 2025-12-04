package com.asg.shipchandling.exceptions;

import com.asg.shipchandling.common.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.xml.bind.ValidationException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<?> handleAsgException(CustomException ex) {
        log.error("CustomException: {}", ex.getMessage());
        return ApiResponse.error(ex.getMessage(), ex.getCode());
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<?> handleValidationException(ValidationException ex) {
        return ApiResponse.badRequest(ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ApiResponse.error(ex.getMessage(), HttpStatus.BAD_REQUEST.value());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<?> handleDateFormatException(MethodArgumentTypeMismatchException ex) {
        if (ex.getRequiredType() == LocalDate.class) {
            String value = ex.getValue() != null ? ex.getValue().toString() : null;
            String parameterName = ex.getPropertyName();

            String message = "Invalid date format. Please use YYYY-MM-DD.";
            if (StringUtils.isNotBlank(value) && StringUtils.isNotBlank(parameterName)) {
                message = String.format("Invalid value '%s' for parameter '%s'. Expected format: YYYY-MM-DD.", value,
                        parameterName);
            }
            return ApiResponse.badRequest(message);
        }
        return ApiResponse.badRequest(ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationExceptions(MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        Map<String, Object> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        log.info("Validation errors at {}", request.getRequestURI());
        return ApiResponse.error("Validation error occurred", HttpStatus.BAD_REQUEST.value(), errors);
    }

    @ExceptionHandler(MissingPathVariableException.class)
    public ResponseEntity<?> handleMissingPathVariable(MissingPathVariableException ex) {
        String msg = String.format("Missing path variable: '%s'", ex.getVariableName());
        return ApiResponse.badRequest(msg);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<?> handleMissingHeader(MissingRequestHeaderException ex) {
        String msg = String.format("Missing Header variable: '%s'", ex.getHeaderName());
        return ApiResponse.badRequest(msg);
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<?> handleResourceAlreadyExists(ResourceAlreadyExistsException ex) {
        return ApiResponse.conflict(ex.getMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> handleResourceNotFound(ResourceNotFoundException ex) {
        return ApiResponse.notFound(ex.getMessage());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<?> handleNoResourceFoundException(NoResourceFoundException ex) {
        return ApiResponse.notFound(ex.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> handleJsonParseErrors(HttpMessageNotReadableException ex) {
        Map<String, Object> response = new HashMap<>();

        // extract root cause if it’s IllegalArgumentException from enum
        Throwable cause = ex.getMostSpecificCause();
        String message = cause != null ? cause.getMessage() : "Invalid request payload";
        return ApiResponse.error(message, HttpStatus.BAD_REQUEST.value());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<?> handleMaxUploadSize(MaxUploadSizeExceededException ex) {
        String msg = "File exceeds the maximum allowed upload size. Please upload a smaller file.";
        log.warn("MaxUploadSizeExceededException: {}", ex.getMessage());
        return ApiResponse.badRequest(msg);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> handleDataIntegrityViolation(DataIntegrityViolationException ex, HttpServletRequest request) {
        log.warn("DataIntegrityViolationException at {}: {}", request.getRequestURI(), ex.getMessage());
        
        String errorMessage = ex.getMessage();
        if (errorMessage == null) {
            errorMessage = ex.getCause() != null ? ex.getCause().getMessage() : "Data integrity violation";
        }
        
        // Parse constraint name from Oracle error message
        // Pattern: ORA-00001: unique constraint (SCHEMA.CONSTRAINT_NAME) violated
        Pattern constraintPattern = Pattern.compile("unique constraint \\([^.]+\\.([^)]+)\\)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = constraintPattern.matcher(errorMessage);
        
        if (matcher.find()) {
            String constraintName = matcher.group(1);
            String userMessage = getConstraintViolationMessage(constraintName, errorMessage);
            if (userMessage != null) {
                return ApiResponse.conflict(userMessage);
            }
        }
        
        // Fallback for other constraint violations
        if (errorMessage.toLowerCase().contains("unique constraint") || 
            errorMessage.toLowerCase().contains("duplicate key")) {
            return ApiResponse.conflict("A record with the same information already exists. Please use a different value.");
        }
        
        // Generic data integrity error
        log.error("Unhandled DataIntegrityViolationException: {}", errorMessage);
        return ApiResponse.error("Data integrity violation occurred", HttpStatus.CONFLICT.value());
    }
    
    private String getConstraintViolationMessage(String constraintName, String errorMessage) {
        // Map known constraint names to user-friendly messages
        if (constraintName != null) {
            switch (constraintName.toUpperCase()) {
                case "STOCK_MASTER_UK2":
                    // Extract stock name from error message if possible, otherwise generic message
                    Pattern stockNamePattern = Pattern.compile("stock[_-]?name[\\s=:]+['\"]?([^'\"\\s]+)['\"]?", Pattern.CASE_INSENSITIVE);
                    Matcher stockNameMatcher = stockNamePattern.matcher(errorMessage);
                    if (stockNameMatcher.find()) {
                        String stockName = stockNameMatcher.group(1);
                        return String.format("Stock name '%s' already exists for this group. Please use a different name.", stockName);
                    }
                    return "Stock name already exists for this group. Please use a different name.";
                case "STOCK_MASTER_UK1":
                    return "Stock code already exists. Please use a different code.";
                default:
                    // Try to extract field name from constraint name
                    if (constraintName.contains("_UK") || constraintName.contains("_PK")) {
                        return String.format("A record with this information already exists (constraint: %s). Please use a different value.", constraintName);
                    }
            }
        }
        return null;
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRuntimeException(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error at {} ", request.getRequestURI(), ex);
        return ApiResponse.error(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneralException(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error at {} ", request.getRequestURI(), ex);
        return ApiResponse.error(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
    }
}
