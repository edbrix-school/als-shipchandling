package com.asg.shipchandling.salesinvoice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;



@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSalesInvoiceDtlRequest {
    private Long detRowId;
    private Long stockPoid;
    private Long stockUnitPoid;
    private BigDecimal quantity;
    private BigDecimal price;
    private BigDecimal discount;
    private BigDecimal baseAmt;
    private Long taxPoid;
    private String costCenterPoid;
    private String remarks;
    // Action type: "isCreated" (create new), "isUpdated" (update existing), "noChanges" (no changes), 
    // "isDeleted" or "delRowId" (delete). Note: This field is only used in create/update requests, not in response/view APIs
    private String actionType;
}
