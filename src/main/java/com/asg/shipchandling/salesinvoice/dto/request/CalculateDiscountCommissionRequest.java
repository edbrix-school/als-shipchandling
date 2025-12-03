package com.asg.shipchandling.salesinvoice.dto.request;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;



@Data
@NoArgsConstructor
@AllArgsConstructor
public class CalculateDiscountCommissionRequest {
    private Long invDiscount;
    private Long incentiveAmt;
    private Long incentiveAmt2;
    private Long incentiveAmt3;
    private String type;
    private Long incentivePercent;
    private Long incentivePercent2;
    private Long incentivePercent3;
    private Long baseAmt; // Required for item level calculation
}
