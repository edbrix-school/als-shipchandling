package com.alsharif.shipchandling.requestforquotation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LastPriceResponse {
    private BigDecimal lastPrice;
}