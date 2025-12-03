package com.asg.shipchandling.salesquotation.dto;

import java.math.BigDecimal;

public record SalesQuotationShipGrantEditRequest(
        BigDecimal groupId,
        BigDecimal userPoid,
        String docId,
        BigDecimal docKeyPoid
) {
}

