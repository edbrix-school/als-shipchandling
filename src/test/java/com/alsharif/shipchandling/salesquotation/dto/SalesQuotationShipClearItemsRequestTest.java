package com.alsharif.shipchandling.salesquotation.dto;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class SalesQuotationShipClearItemsRequestTest {

    @Test
    void testRecordCreation() {
        BigDecimal groupId = BigDecimal.valueOf(100);
        BigDecimal companyId = BigDecimal.valueOf(200);
        BigDecimal transactionPoid = BigDecimal.valueOf(300);

        SalesQuotationShipClearItemsRequest request = new SalesQuotationShipClearItemsRequest(
            groupId, companyId, transactionPoid
        );

        assertEquals(groupId, request.groupId());
        assertEquals(companyId, request.companyId());
        assertEquals(transactionPoid, request.transactionPoid());
    }

    @Test
    void testRecordWithNullValues() {
        SalesQuotationShipClearItemsRequest request = new SalesQuotationShipClearItemsRequest(
            null, null, null
        );

        assertNull(request.groupId());
        assertNull(request.companyId());
        assertNull(request.transactionPoid());
    }

    @Test
    void testRecordEquality() {
        BigDecimal groupId = BigDecimal.valueOf(100);
        BigDecimal companyId = BigDecimal.valueOf(200);
        BigDecimal transactionPoid = BigDecimal.valueOf(300);

        SalesQuotationShipClearItemsRequest request1 = new SalesQuotationShipClearItemsRequest(
            groupId, companyId, transactionPoid
        );
        SalesQuotationShipClearItemsRequest request2 = new SalesQuotationShipClearItemsRequest(
            groupId, companyId, transactionPoid
        );

        assertEquals(request1, request2);
        assertEquals(request1.hashCode(), request2.hashCode());
    }

    @Test
    void testToString() {
        BigDecimal groupId = BigDecimal.valueOf(100);
        BigDecimal companyId = BigDecimal.valueOf(200);
        BigDecimal transactionPoid = BigDecimal.valueOf(300);

        SalesQuotationShipClearItemsRequest request = new SalesQuotationShipClearItemsRequest(
            groupId, companyId, transactionPoid
        );

        String toString = request.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("SalesQuotationShipClearItemsRequest"));
    }
}

