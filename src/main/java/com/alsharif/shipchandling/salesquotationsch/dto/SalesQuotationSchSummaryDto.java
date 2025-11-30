package com.alsharif.shipchandling.salesquotationsch.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesQuotationSchSummaryDto {
    private Long transactionPoid;
    private Timestamp transactionDate;
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

