package com.alsharif.shipchandling.salesinvoice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.sql.Timestamp;

// Invoice Details DTO
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesInvoiceDtlDto {
    private Long transactionPoid;
    private Long detRowId;
    private Long stockPoid;
    private Long stockUnitPoid;
    private Long quantity;
    private BigDecimal price;
    private Long discount;
    private Long amount;
    private Long baseAmt;
    private Long taxPoid;
    private Long taxPercentage;
    private Long taxAmount;
    private Long netSales;
    private Long netDiscount;
    private Long incentive;
    private Long incentivePercent;
    private Long incentiveAmt;
    private Long incentivePercent2;
    private Long incentiveAmt2;
    private Long incentivePercent3;
    private Long incentiveAmt3;
    private Long itemGp;
    private Long itemGpPer;
    private Long costAmt;
    private Long purCost;
    private Long purchasePrice;
    private Long purchaseQty;
    private String itemType;
    private Long costCenterPoid;
    private Long dnPoidLinkFk;
    private Long detRowIdChrgFk;
    private Long quotationPoid;
    private Long quotationDetRowId;
    private String remarks;
    private String createdBy;
    private Timestamp createdDate;
    private String lastmodifiedBy;
    private Timestamp lastmodifiedDate;
}
