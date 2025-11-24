package com.alsharif.shipchandling.salesquotation.dto;

import java.math.BigDecimal;

public record SalesQuotationShipResetSequenceRequest(
        BigDecimal groupId,
        BigDecimal companyId,
        BigDecimal userPoid,
        String tableName,
        String masterVoSqlName
) {
}

