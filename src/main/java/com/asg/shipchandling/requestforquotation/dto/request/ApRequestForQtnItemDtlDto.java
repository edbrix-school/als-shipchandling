package com.asg.shipchandling.requestforquotation.dto.request;

import com.asg.shipchandling.commonlov.dto.LovItem;
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
    private LovItem stockPoidDetails;
    private Long stockUnitPoid;
    private LovItem stockUnitDetails;
    private BigDecimal qty;
    private Long supplierPoid;
    private LovItem supplierPoidDetails;
    private BigDecimal price;
    private Long taxPoid;
    private LovItem taxPoidDetails;
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