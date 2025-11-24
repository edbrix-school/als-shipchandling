package com.alsharif.shipchandling.salesquotation.dto;

import java.math.BigDecimal;

public record SalesQuotationShipRefreshDetailRequest(
        BigDecimal groupId,
        BigDecimal companyId,
        BigDecimal transactionPoid,
        String quotedRate
) {
}

