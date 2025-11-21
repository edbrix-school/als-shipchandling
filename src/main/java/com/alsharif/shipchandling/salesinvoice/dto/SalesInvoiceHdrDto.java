package com.alsharif.shipchandling.salesinvoice.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.sql.Timestamp;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesInvoiceHdrDto {
    private Long transactionPoid;
    private String docRef;
    private Timestamp transactionDate;
    private Long groupPoid;
    private Long companyPoid;
    private String partyType;
    private Long customerPoid;
    private String customerName; // From joined SALES_CUSTOMER_MASTER table
    private Long principalPoid;
    private Long customerAddrPoid;
    private String currencyCode;
    private Long currencyRate;
    private Long invAmount;
    private Long creditDays;
    private Timestamp dueDate;
    private String qtnPoid;
    private String status;
    private String invStatus;
    private Long discountPercent;
    private Long discountAmt;
    private Long invDiscount;
    private Long incentivePercent;
    private Long incentiveAmt;
    private String incentiveTo;
    private Long incentivePercent2;
    private Long incentiveAmt2;
    private String incentiveTo2;
    private Long incentivePercent3;
    private Long incentiveAmt3;
    private String incentiveTo3;
    private Long totalGpAmt;
    private Long totalGpPercent;
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
    private Timestamp createdDate;
    private String lastmodifiedBy;
    private Timestamp lastmodifiedDate;
    
    // Detail tables
    private List<SalesInvoiceDtlDto> invoiceDetails;
    private List<SalesDnDtlDto> deliveryNoteDetails;
    private List<SalesInvCostbkdDtlDto> costBookedDetails;
}
