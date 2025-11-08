package com.alsharif.shipchandling.salesinvoice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CalculateDiscountCommissionResponse {
    private Boolean success;
    private String message;
    private Long netSales;
    private Long incentive;
}
