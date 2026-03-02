package com.asg.shipchandling.salesinvoice.dto;

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
    private BigDecimal incentiveAmt;
    private BigDecimal incentivePercent;
    private BigDecimal incentiveAmt2;
    private BigDecimal incentivePercent2;
    private BigDecimal incentiveAmt3;
    private BigDecimal incentivePercent3;
    private BigDecimal discountAmt;
    private BigDecimal discountPercent;
    private BigDecimal totalGpAmt;
    private BigDecimal totalGpPercent;
    private BigDecimal invAmount;
}
