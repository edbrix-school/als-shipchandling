package com.asg.shipchandling.salesquotation.dto;

import java.math.BigDecimal;

public record SalesQuotationShipApprovalResponse(
        String status,
        String message,
        BigDecimal nextApprover
) {
}

