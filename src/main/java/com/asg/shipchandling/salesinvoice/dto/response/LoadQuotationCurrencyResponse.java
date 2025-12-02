package com.asg.shipchandling.salesinvoice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import com.asg.shipchandling.salesinvoice.dto.QuotationCurrencyDto;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoadQuotationCurrencyResponse {
    private Boolean success;
    private String message;
    private String currencyCode;
    private Long currencyRate;
    private List<QuotationCurrencyDto> quotationCurrencyList;
}
