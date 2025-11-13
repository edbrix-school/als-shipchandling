package com.alsharif.shipchandling.salesquotation.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SalesQuotationShipApprovalActionRequest(
        BigDecimal groupId,
        BigDecimal companyId,
        BigDecimal userPoid,
        String docId,
        BigDecimal docKeyPoid,
        String action,
        String comments,
        String docInfo,
        String docRef,
        LocalDate docDate,
        BigDecimal targetUserPoid
) {
}

