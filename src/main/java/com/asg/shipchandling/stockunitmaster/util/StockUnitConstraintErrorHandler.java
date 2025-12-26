package com.asg.shipchandling.stockunitmaster.util;

import com.asg.shipchandling.stockunitmaster.exception.StockUnitConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Utility class to handle database constraint violations for STOCK_UNIT_MASTER table
 * and convert them into user-friendly error messages with detailed reasons.
 */
public class StockUnitConstraintErrorHandler {
    
    private static final Logger log = LoggerFactory.getLogger(StockUnitConstraintErrorHandler.class);
    
    /**
     * Handles DataIntegrityViolationException and throws a user-friendly exception
     * with detailed reason for each constraint violation.
     * 
     * @param ex The DataIntegrityViolationException to handle
     * @throws StockUnitConstraintViolationException with user-friendly message if it's a known constraint
     */
    public static void handleConstraintViolation(DataIntegrityViolationException ex) {
        String errorMessage = ex.getMessage();
        if (errorMessage == null) {
            errorMessage = ex.getCause() != null ? ex.getCause().getMessage() : "Data integrity violation";
        }
        
        // Get root cause message for better constraint name extraction
        Throwable rootCause = ex.getRootCause();
        if (rootCause != null && rootCause.getMessage() != null) {
            errorMessage = rootCause.getMessage();
        }
        
        log.debug("Processing constraint violation: {}", errorMessage);
        
        // Pattern 1: Unique constraint violation (ORA-00001)
        // Format: ORA-00001: unique constraint (SCHEMA.CONSTRAINT_NAME) violated
        Pattern uniqueConstraintPattern = Pattern.compile("unique constraint \\([^.]+\\.([^)]+)\\)", Pattern.CASE_INSENSITIVE);
        Matcher uniqueMatcher = uniqueConstraintPattern.matcher(errorMessage);
        
        if (uniqueMatcher.find()) {
            String constraintName = uniqueMatcher.group(1);
            String userMessage = getUniqueConstraintMessage(constraintName, errorMessage);
            if (userMessage != null) {
                throw new StockUnitConstraintViolationException(userMessage, constraintName, "UNIQUE");
            }
        }
        
        // Pattern 2: Foreign key constraint violation - parent key not found (ORA-02291)
        // Format: ORA-02291: integrity constraint (SCHEMA.CONSTRAINT_NAME) violated - parent key not found
        Pattern fkParentPattern = Pattern.compile("integrity constraint \\([^.]+\\.([^)]+)\\) violated.*parent key not found", Pattern.CASE_INSENSITIVE);
        Matcher fkParentMatcher = fkParentPattern.matcher(errorMessage);
        
        if (fkParentMatcher.find()) {
            String constraintName = fkParentMatcher.group(1);
            String userMessage = getForeignKeyParentMessage(constraintName, errorMessage);
            if (userMessage != null) {
                throw new StockUnitConstraintViolationException(userMessage, constraintName, "FK_PARENT");
            }
        }
        
        // Pattern 3: Foreign key constraint violation - child record found (ORA-02292)
        // Format: ORA-02292: integrity constraint (SCHEMA.CONSTRAINT_NAME) violated - child record found
        Pattern fkChildPattern = Pattern.compile("integrity constraint \\([^.]+\\.([^)]+)\\) violated.*child record found", Pattern.CASE_INSENSITIVE);
        Matcher fkChildMatcher = fkChildPattern.matcher(errorMessage);
        
        if (fkChildMatcher.find()) {
            String constraintName = fkChildMatcher.group(1);
            String userMessage = getForeignKeyChildMessage(constraintName, errorMessage);
            if (userMessage != null) {
                throw new StockUnitConstraintViolationException(userMessage, constraintName, "FK_CHILD");
            }
        }
        
        // Pattern 4: Primary key constraint violation (ORA-00001)
        // Format: ORA-00001: unique constraint (SCHEMA.CONSTRAINT_NAME) violated (for PK)
        Pattern pkPattern = Pattern.compile("(primary key|unique constraint) \\([^.]+\\.([^)]+)\\)", Pattern.CASE_INSENSITIVE);
        Matcher pkMatcher = pkPattern.matcher(errorMessage);
        
        if (pkMatcher.find()) {
            String constraintName = pkMatcher.group(2);
            String userMessage = getPrimaryKeyMessage(constraintName, errorMessage);
            if (userMessage != null) {
                throw new StockUnitConstraintViolationException(userMessage, constraintName, "PK");
            }
        }
        
        // If we reach here, it's not a known STOCK_UNIT_MASTER constraint
        // Let the exception propagate to be handled by global handler
    }
    
    /**
     * Returns user-friendly message for unique constraint violations
     */
    private static String getUniqueConstraintMessage(String constraintName, String errorMessage) {
        String upperConstraintName = constraintName.toUpperCase();
        
        switch (upperConstraintName) {
            case "STOCK_UNIT_MASTER_UK1":
                // Unique constraint on STOCK_UNIT_CODE
                // Reason: Stock unit code must be unique across all groups
                Pattern codePattern = Pattern.compile("stock[_-]?unit[_-]?code[\\s=:]+['\"]?([^'\"\\s]+)['\"]?", Pattern.CASE_INSENSITIVE);
                Matcher codeMatcher = codePattern.matcher(errorMessage);
                if (codeMatcher.find()) {
                    String unitCode = codeMatcher.group(1);
                    return String.format(
                        "Unit code '%s' already exists.",
                        unitCode
                    );
                }
                return "Unit code already exists.";
                
            case "STOCK_UNIT_MASTER_UK2":
                // Unique constraint on STOCK_UNIT_NAME
                // Reason: Stock unit name must be unique to prevent confusion
                Pattern namePattern = Pattern.compile("stock[_-]?unit[_-]?name[\\s=:]+['\"]?([^'\"\\s]+)['\"]?", Pattern.CASE_INSENSITIVE);
                Matcher nameMatcher = namePattern.matcher(errorMessage);
                if (nameMatcher.find()) {
                    String unitName = nameMatcher.group(1);
                    return String.format(
                        "Unit name '%s' already exists. Please use a different name.",
                        unitName
                    );
                }
                return "Unit name already exists. Please use a different name.";
                
            default:
                return null;
        }
    }
    
    /**
     * Returns user-friendly message for foreign key parent key not found violations
     */
    private static String getForeignKeyParentMessage(String constraintName, String errorMessage) {
        String upperConstraintName = constraintName.toUpperCase();
        
        switch (upperConstraintName) {
            case "STOCK_UNIT_MASTER_FK1":
                // Foreign key constraint on GROUP_POID referencing GLOBAL_GROUP_MASTER
                // Reason: The group must exist before creating a stock unit
                Pattern groupPattern = Pattern.compile("group[_-]?poid[\\s=:]+['\"]?([^'\"\\s]+)['\"]?", Pattern.CASE_INSENSITIVE);
                Matcher groupMatcher = groupPattern.matcher(errorMessage);
                if (groupMatcher.find()) {
                    String groupPoid = groupMatcher.group(1);
                    return String.format(
                        "The specified group (ID: %s) does not exist.",
                        groupPoid
                    );
                }
                return "The specified group does not exist.";
                
            default:
                return null;
        }
    }
    
    /**
     * Returns user-friendly message for foreign key child record found violations
     */
    private static String getForeignKeyChildMessage(String constraintName, String errorMessage) {
        String upperConstraintName = constraintName.toUpperCase();
        
        switch (upperConstraintName) {
            case "STOCK_UNIT_MASTER_FK1":
                // This would occur if trying to delete a group that has stock units
                // Reason: Cannot delete parent when children exist
                return "Cannot delete the group because it is being used by one or more units. ";
                
            default:
                return null;
        }
    }
    
    /**
     * Returns user-friendly message for primary key violations
     */
    private static String getPrimaryKeyMessage(String constraintName, String errorMessage) {
        String upperConstraintName = constraintName.toUpperCase();
        
        switch (upperConstraintName) {
            case "STOCK_UNIT_MASTER_PK":
                // Primary key constraint on STOCK_UNIT_POID
                // Reason: Primary key must be unique
                Pattern poidPattern = Pattern.compile("stock[_-]?unit[_-]?poid[\\s=:]+['\"]?([^'\"\\s]+)['\"]?", Pattern.CASE_INSENSITIVE);
                Matcher poidMatcher = poidPattern.matcher(errorMessage);
                if (poidMatcher.find()) {
                    String poid = poidMatcher.group(1);
                    return String.format(
                        "A Unit with ID '%s' already exists.",
                        poid
                    );
                }
                return "A Unit with this ID already exists.";
                
            default:
                return null;
        }
    }
}

