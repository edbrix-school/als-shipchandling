package com.alsharif.shipchandling.salesquotation.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SalesQuotationShipDefaultDetailRequest(
        BigDecimal customerAddressId,
        BigDecimal stockPoid,
        BigDecimal transactionPoid,
        BigDecimal stockUnitPoid,
        LocalDate documentDate
) {
}

