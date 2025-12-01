package com.alsharif.shipchandling.salesinvoice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;



@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateSalesInvoiceDtlRequest {
    private Long stockPoid;
    private Long stockUnitPoid;
    private Long quantity;
    private BigDecimal price;
    private Long discount;
    private Long baseAmt;
    private Long taxPoid;
    private String costCenterPoid;
    private String remarks;
    // Action type: "isCreated" to create new, "isDeleted" or "delRowId" to delete
    // Note: This field is only used in create/update requests, not in response/view APIs
    private String actionType;
}
