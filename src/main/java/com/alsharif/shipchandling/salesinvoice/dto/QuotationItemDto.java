package com.alsharif.shipchandling.salesinvoice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuotationItemDto {
    private Long stockPoid;
    private Long stockUnitPoid;
    private Long quantity;
    private BigDecimal price;
    private Long discount;
    private Long baseAmt;
    private Long taxPoid;
    private Long incentiveAmt;
    private Long incentivePercent;
    private Long incentiveAmt2;
    private Long incentivePercent2;
    private Long incentiveAmt3;
    private Long incentivePercent3;
    private Long discountAmt;
    private Long discountPercent;
    private Long totalGpAmt;
    private Long totalGpPercent;
    private Long invAmount;
}
