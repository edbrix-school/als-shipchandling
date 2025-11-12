package com.alsharif.shipchandling.requestforquotation.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.sql.Timestamp;

// Item Details DTO
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApRequestForQtnItemDtlDto {
    private Long transactionPoid;
    private Long detRowId;
    private Long stockPoid;
    private Long stockUnitPoid;
    private BigDecimal qty;
    private Long supplierPoid;
    private BigDecimal price;
    private Long taxPoid;
    private BigDecimal taxPercentage;
    private BigDecimal taxAmount;
    private BigDecimal lastRate;
    private BigDecimal purchaseQty;
    private BigDecimal purchasePrice;
    private String refDocId;
    private String refPoid;
    private String remarks;
    private String createdBy;
    private Timestamp createdDate;
    private String lastmodifiedBy;
    private Timestamp lastmodifiedDate;
}