package com.asg.shipchandling.salesquotation.dto;

import java.math.BigDecimal;

public record SalesQuotationShipClearItemsRequest(
        BigDecimal groupId,
        BigDecimal companyId,
        BigDecimal transactionPoid
) {
}

