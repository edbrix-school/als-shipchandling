package com.asg.shipchandling.salesinvoice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;


// Invoice Details DTO
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesInvoiceDtlDto {
    private Long transactionPoid;
    private Long detRowId;
    private Long stockPoid;
    private Long stockUnitPoid;
    private BigDecimal quantity;
    private BigDecimal price;
    private BigDecimal discount;
    private BigDecimal amount;
    private BigDecimal baseAmt;
    private Long taxPoid;
    private BigDecimal taxPercentage;
    private BigDecimal taxAmount;
    private BigDecimal netSales;
    private BigDecimal netDiscount;
    private BigDecimal incentive;
    private BigDecimal itemGp;
    private BigDecimal itemGpPer;
    private BigDecimal costAmt;
    private BigDecimal purCost;
    private BigDecimal purchasePrice;
    private BigDecimal purchaseQty;
    private String itemType;
    private String costCenterPoid;
    private Long dnPoidLinkFk;
    private Long detRowIdChrgFk;
    private Long quotationPoid;
    private Long quotationDetRowId;
    private String remarks;
    private String createdBy;
    private LocalDateTime createdDate;
    private String lastmodifiedBy;
    private LocalDateTime lastmodifiedDate;
    
    // LOV Details
    private LovDetailDto stockDetails;
    private LovDetailDto costCenterDetails;
    private LovDetailDto taxDetails;
    
    // Inner class for LOV details
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LovDetailDto {
        private Long poid;
        private String code;
        private String description;
    }
}
