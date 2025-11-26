package com.alsharif.shipchandling.salesquotation.dto;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class SalesQuotationShipSelectAllRequestTest {

    @Test
    void testRecordCreation() {
        BigDecimal groupId = BigDecimal.valueOf(100);
        BigDecimal companyId = BigDecimal.valueOf(200);
        BigDecimal transactionPoid = BigDecimal.valueOf(300);
        String selectionFlag = "Y";

        SalesQuotationShipSelectAllRequest request = new SalesQuotationShipSelectAllRequest(
            groupId, companyId, transactionPoid, selectionFlag
        );

        assertEquals(groupId, request.groupId());
        assertEquals(companyId, request.companyId());
        assertEquals(transactionPoid, request.transactionPoid());
        assertEquals(selectionFlag, request.selectionFlag());
    }

    @Test
    void testRecordWithNullValues() {
        SalesQuotationShipSelectAllRequest request = new SalesQuotationShipSelectAllRequest(
            null, null, null, null
        );

        assertNull(request.groupId());
        assertNull(request.companyId());
        assertNull(request.transactionPoid());
        assertNull(request.selectionFlag());
    }

    @Test
    void testRecordEquality() {
        BigDecimal groupId = BigDecimal.valueOf(100);
        BigDecimal companyId = BigDecimal.valueOf(200);
        BigDecimal transactionPoid = BigDecimal.valueOf(300);
        String selectionFlag = "Y";

        SalesQuotationShipSelectAllRequest request1 = new SalesQuotationShipSelectAllRequest(
            groupId, companyId, transactionPoid, selectionFlag
        );
        SalesQuotationShipSelectAllRequest request2 = new SalesQuotationShipSelectAllRequest(
            groupId, companyId, transactionPoid, selectionFlag
        );

        assertEquals(request1, request2);
        assertEquals(request1.hashCode(), request2.hashCode());
    }

    @Test
    void testToString() {
        BigDecimal groupId = BigDecimal.valueOf(100);
        BigDecimal companyId = BigDecimal.valueOf(200);
        BigDecimal transactionPoid = BigDecimal.valueOf(300);
        String selectionFlag = "N";

        SalesQuotationShipSelectAllRequest request = new SalesQuotationShipSelectAllRequest(
            groupId, companyId, transactionPoid, selectionFlag
        );

        String toString = request.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("SalesQuotationShipSelectAllRequest"));
    }
}

