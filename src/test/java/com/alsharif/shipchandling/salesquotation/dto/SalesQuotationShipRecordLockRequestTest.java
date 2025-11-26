package com.alsharif.shipchandling.salesquotation.dto;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class SalesQuotationShipRecordLockRequestTest {

    @Test
    void testRecordCreation() {
        String userId = "user123";
        String sessionDetail = "session123";
        String docId = "DOC001";
        String docName = "Document Name";
        BigDecimal docKeyPoid = BigDecimal.valueOf(300);
        String requestType = "LOCK";

        SalesQuotationShipRecordLockRequest request = new SalesQuotationShipRecordLockRequest(
            userId, sessionDetail, docId, docName, docKeyPoid, requestType
        );

        assertEquals(userId, request.userId());
        assertEquals(sessionDetail, request.sessionDetail());
        assertEquals(docId, request.docId());
        assertEquals(docName, request.docName());
        assertEquals(docKeyPoid, request.docKeyPoid());
        assertEquals(requestType, request.requestType());
    }

    @Test
    void testRecordWithNullValues() {
        SalesQuotationShipRecordLockRequest request = new SalesQuotationShipRecordLockRequest(
            null, null, null, null, null, null
        );

        assertNull(request.userId());
        assertNull(request.sessionDetail());
        assertNull(request.docId());
        assertNull(request.docName());
        assertNull(request.docKeyPoid());
        assertNull(request.requestType());
    }

    @Test
    void testRecordEquality() {
        String userId = "user123";
        String sessionDetail = "session123";
        String docId = "DOC001";
        String docName = "Document Name";
        BigDecimal docKeyPoid = BigDecimal.valueOf(300);
        String requestType = "LOCK";

        SalesQuotationShipRecordLockRequest request1 = new SalesQuotationShipRecordLockRequest(
            userId, sessionDetail, docId, docName, docKeyPoid, requestType
        );
        SalesQuotationShipRecordLockRequest request2 = new SalesQuotationShipRecordLockRequest(
            userId, sessionDetail, docId, docName, docKeyPoid, requestType
        );

        assertEquals(request1, request2);
        assertEquals(request1.hashCode(), request2.hashCode());
    }

    @Test
    void testToString() {
        String userId = "user123";
        String sessionDetail = "session123";
        String docId = "DOC001";
        String docName = "Document Name";
        BigDecimal docKeyPoid = BigDecimal.valueOf(300);
        String requestType = "UNLOCK";

        SalesQuotationShipRecordLockRequest request = new SalesQuotationShipRecordLockRequest(
            userId, sessionDetail, docId, docName, docKeyPoid, requestType
        );

        String toString = request.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("SalesQuotationShipRecordLockRequest"));
    }
}

