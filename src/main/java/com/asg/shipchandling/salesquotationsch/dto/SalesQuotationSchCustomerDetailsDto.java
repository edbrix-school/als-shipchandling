package com.asg.shipchandling.salesquotationsch.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesQuotationSchCustomerDetailsDto {
    private String currencyCode;
    private Long currencyRate;
    private String paymentMode;
}
