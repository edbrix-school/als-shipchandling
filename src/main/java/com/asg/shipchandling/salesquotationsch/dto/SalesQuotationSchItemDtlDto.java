package com.asg.shipchandling.salesquotationsch.dto;

import com.asg.shipchandling.StockMaster.dto.StockDetailsResponse;
import com.asg.shipchandling.commonlov.dto.LovItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesQuotationSchItemDtlDto {
    private Long transactionPoid;
    private Long detRowId;
    private Long stockPoid;
    private LovItem stockPoidDetails;
    private StockDetailsResponse.CategoryDetailDto categoryDetails;
    private BigDecimal quantity;
    private BigDecimal price;
    private BigDecimal discount;
    private BigDecimal amount;
    private String remarks;
    private Long stockUnitPoid;
    private LovItem stockUnitDetails;
    private String createdBy;
    private LocalDateTime createdDate;
    private String lastmodifiedBy;
    private LocalDateTime lastmodifiedDate;
    private BigDecimal adjQuantity;
    private BigDecimal cost;
    private BigDecimal lastRate1;
    private BigDecimal lastRate2;
    private String deliverySelect;
    private String dnRefNo;
    private BigDecimal gpAmount;
    private BigDecimal gpPercentage;
    private BigDecimal totCost;
    private BigDecimal purchasePrice;
    private BigDecimal purchaseQty;
    private String itemType;
    private String refDocId;
    private Long refPoid;
    private Long taxPoid;
    private LovItem taxPoidDetails;
    private BigDecimal taxAmount;
    private BigDecimal taxPercentage;
    private String vatModified;
}
