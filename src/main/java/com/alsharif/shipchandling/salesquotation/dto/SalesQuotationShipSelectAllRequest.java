package com.alsharif.shipchandling.salesquotation.dto;

import java.math.BigDecimal;

public record SalesQuotationShipSelectAllRequest(
        BigDecimal groupId,
        BigDecimal companyId,
        BigDecimal transactionPoid,
        String selectionFlag
) {
}

