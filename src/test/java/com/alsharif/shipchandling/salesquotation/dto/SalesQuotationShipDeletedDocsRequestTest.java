package com.alsharif.shipchandling.salesquotation.dto;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class SalesQuotationShipDeletedDocsRequestTest {

    @Test
    void testRecordCreation() {
        BigDecimal groupId = BigDecimal.valueOf(100);
        BigDecimal companyId = BigDecimal.valueOf(200);
        BigDecimal userPoid = BigDecimal.valueOf(500);
        String docId = "DOC001";
        String filterField1 = "status";
        String filterValue1 = "DELETED";
        String filterField2 = "date";
        String filterValue2 = "2024-01-01";

        SalesQuotationShipDeletedDocsRequest request = new SalesQuotationShipDeletedDocsRequest(
            groupId, companyId, userPoid, docId, filterField1, filterValue1, filterField2, filterValue2
        );

        assertEquals(groupId, request.groupId());
        assertEquals(companyId, request.companyId());
        assertEquals(userPoid, request.userPoid());
        assertEquals(docId, request.docId());
        assertEquals(filterField1, request.filterField1());
        assertEquals(filterValue1, request.filterValue1());
        assertEquals(filterField2, request.filterField2());
        assertEquals(filterValue2, request.filterValue2());
    }

    @Test
    void testRecordWithNullValues() {
        SalesQuotationShipDeletedDocsRequest request = new SalesQuotationShipDeletedDocsRequest(
            null, null, null, null, null, null, null, null
        );

        assertNull(request.groupId());
        assertNull(request.companyId());
        assertNull(request.userPoid());
        assertNull(request.docId());
        assertNull(request.filterField1());
        assertNull(request.filterValue1());
        assertNull(request.filterField2());
        assertNull(request.filterValue2());
    }

    @Test
    void testRecordEquality() {
        BigDecimal groupId = BigDecimal.valueOf(100);
        BigDecimal companyId = BigDecimal.valueOf(200);
        BigDecimal userPoid = BigDecimal.valueOf(500);
        String docId = "DOC001";
        String filterField1 = "status";
        String filterValue1 = "DELETED";
        String filterField2 = "date";
        String filterValue2 = "2024-01-01";

        SalesQuotationShipDeletedDocsRequest request1 = new SalesQuotationShipDeletedDocsRequest(
            groupId, companyId, userPoid, docId, filterField1, filterValue1, filterField2, filterValue2
        );
        SalesQuotationShipDeletedDocsRequest request2 = new SalesQuotationShipDeletedDocsRequest(
            groupId, companyId, userPoid, docId, filterField1, filterValue1, filterField2, filterValue2
        );

        assertEquals(request1, request2);
        assertEquals(request1.hashCode(), request2.hashCode());
    }

    @Test
    void testToString() {
        BigDecimal groupId = BigDecimal.valueOf(100);
        BigDecimal companyId = BigDecimal.valueOf(200);
        BigDecimal userPoid = BigDecimal.valueOf(500);
        String docId = "DOC001";
        String filterField1 = "status";
        String filterValue1 = "DELETED";
        String filterField2 = "date";
        String filterValue2 = "2024-01-01";

        SalesQuotationShipDeletedDocsRequest request = new SalesQuotationShipDeletedDocsRequest(
            groupId, companyId, userPoid, docId, filterField1, filterValue1, filterField2, filterValue2
        );

        String toString = request.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("SalesQuotationShipDeletedDocsRequest"));
    }
}

