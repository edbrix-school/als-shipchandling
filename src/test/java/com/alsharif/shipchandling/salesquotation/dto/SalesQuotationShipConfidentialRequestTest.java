package com.alsharif.shipchandling.salesquotation.dto;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class SalesQuotationShipConfidentialRequestTest {

    @Test
    void testRecordCreation() {
        BigDecimal groupId = BigDecimal.valueOf(100);
        BigDecimal userPoid = BigDecimal.valueOf(500);
        String docId = "DOC001";
        BigDecimal docKeyPoid = BigDecimal.valueOf(300);
        String rightCode = "VIEW";
        String actionFlag = "GRANT";

        SalesQuotationShipConfidentialRequest request = new SalesQuotationShipConfidentialRequest(
            groupId, userPoid, docId, docKeyPoid, rightCode, actionFlag
        );

        assertEquals(groupId, request.groupId());
        assertEquals(userPoid, request.userPoid());
        assertEquals(docId, request.docId());
        assertEquals(docKeyPoid, request.docKeyPoid());
        assertEquals(rightCode, request.rightCode());
        assertEquals(actionFlag, request.actionFlag());
    }

    @Test
    void testRecordWithNullValues() {
        SalesQuotationShipConfidentialRequest request = new SalesQuotationShipConfidentialRequest(
            null, null, null, null, null, null
        );

        assertNull(request.groupId());
        assertNull(request.userPoid());
        assertNull(request.docId());
        assertNull(request.docKeyPoid());
        assertNull(request.rightCode());
        assertNull(request.actionFlag());
    }

    @Test
    void testRecordEquality() {
        BigDecimal groupId = BigDecimal.valueOf(100);
        BigDecimal userPoid = BigDecimal.valueOf(500);
        String docId = "DOC001";
        BigDecimal docKeyPoid = BigDecimal.valueOf(300);
        String rightCode = "VIEW";
        String actionFlag = "GRANT";

        SalesQuotationShipConfidentialRequest request1 = new SalesQuotationShipConfidentialRequest(
            groupId, userPoid, docId, docKeyPoid, rightCode, actionFlag
        );
        SalesQuotationShipConfidentialRequest request2 = new SalesQuotationShipConfidentialRequest(
            groupId, userPoid, docId, docKeyPoid, rightCode, actionFlag
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
        String rightCode = "VIEW";
        String actionFlag = "GRANT";

        SalesQuotationShipConfidentialRequest request = new SalesQuotationShipConfidentialRequest(
            groupId, userPoid, docId, docKeyPoid, rightCode, actionFlag
        );

        String toString = request.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("SalesQuotationShipConfidentialRequest"));
    }
}

