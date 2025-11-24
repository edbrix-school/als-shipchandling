package com.alsharif.shipchandling.salesquotation.dto;

import java.math.BigDecimal;

public record SalesQuotationShipCalculateRequest(
        BigDecimal groupId,
        BigDecimal companyId,
        String loginUser,
        BigDecimal transactionPoid
) {
}

