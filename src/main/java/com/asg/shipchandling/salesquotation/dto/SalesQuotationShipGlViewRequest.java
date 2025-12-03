package com.asg.shipchandling.salesquotation.dto;

import java.math.BigDecimal;

public record SalesQuotationShipGlViewRequest(
        BigDecimal groupId,
        BigDecimal companyId,
        String docId,
        BigDecimal docKeyPoid
) {
}

