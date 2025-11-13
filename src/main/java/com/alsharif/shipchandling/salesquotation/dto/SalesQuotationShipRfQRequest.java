package com.alsharif.shipchandling.salesquotation.dto;

import java.math.BigDecimal;

public record SalesQuotationShipRfQRequest(
        BigDecimal groupId,
        BigDecimal companyId,
        BigDecimal transactionPoid,
        String loginUser
) {
}

