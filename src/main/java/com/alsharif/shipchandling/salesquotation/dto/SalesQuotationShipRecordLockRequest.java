package com.alsharif.shipchandling.salesquotation.dto;

import java.math.BigDecimal;

public record SalesQuotationShipRecordLockRequest(
        String userId,
        String sessionDetail,
        String docId,
        String docName,
        BigDecimal docKeyPoid,
        String requestType
) {
}

