package com.asg.shipchandling.salesquotationsch.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;



@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesQuotationSchSummaryDto {
    private Long transactionPoid;
    private LocalDateTime transactionDate;
    private String docRef;
    private Long companyPoid;
    private BigDecimal customerPoid;
    private String quotationStatus;
    private LocalDate validityToDate;

    private BigDecimal totalAmount;
    private BigDecimal totalTax;
    private BigDecimal totalGpAmt;
    private String currencyCode;
    private String customerRef;
    private String vesselName;
    private Long salesmanPoid;
}

