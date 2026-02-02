package com.asg.shipchandling.salesquotationsch.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesQuotationSchCustomerDetailsDto {
    private String currencyCode;
    private BigDecimal currencyRate;
    private String paymentMode;
}
