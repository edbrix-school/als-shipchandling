package com.asg.shipchandling.salesinvoice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Delivery Note Details DTO
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuotationCurrencyDto {
    private Long currencyCode;
    private Long currencyRate;
}
