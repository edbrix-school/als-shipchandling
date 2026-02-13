package com.asg.shipchandling.salesinvoice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefreshGpProcResponse {
    private Boolean success;
    private String message;
    private Long discountPercent;
    private Long discountAmt;
    private Long incentivePercent;
    private Long incentivePercent2;
    private Long incentivePercent3;
    private Long incentiveAmt;
    private Long incentiveAmt2;
    private Long incentiveAmt3;
    private Long totalGpAmt;
    private Long totalGpPercent;
    private Long invAmount;
}
