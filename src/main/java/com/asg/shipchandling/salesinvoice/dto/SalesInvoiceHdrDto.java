package com.asg.shipchandling.salesinvoice.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesInvoiceHdrDto {
    private Long transactionPoid;
    private String docRef;
    private LocalDateTime transactionDate;
    private Long groupPoid;
    private Long companyPoid;
    private String partyType;
    private Long customerPoid;
    private String customerName; // From joined SALES_CUSTOMER_MASTER table
    private Long principalPoid;
    private Long customerAddrPoid;
    private String currencyCode;
    private Long currencyRate;
    private BigDecimal invAmount;
    private Long creditDays;
    private LocalDateTime dueDate;
    private Long qtnPoid;
    private String status;
    private String invStatus;
    private BigDecimal discountPercent;
    private BigDecimal discountAmt;
    private BigDecimal invDiscount;
    private BigDecimal incentivePercent;
    private BigDecimal incentiveAmt;
    private String incentiveTo;
    private BigDecimal incentivePercent2;
    private BigDecimal incentiveAmt2;
    private String incentiveTo2;
    private BigDecimal incentivePercent3;
    private BigDecimal incentiveAmt3;
    private String incentiveTo3;
    private BigDecimal totalGpAmt;
    private BigDecimal totalGpPercent;
    private Long totalCost;
    private String paymentMode;
    private String dataLoadType;
    private String vesselName;
    private String portName;
    private String descriptionPrintYn;
    private String deliveryToAddress;
    private String details;
    private String remarks;
    private String lpoDetails;
    private String lpoNumber;
    private String contractRefNumber;
    private String costRefNumber;
    private String fdaRef;
    private Long printDivisionPoid;
    private String dnPoid;
    private String verified;
    private String pjLoadStatus;
    private String postWithSplRights;
    private String authorizedId;
    private String deleted;
    private String createdBy;
    private LocalDateTime createdDate;
    private String lastmodifiedBy;
    private LocalDateTime lastmodifiedDate;
    
    // Detail tables
    private List<SalesInvoiceDtlDto> invoiceDetails;
    private List<SalesDnDtlDto> deliveryNoteDetails;
    private List<SalesInvCostbkdDtlDto> costBookedDetails;
    
    // LOV Details
    private LovDetailDto customerDetails;
    private LovDetailDto principalDetails;
    private LovDetailDto qtnDetails;
    private LovDetailDto printDivisionDetails;
    private LovDetailDto dnDetails;
    private LovDetailDto fdaDetails;
    
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
