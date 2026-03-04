package com.asg.shipchandling.salesinvoice.dto.response;

import com.asg.shipchandling.salesinvoice.dto.SalesInvoiceHdrDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefreshGpProcResponse {
    private Boolean success;
    private String message;
    private BigDecimal discountPercent;
    private BigDecimal discountAmt;
    private BigDecimal incentivePercent;
    private BigDecimal incentivePercent2;
    private BigDecimal incentivePercent3;
    private BigDecimal incentiveAmt;
    private BigDecimal incentiveAmt2;
    private BigDecimal incentiveAmt3;
    private BigDecimal totalGpAmt;
    private BigDecimal totalGpPercent;
    private BigDecimal invAmount;
    private SalesInvoiceHdrDto invoice;
}
