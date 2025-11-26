package com.alsharif.shipchandling.salesquotation.dto;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class SalesQuotationShipRfQRequestTest {

    @Test
    void testRecordCreation() {
        BigDecimal groupId = BigDecimal.valueOf(100);
        BigDecimal companyId = BigDecimal.valueOf(200);
        BigDecimal transactionPoid = BigDecimal.valueOf(300);
        String loginUser = "testuser";

        SalesQuotationShipRfQRequest request = new SalesQuotationShipRfQRequest(
            groupId, companyId, transactionPoid, loginUser
        );

        assertEquals(groupId, request.groupId());
        assertEquals(companyId, request.companyId());
        assertEquals(transactionPoid, request.transactionPoid());
        assertEquals(loginUser, request.loginUser());
    }

    @Test
    void testRecordWithNullValues() {
        SalesQuotationShipRfQRequest request = new SalesQuotationShipRfQRequest(
            null, null, null, null
        );

        assertNull(request.groupId());
        assertNull(request.companyId());
        assertNull(request.transactionPoid());
        assertNull(request.loginUser());
    }

    @Test
    void testRecordEquality() {
        BigDecimal groupId = BigDecimal.valueOf(100);
        BigDecimal companyId = BigDecimal.valueOf(200);
        BigDecimal transactionPoid = BigDecimal.valueOf(300);
        String loginUser = "testuser";

        SalesQuotationShipRfQRequest request1 = new SalesQuotationShipRfQRequest(
            groupId, companyId, transactionPoid, loginUser
        );
        SalesQuotationShipRfQRequest request2 = new SalesQuotationShipRfQRequest(
            groupId, companyId, transactionPoid, loginUser
        );

        assertEquals(request1, request2);
        assertEquals(request1.hashCode(), request2.hashCode());
    }

    @Test
    void testToString() {
        BigDecimal groupId = BigDecimal.valueOf(100);
        BigDecimal companyId = BigDecimal.valueOf(200);
        BigDecimal transactionPoid = BigDecimal.valueOf(300);
        String loginUser = "testuser";

        SalesQuotationShipRfQRequest request = new SalesQuotationShipRfQRequest(
            groupId, companyId, transactionPoid, loginUser
        );

        String toString = request.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("SalesQuotationShipRfQRequest"));
    }
}

