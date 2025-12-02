package com.asg.shipchandling.salesquotation.dto;

import java.math.BigDecimal;

public record SalesQuotationShipValidationRequest(
        BigDecimal customerAddressId,
        BigDecimal transactionPoid
) {
}

