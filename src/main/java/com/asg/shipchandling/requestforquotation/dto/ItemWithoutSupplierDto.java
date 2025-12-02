package com.asg.shipchandling.requestforquotation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemWithoutSupplierDto {
    private Long transactionPoid;
    private Long detRowId;
    private Long stockPoid;
    private String stockName;
    private BigDecimal qty;
}