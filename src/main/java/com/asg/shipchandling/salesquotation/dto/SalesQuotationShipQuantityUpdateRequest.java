package com.asg.shipchandling.salesquotation.dto;

import java.math.BigDecimal;

public record SalesQuotationShipQuantityUpdateRequest(
        BigDecimal groupId,
        BigDecimal companyId,
        BigDecimal userPoid,
        String loginUser,
        BigDecimal transactionPoid
) {
}

