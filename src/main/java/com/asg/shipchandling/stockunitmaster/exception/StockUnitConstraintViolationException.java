package com.asg.shipchandling.stockunitmaster.exception;

/**
 * Custom exception for stock unit constraint violations.
 * This exception is thrown when a database constraint is violated
 * and provides user-friendly error messages with detailed reasons.
 */
public class StockUnitConstraintViolationException extends RuntimeException {
    
    private final String constraintName;
    private final String violationType;
    
    public StockUnitConstraintViolationException(String message, String constraintName, String violationType) {
        super(message);
        this.constraintName = constraintName;
        this.violationType = violationType;
    }
    
    public StockUnitConstraintViolationException(String message) {
        super(message);
        this.constraintName = null;
        this.violationType = null;
    }
    
    public String getConstraintName() {
        return constraintName;
    }
    
    public String getViolationType() {
        return violationType;
    }
}

