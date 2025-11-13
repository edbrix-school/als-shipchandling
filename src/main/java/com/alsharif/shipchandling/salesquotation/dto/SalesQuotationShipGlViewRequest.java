package com.alsharif.shipchandling.salesquotation.dto;

import java.math.BigDecimal;

public record SalesQuotationShipGlViewRequest(
        BigDecimal groupId,
        BigDecimal companyId,
        String docId,
        BigDecimal docKeyPoid
) {
}

