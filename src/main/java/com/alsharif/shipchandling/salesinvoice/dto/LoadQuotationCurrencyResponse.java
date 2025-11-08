package com.alsharif.shipchandling.salesinvoice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;



@Data
@NoArgsConstructor
@AllArgsConstructor
class LoadQuotationCurrencyResponse {
    private Boolean success;
    private String message;
    private String currencyCode;
    private Long currencyRate;
}
