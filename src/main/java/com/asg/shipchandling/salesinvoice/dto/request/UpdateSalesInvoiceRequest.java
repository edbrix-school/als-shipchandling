package com.asg.shipchandling.salesinvoice.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.sql.Timestamp;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSalesInvoiceRequest {
   
    @NotNull(message = "Transaction date is required")
    private Timestamp transactionDate;

    @NotNull(message = "Party type is required")
    @Size(max = 100, message = "Party type must not exceed 100 characters")
    private String partyType; // CUSTOMER or PRINCIPAL

    private Long customerPoid; // Required if partyType = CUSTOMER
    private Long principalPoid; // Required if partyType = PRINCIPAL
    private Long customerAddrPoid;
    private String currencyCode;
    private Long currencyRate;
    private Long creditDays;
    private Long qtnPoid;
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
    private Long incentivePercent;
    private Long incentiveAmt;
    private String incentiveTo;
    private Long incentivePercent2;
    private Long incentiveAmt2;
    private String incentiveTo2;
    private Long incentivePercent3;
    private Long incentiveAmt3;
    private String incentiveTo3;
    private String authorizedId;
    private String verified;

    // Detail tables
    private List<UpdateSalesInvoiceDtlRequest> invoiceDetails;
    private List<UpdateSalesDnDtlRequest> deliveryNoteDetails;
}