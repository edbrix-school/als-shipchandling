package com.alsharif.shipchandling.salesquotation.dto;

import java.math.BigDecimal;

public record SalesQuotationShipDeletedDocsRequest(
        BigDecimal groupId,
        BigDecimal companyId,
        BigDecimal userPoid,
        String docId,
        String filterField1,
        String filterValue1,
        String filterField2,
        String filterValue2
) {
}

