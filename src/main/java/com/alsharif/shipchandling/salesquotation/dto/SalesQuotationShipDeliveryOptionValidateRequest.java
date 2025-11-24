package com.alsharif.shipchandling.salesquotation.dto;

import java.math.BigDecimal;

public record SalesQuotationShipDeliveryOptionValidateRequest(
        BigDecimal transactionPoid,
        BigDecimal detailRowId,
        BigDecimal stockPoid,
        String selectionFlag
) {
}

