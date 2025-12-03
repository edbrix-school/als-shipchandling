package com.asg.shipchandling.salesinvoice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import com.asg.shipchandling.salesinvoice.dto.QuotationItemDto;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CalculateDiscountCommissionResponse {
    private Boolean success;
    private String message;
    private Long netSales;
    private Long incentive;
    private List<QuotationItemDto> items;
}
