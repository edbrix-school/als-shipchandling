package com.asg.shipchandling.salesinvoice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoadQuotationItemsRequest {
    private Long qtnPoid;
    private BigDecimal incentiveAmt;
    private BigDecimal incentiveAmt2;
    private BigDecimal incentiveAmt3;
}
