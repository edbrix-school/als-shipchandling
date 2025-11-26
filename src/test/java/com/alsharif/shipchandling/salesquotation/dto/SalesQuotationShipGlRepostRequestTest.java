package com.alsharif.shipchandling.salesquotation.dto;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class SalesQuotationShipGlRepostRequestTest {

    @Test
    void testRecordCreation() {
        BigDecimal groupId = BigDecimal.valueOf(100);
        BigDecimal companyId = BigDecimal.valueOf(200);
        BigDecimal userPoid = BigDecimal.valueOf(500);
        String docId = "DOC001";
        BigDecimal docKeyPoid = BigDecimal.valueOf(300);
        String docRef = "REF001";

        SalesQuotationShipGlRepostRequest request = new SalesQuotationShipGlRepostRequest(
            groupId, companyId, userPoid, docId, docKeyPoid, docRef
        );

        assertEquals(groupId, request.groupId());
        assertEquals(companyId, request.companyId());
        assertEquals(userPoid, request.userPoid());
        assertEquals(docId, request.docId());
        assertEquals(docKeyPoid, request.docKeyPoid());
        assertEquals(docRef, request.docRef());
    }

    @Test
    void testRecordWithNullValues() {
        SalesQuotationShipGlRepostRequest request = new SalesQuotationShipGlRepostRequest(
            null, null, null, null, null, null
        );

        assertNull(request.groupId());
        assertNull(request.companyId());
        assertNull(request.userPoid());
        assertNull(request.docId());
        assertNull(request.docKeyPoid());
        assertNull(request.docRef());
    }

    @Test
    void testRecordEquality() {
        BigDecimal groupId = BigDecimal.valueOf(100);
        BigDecimal companyId = BigDecimal.valueOf(200);
        BigDecimal userPoid = BigDecimal.valueOf(500);
        String docId = "DOC001";
        BigDecimal docKeyPoid = BigDecimal.valueOf(300);
        String docRef = "REF001";

        SalesQuotationShipGlRepostRequest request1 = new SalesQuotationShipGlRepostRequest(
            groupId, companyId, userPoid, docId, docKeyPoid, docRef
        );
        SalesQuotationShipGlRepostRequest request2 = new SalesQuotationShipGlRepostRequest(
            groupId, companyId, userPoid, docId, docKeyPoid, docRef
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
        BigDecimal docKeyPoid = BigDecimal.valueOf(300);
        String docRef = "REF001";

        SalesQuotationShipGlRepostRequest request = new SalesQuotationShipGlRepostRequest(
            groupId, companyId, userPoid, docId, docKeyPoid, docRef
        );

        String toString = request.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("SalesQuotationShipGlRepostRequest"));
    }
}

