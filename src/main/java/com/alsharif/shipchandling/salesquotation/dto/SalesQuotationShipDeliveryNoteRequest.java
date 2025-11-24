package com.alsharif.shipchandling.salesquotation.dto;

import java.math.BigDecimal;

public record SalesQuotationShipDeliveryNoteRequest(
        BigDecimal groupId,
        BigDecimal companyId,
        BigDecimal transactionPoid,
        String loginUser
) {
}

