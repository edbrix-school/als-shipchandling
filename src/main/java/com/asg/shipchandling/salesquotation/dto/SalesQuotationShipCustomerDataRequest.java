package com.asg.shipchandling.salesquotation.dto;

import java.math.BigDecimal;

public record SalesQuotationShipCustomerDataRequest(
        BigDecimal groupId,
        BigDecimal companyId,
        BigDecimal customerAddressId,
        BigDecimal transactionPoid
) {
}

