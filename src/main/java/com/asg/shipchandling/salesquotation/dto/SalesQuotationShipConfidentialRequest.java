package com.asg.shipchandling.salesquotation.dto;

import java.math.BigDecimal;

public record SalesQuotationShipConfidentialRequest(
        BigDecimal groupId,
        BigDecimal userPoid,
        String docId,
        BigDecimal docKeyPoid,
        String rightCode,
        String actionFlag
) {
}

