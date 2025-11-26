package com.alsharif.shipchandling.salesquotation.dto;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class SalesQuotationShipCalculateRequestTest {

    @Test
    void testRecordCreation() {
        BigDecimal groupId = BigDecimal.valueOf(100);
        BigDecimal companyId = BigDecimal.valueOf(200);
        String loginUser = "testuser";
        BigDecimal transactionPoid = BigDecimal.valueOf(300);

        SalesQuotationShipCalculateRequest request = new SalesQuotationShipCalculateRequest(
            groupId, companyId, loginUser, transactionPoid
        );

        assertEquals(groupId, request.groupId());
        assertEquals(companyId, request.companyId());
        assertEquals(loginUser, request.loginUser());
        assertEquals(transactionPoid, request.transactionPoid());
    }

    @Test
    void testRecordWithNullValues() {
        SalesQuotationShipCalculateRequest request = new SalesQuotationShipCalculateRequest(
            null, null, null, null
        );

        assertNull(request.groupId());
        assertNull(request.companyId());
        assertNull(request.loginUser());
        assertNull(request.transactionPoid());
    }

    @Test
    void testRecordEquality() {
        BigDecimal groupId = BigDecimal.valueOf(100);
        BigDecimal companyId = BigDecimal.valueOf(200);
        String loginUser = "testuser";
        BigDecimal transactionPoid = BigDecimal.valueOf(300);

        SalesQuotationShipCalculateRequest request1 = new SalesQuotationShipCalculateRequest(
            groupId, companyId, loginUser, transactionPoid
        );
        SalesQuotationShipCalculateRequest request2 = new SalesQuotationShipCalculateRequest(
            groupId, companyId, loginUser, transactionPoid
        );

        assertEquals(request1, request2);
        assertEquals(request1.hashCode(), request2.hashCode());
    }

    @Test
    void testToString() {
        BigDecimal groupId = BigDecimal.valueOf(100);
        BigDecimal companyId = BigDecimal.valueOf(200);
        String loginUser = "testuser";
        BigDecimal transactionPoid = BigDecimal.valueOf(300);

        SalesQuotationShipCalculateRequest request = new SalesQuotationShipCalculateRequest(
            groupId, companyId, loginUser, transactionPoid
        );

        String toString = request.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("SalesQuotationShipCalculateRequest"));
    }
}

