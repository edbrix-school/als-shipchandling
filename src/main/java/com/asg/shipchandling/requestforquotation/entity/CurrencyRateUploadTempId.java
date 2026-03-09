package com.asg.shipchandling.requestforquotation.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class CurrencyRateUploadTempId implements Serializable {

    private BigDecimal groupPoid;
    private BigDecimal companyPoid;
    private String currencyCode;
    private LocalDateTime effectiveDate;
}
