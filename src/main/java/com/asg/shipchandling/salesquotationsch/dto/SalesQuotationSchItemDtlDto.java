package com.asg.shipchandling.salesquotationsch.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesQuotationSchItemDtlDto {
    private Long transactionPoid;
    private Long detRowId;
    private Long stockPoid;
    private Long quantity;
    private Long price;
    private Long discount;
    private Long amount;
    private String remarks;
    private Long stockUnitPoid;
    private String createdBy;
    private Timestamp createdDate;
    private String lastmodifiedBy;
    private Timestamp lastmodifiedDate;
    private Long adjQuantity;
    private Long cost;
    private Long lastRate1;
    private Long lastRate2;
    private String deliverySelect;
    private String dnRefNo;
    private Long gpAmount;
    private Long gpPercentage;
    private Long totCost;
    private Long purchasePrice;
    private Long purchaseQty;
    private String itemType;
    private String refDocId;
    private Long refPoid;
    private Long taxPoid;
    private Long taxAmount;
    private Long taxPercentage;
    private String vatModified;
}
