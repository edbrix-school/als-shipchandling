package com.alsharif.shipchandling.salesquotation.dto;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class SalesQuotationShipGrantEditRequestTest {

    @Test
    void testRecordCreation() {
        BigDecimal groupId = BigDecimal.valueOf(100);
        BigDecimal userPoid = BigDecimal.valueOf(500);
        String docId = "DOC001";
        BigDecimal docKeyPoid = BigDecimal.valueOf(300);

        SalesQuotationShipGrantEditRequest request = new SalesQuotationShipGrantEditRequest(
            groupId, userPoid, docId, docKeyPoid
        );

        assertEquals(groupId, request.groupId());
        assertEquals(userPoid, request.userPoid());
        assertEquals(docId, request.docId());
        assertEquals(docKeyPoid, request.docKeyPoid());
    }

    @Test
    void testRecordWithNullValues() {
        SalesQuotationShipGrantEditRequest request = new SalesQuotationShipGrantEditRequest(
            null, null, null, null
        );

        assertNull(request.groupId());
        assertNull(request.userPoid());
        assertNull(request.docId());
        assertNull(request.docKeyPoid());
    }

    @Test
    void testRecordEquality() {
        BigDecimal groupId = BigDecimal.valueOf(100);
        BigDecimal userPoid = BigDecimal.valueOf(500);
        String docId = "DOC001";
        BigDecimal docKeyPoid = BigDecimal.valueOf(300);

        SalesQuotationShipGrantEditRequest request1 = new SalesQuotationShipGrantEditRequest(
            groupId, userPoid, docId, docKeyPoid
        );
        SalesQuotationShipGrantEditRequest request2 = new SalesQuotationShipGrantEditRequest(
            groupId, userPoid, docId, docKeyPoid
        );

        assertEquals(request1, request2);
        assertEquals(request1.hashCode(), request2.hashCode());
    }

    @Test
    void testToString() {
        BigDecimal groupId = BigDecimal.valueOf(100);
        BigDecimal userPoid = BigDecimal.valueOf(500);
        String docId = "DOC001";
        BigDecimal docKeyPoid = BigDecimal.valueOf(300);

        SalesQuotationShipGrantEditRequest request = new SalesQuotationShipGrantEditRequest(
            groupId, userPoid, docId, docKeyPoid
        );

        String toString = request.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("SalesQuotationShipGrantEditRequest"));
    }
}

