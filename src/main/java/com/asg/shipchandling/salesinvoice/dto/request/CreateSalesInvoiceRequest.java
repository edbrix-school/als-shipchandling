package com.asg.shipchandling.salesinvoice.dto.request;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateSalesInvoiceRequest {
    
    @NotNull(message = "Transaction date is required")
    private LocalDateTime transactionDate;

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
    private BigDecimal incentivePercent;
    private BigDecimal incentiveAmt;
    private String incentiveTo;
    private BigDecimal incentivePercent2;
    private BigDecimal incentiveAmt2;
    private String incentiveTo2;
    private BigDecimal incentivePercent3;
    private BigDecimal incentiveAmt3;
    private String incentiveTo3;
    private String authorizedId;
    private String verified;

    // Detail tables
    private List<CreateSalesInvoiceDtlRequest> invoiceDetails;
    private List<CreateSalesDnDtlRequest> deliveryNoteDetails;
}
