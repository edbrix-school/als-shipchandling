package com.alsharif.shipchandling.salesquotation.dto;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class SalesQuotationShipImportRequestTest {

    @Test
    void testRecordCreation() {
        BigDecimal groupId = BigDecimal.valueOf(100);
        BigDecimal companyId = BigDecimal.valueOf(200);
        BigDecimal transactionPoid = BigDecimal.valueOf(300);
        String loginUser = "testuser";

        SalesQuotationShipImportRequest request = new SalesQuotationShipImportRequest(
            groupId, companyId, transactionPoid, loginUser
        );

        assertEquals(groupId, request.groupId());
        assertEquals(companyId, request.companyId());
        assertEquals(transactionPoid, request.transactionPoid());
        assertEquals(loginUser, request.loginUser());
    }

    @Test
    void testRecordWithNullValues() {
        SalesQuotationShipImportRequest request = new SalesQuotationShipImportRequest(
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

        SalesQuotationShipImportRequest request1 = new SalesQuotationShipImportRequest(
            groupId, companyId, transactionPoid, loginUser
        );
        SalesQuotationShipImportRequest request2 = new SalesQuotationShipImportRequest(
            groupId, companyId, transactionPoid, loginUser
        );

        assertEquals(request1, request2);
        assertEquals(request1.hashCode(), request2.hashCode());
    }

    @Test
    void testRecordInequality() {
        BigDecimal groupId = BigDecimal.valueOf(100);
        BigDecimal companyId = BigDecimal.valueOf(200);
        BigDecimal transactionPoid = BigDecimal.valueOf(300);
        String loginUser = "testuser";

        SalesQuotationShipImportRequest request1 = new SalesQuotationShipImportRequest(
            groupId, companyId, transactionPoid, loginUser
        );
        SalesQuotationShipImportRequest request2 = new SalesQuotationShipImportRequest(
            BigDecimal.valueOf(999), companyId, transactionPoid, loginUser
        );

        assertNotEquals(request1, request2);
    }

    @Test
    void testToString() {
        BigDecimal groupId = BigDecimal.valueOf(100);
        BigDecimal companyId = BigDecimal.valueOf(200);
        BigDecimal transactionPoid = BigDecimal.valueOf(300);
        String loginUser = "testuser";

        SalesQuotationShipImportRequest request = new SalesQuotationShipImportRequest(
            groupId, companyId, transactionPoid, loginUser
        );

        String toString = request.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("SalesQuotationShipImportRequest"));
    }
}

