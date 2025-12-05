package com.asg.shipchandling.salesquotationsch.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CurrencyRateResponse {
    private Long currencyPoid;
    private String currencyCode;
    private String currencyName;
    private BigDecimal buyRate;
    private BigDecimal sellRate;
    private Date rateDate;
}

