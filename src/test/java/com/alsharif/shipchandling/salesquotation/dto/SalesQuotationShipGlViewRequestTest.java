package com.alsharif.shipchandling.salesquotation.dto;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class SalesQuotationShipGlViewRequestTest {

    @Test
    void testRecordCreation() {
        BigDecimal groupId = BigDecimal.valueOf(100);
        BigDecimal companyId = BigDecimal.valueOf(200);
        String docId = "DOC001";
        BigDecimal docKeyPoid = BigDecimal.valueOf(300);

        SalesQuotationShipGlViewRequest request = new SalesQuotationShipGlViewRequest(
            groupId, companyId, docId, docKeyPoid
        );

        assertEquals(groupId, request.groupId());
        assertEquals(companyId, request.companyId());
        assertEquals(docId, request.docId());
        assertEquals(docKeyPoid, request.docKeyPoid());
    }

    @Test
    void testRecordWithNullValues() {
        SalesQuotationShipGlViewRequest request = new SalesQuotationShipGlViewRequest(
            null, null, null, null
        );

        assertNull(request.groupId());
        assertNull(request.companyId());
        assertNull(request.docId());
        assertNull(request.docKeyPoid());
    }

    @Test
    void testRecordEquality() {
        BigDecimal groupId = BigDecimal.valueOf(100);
        BigDecimal companyId = BigDecimal.valueOf(200);
        String docId = "DOC001";
        BigDecimal docKeyPoid = BigDecimal.valueOf(300);

        SalesQuotationShipGlViewRequest request1 = new SalesQuotationShipGlViewRequest(
            groupId, companyId, docId, docKeyPoid
        );
        SalesQuotationShipGlViewRequest request2 = new SalesQuotationShipGlViewRequest(
            groupId, companyId, docId, docKeyPoid
        );

        assertEquals(request1, request2);
        assertEquals(request1.hashCode(), request2.hashCode());
    }

    @Test
    void testToString() {
        BigDecimal groupId = BigDecimal.valueOf(100);
        BigDecimal companyId = BigDecimal.valueOf(200);
        String docId = "DOC001";
        BigDecimal docKeyPoid = BigDecimal.valueOf(300);

        SalesQuotationShipGlViewRequest request = new SalesQuotationShipGlViewRequest(
            groupId, companyId, docId, docKeyPoid
        );

        String toString = request.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("SalesQuotationShipGlViewRequest"));
    }
}

