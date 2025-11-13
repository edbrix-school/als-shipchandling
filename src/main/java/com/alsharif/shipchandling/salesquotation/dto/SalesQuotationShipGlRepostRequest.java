package com.alsharif.shipchandling.salesquotation.dto;

import java.math.BigDecimal;

public record SalesQuotationShipGlRepostRequest(
        BigDecimal groupId,
        BigDecimal companyId,
        BigDecimal userPoid,
        String docId,
        BigDecimal docKeyPoid,
        String docRef
) {
}

