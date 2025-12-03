package com.asg.shipchandling.salesquotation.dto;

import java.math.BigDecimal;

public record SalesQuotationShipDefaultDetailResponse(
        BigDecimal outValue1,
        BigDecimal outValue2,
        BigDecimal outValue3
) {
}

