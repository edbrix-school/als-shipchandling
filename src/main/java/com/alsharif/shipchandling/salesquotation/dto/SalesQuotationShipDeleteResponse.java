package com.alsharif.shipchandling.salesquotation.dto;

/**
 * Response DTO for Sales Quotation deletion operation.
 * Contains information about whether deletion is allowed and any dependencies.
 */
public class SalesQuotationShipDeleteResponse {
    private boolean canDelete;
    private String reason;
    private String message;
    private Integer salesInvoiceCount;
    private Integer dependencyCount;
    
    public SalesQuotationShipDeleteResponse() {
    }
    
    public SalesQuotationShipDeleteResponse(boolean canDelete, String reason, String message) {
        this.canDelete = canDelete;
        this.reason = reason;
        this.message = message;
    }
    
    public static SalesQuotationShipDeleteResponse success() {
        return new SalesQuotationShipDeleteResponse(true, "No dependencies found", "Sales quotation deleted successfully");
    }
    
    public static SalesQuotationShipDeleteResponse blocked(String reason, String message) {
        return new SalesQuotationShipDeleteResponse(false, reason, message);
    }
    
    public static SalesQuotationShipDeleteResponse blockedWithDependencies(String reason, String message, Integer salesInvoiceCount) {
        SalesQuotationShipDeleteResponse response = new SalesQuotationShipDeleteResponse(false, reason, message);
        response.setSalesInvoiceCount(salesInvoiceCount);
        response.setDependencyCount(salesInvoiceCount);
        return response;
    }
    
    public boolean isCanDelete() {
        return canDelete;
    }
    
    public void setCanDelete(boolean canDelete) {
        this.canDelete = canDelete;
    }
    
    public String getReason() {
        return reason;
    }
    
    public void setReason(String reason) {
        this.reason = reason;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public Integer getSalesInvoiceCount() {
        return salesInvoiceCount;
    }
    
    public void setSalesInvoiceCount(Integer salesInvoiceCount) {
        this.salesInvoiceCount = salesInvoiceCount;
    }
    
    public Integer getDependencyCount() {
        return dependencyCount;
    }
    
    public void setDependencyCount(Integer dependencyCount) {
        this.dependencyCount = dependencyCount;
    }
}

