package com.alsharif.shipchandling.salesquotation.dto;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class SalesQuotationShipQuantityUpdateRequestTest {

    @Test
    void testRecordCreation() {
        BigDecimal groupId = BigDecimal.valueOf(100);
        BigDecimal companyId = BigDecimal.valueOf(200);
        BigDecimal userPoid = BigDecimal.valueOf(500);
        String loginUser = "testuser";
        BigDecimal transactionPoid = BigDecimal.valueOf(300);

        SalesQuotationShipQuantityUpdateRequest request = new SalesQuotationShipQuantityUpdateRequest(
            groupId, companyId, userPoid, loginUser, transactionPoid
        );

        assertEquals(groupId, request.groupId());
        assertEquals(companyId, request.companyId());
        assertEquals(userPoid, request.userPoid());
        assertEquals(loginUser, request.loginUser());
        assertEquals(transactionPoid, request.transactionPoid());
    }

    @Test
    void testRecordWithNullValues() {
        SalesQuotationShipQuantityUpdateRequest request = new SalesQuotationShipQuantityUpdateRequest(
            null, null, null, null, null
        );

        assertNull(request.groupId());
        assertNull(request.companyId());
        assertNull(request.userPoid());
        assertNull(request.loginUser());
        assertNull(request.transactionPoid());
    }

    @Test
    void testRecordEquality() {
        BigDecimal groupId = BigDecimal.valueOf(100);
        BigDecimal companyId = BigDecimal.valueOf(200);
        BigDecimal userPoid = BigDecimal.valueOf(500);
        String loginUser = "testuser";
        BigDecimal transactionPoid = BigDecimal.valueOf(300);

        SalesQuotationShipQuantityUpdateRequest request1 = new SalesQuotationShipQuantityUpdateRequest(
            groupId, companyId, userPoid, loginUser, transactionPoid
        );
        SalesQuotationShipQuantityUpdateRequest request2 = new SalesQuotationShipQuantityUpdateRequest(
            groupId, companyId, userPoid, loginUser, transactionPoid
        );

        assertEquals(request1, request2);
        assertEquals(request1.hashCode(), request2.hashCode());
    }

    @Test
    void testToString() {
        BigDecimal groupId = BigDecimal.valueOf(100);
        BigDecimal companyId = BigDecimal.valueOf(200);
        BigDecimal userPoid = BigDecimal.valueOf(500);
        String loginUser = "testuser";
        BigDecimal transactionPoid = BigDecimal.valueOf(300);

        SalesQuotationShipQuantityUpdateRequest request = new SalesQuotationShipQuantityUpdateRequest(
            groupId, companyId, userPoid, loginUser, transactionPoid
        );

        String toString = request.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("SalesQuotationShipQuantityUpdateRequest"));
    }
}

