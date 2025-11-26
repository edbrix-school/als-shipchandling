package com.alsharif.shipchandling.salesquotation.dto;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class SalesQuotationShipReleaseLockRequestTest {

    @Test
    void testRecordCreation() {
        BigDecimal groupId = BigDecimal.valueOf(100);
        BigDecimal companyId = BigDecimal.valueOf(200);
        BigDecimal userPoid = BigDecimal.valueOf(500);
        String docId = "DOC001";
        BigDecimal docKeyPoid = BigDecimal.valueOf(300);
        String requestMetadata = "metadata";

        SalesQuotationShipReleaseLockRequest request = new SalesQuotationShipReleaseLockRequest(
            groupId, companyId, userPoid, docId, docKeyPoid, requestMetadata
        );

        assertEquals(groupId, request.groupId());
        assertEquals(companyId, request.companyId());
        assertEquals(userPoid, request.userPoid());
        assertEquals(docId, request.docId());
        assertEquals(docKeyPoid, request.docKeyPoid());
        assertEquals(requestMetadata, request.requestMetadata());
    }

    @Test
    void testRecordWithNullValues() {
        SalesQuotationShipReleaseLockRequest request = new SalesQuotationShipReleaseLockRequest(
            null, null, null, null, null, null
        );

        assertNull(request.groupId());
        assertNull(request.companyId());
        assertNull(request.userPoid());
        assertNull(request.docId());
        assertNull(request.docKeyPoid());
        assertNull(request.requestMetadata());
    }

    @Test
    void testRecordEquality() {
        BigDecimal groupId = BigDecimal.valueOf(100);
        BigDecimal companyId = BigDecimal.valueOf(200);
        BigDecimal userPoid = BigDecimal.valueOf(500);
        String docId = "DOC001";
        BigDecimal docKeyPoid = BigDecimal.valueOf(300);
        String requestMetadata = "metadata";

        SalesQuotationShipReleaseLockRequest request1 = new SalesQuotationShipReleaseLockRequest(
            groupId, companyId, userPoid, docId, docKeyPoid, requestMetadata
        );
        SalesQuotationShipReleaseLockRequest request2 = new SalesQuotationShipReleaseLockRequest(
            groupId, companyId, userPoid, docId, docKeyPoid, requestMetadata
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
        String requestMetadata = "metadata";

        SalesQuotationShipReleaseLockRequest request = new SalesQuotationShipReleaseLockRequest(
            groupId, companyId, userPoid, docId, docKeyPoid, requestMetadata
        );

        String toString = request.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("SalesQuotationShipReleaseLockRequest"));
    }
}

