package com.alsharif.shipchandling.salesquotation.dto;

import java.math.BigDecimal;

public record SalesQuotationShipUpdateSequenceRequest(
        BigDecimal groupId,
        BigDecimal companyId,
        BigDecimal userPoid,
        String tableName,
        BigDecimal currentSeqNo,
        String masterVoSqlName,
        BigDecimal docKeyPoid
) {
}

