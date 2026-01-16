package com.asg.shipchandling.salesquotationsch.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesQuotationSchSummaryDto {
    private Long transactionPoid;
    private LocalDate transactionDate;
    private String docRef;
    private Long companyPoid;
    private Long customerPoid;
    private String quotationStatus;
    private Timestamp validityToDate;
    private Long totalAmount;
    private Long totalTax;
    private Long totalGpAmt;
    private String currencyCode;
    private String customerRef;
    private String vesselName;
    private Long salesmanPoid;
}

