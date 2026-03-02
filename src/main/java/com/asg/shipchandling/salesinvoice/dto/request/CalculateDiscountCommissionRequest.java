package com.asg.shipchandling.salesinvoice.dto.request;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CalculateDiscountCommissionRequest {
    private BigDecimal invDiscount;
    private BigDecimal incentiveAmt;
    private BigDecimal incentiveAmt2;
    private BigDecimal incentiveAmt3;
    private String type;
    private BigDecimal incentivePercent;
    private BigDecimal incentivePercent2;
    private BigDecimal incentivePercent3;
    private Long baseAmt; // Required for item level calculation
}
