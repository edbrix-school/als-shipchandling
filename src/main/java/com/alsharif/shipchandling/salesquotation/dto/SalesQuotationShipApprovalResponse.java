package com.alsharif.shipchandling.salesquotation.dto;

import java.math.BigDecimal;

public record SalesQuotationShipApprovalResponse(
        String status,
        String message,
        BigDecimal nextApprover
) {
}

