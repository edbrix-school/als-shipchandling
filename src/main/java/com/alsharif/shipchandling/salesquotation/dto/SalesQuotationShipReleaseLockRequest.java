package com.alsharif.shipchandling.salesquotation.dto;

import java.math.BigDecimal;

public record SalesQuotationShipReleaseLockRequest(
        BigDecimal groupId,
        BigDecimal companyId,
        BigDecimal userPoid,
        String docId,
        BigDecimal docKeyPoid,
        String requestMetadata
) {
}

