package com.asg.shipchandling.salesquotation.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SalesQuotationShipGlobalDeleteRequest(
        BigDecimal groupId,
        BigDecimal companyId,
        BigDecimal userPoid,
        String docId,
        String masterVoSqlName,
        BigDecimal docKeyPoid,
        String tableName,
        String operationMode,
        LocalDate docDate,
        String docType
) {
}

