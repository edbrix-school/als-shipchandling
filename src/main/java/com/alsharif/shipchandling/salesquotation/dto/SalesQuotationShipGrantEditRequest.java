package com.alsharif.shipchandling.salesquotation.dto;

import java.math.BigDecimal;

public record SalesQuotationShipGrantEditRequest(
        BigDecimal groupId,
        BigDecimal userPoid,
        String docId,
        BigDecimal docKeyPoid
) {
}

