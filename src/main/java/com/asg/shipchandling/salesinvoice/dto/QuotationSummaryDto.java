package com.asg.shipchandling.salesinvoice.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuotationSummaryDto {
    private String status;
    private String dataLoadType;
    private String paymentMode;
    private String vesselName;
    private String portName;
    private String currencyCode;
    private BigDecimal currencyRate;
    private BigDecimal invDiscount;
    private BigDecimal discountAmt;
    private BigDecimal discountPercent;
    private String details;
    private BigDecimal invAmount;
    private BigDecimal totalGpAmt;
    private BigDecimal totalGpPercent;
    private String descriptionPrintYn;
    private String deliveryToAddress;
}
