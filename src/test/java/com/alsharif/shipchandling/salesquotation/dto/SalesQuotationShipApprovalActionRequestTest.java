package com.alsharif.shipchandling.salesquotation.dto;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class SalesQuotationShipApprovalActionRequestTest {

    @Test
    void testRecordCreation() {
        BigDecimal groupId = BigDecimal.valueOf(100);
        BigDecimal companyId = BigDecimal.valueOf(200);
        BigDecimal userPoid = BigDecimal.valueOf(500);
        String docId = "DOC001";
        BigDecimal docKeyPoid = BigDecimal.valueOf(300);
        String action = "APPROVE";
        String comments = "Approved by manager";
        String docInfo = "Document info";
        String docRef = "REF001";
        LocalDate docDate = LocalDate.of(2024, 1, 15);
        BigDecimal targetUserPoid = BigDecimal.valueOf(600);

        SalesQuotationShipApprovalActionRequest request = new SalesQuotationShipApprovalActionRequest(
            groupId, companyId, userPoid, docId, docKeyPoid, action, comments,
            docInfo, docRef, docDate, targetUserPoid
        );

        assertEquals(groupId, request.groupId());
        assertEquals(companyId, request.companyId());
        assertEquals(userPoid, request.userPoid());
        assertEquals(docId, request.docId());
        assertEquals(docKeyPoid, request.docKeyPoid());
        assertEquals(action, request.action());
        assertEquals(comments, request.comments());
        assertEquals(docInfo, request.docInfo());
        assertEquals(docRef, request.docRef());
        assertEquals(docDate, request.docDate());
        assertEquals(targetUserPoid, request.targetUserPoid());
    }

    @Test
    void testRecordWithNullValues() {
        SalesQuotationShipApprovalActionRequest request = new SalesQuotationShipApprovalActionRequest(
            null, null, null, null, null, null, null, null, null, null, null
        );

        assertNull(request.groupId());
        assertNull(request.companyId());
        assertNull(request.userPoid());
        assertNull(request.docId());
        assertNull(request.docKeyPoid());
        assertNull(request.action());
        assertNull(request.comments());
        assertNull(request.docInfo());
        assertNull(request.docRef());
        assertNull(request.docDate());
        assertNull(request.targetUserPoid());
    }

    @Test
    void testRecordEquality() {
        BigDecimal groupId = BigDecimal.valueOf(100);
        BigDecimal companyId = BigDecimal.valueOf(200);
        BigDecimal userPoid = BigDecimal.valueOf(500);
        String docId = "DOC001";
        BigDecimal docKeyPoid = BigDecimal.valueOf(300);
        String action = "APPROVE";
        String comments = "Approved";
        String docInfo = "Info";
        String docRef = "REF001";
        LocalDate docDate = LocalDate.of(2024, 1, 15);
        BigDecimal targetUserPoid = BigDecimal.valueOf(600);

        SalesQuotationShipApprovalActionRequest request1 = new SalesQuotationShipApprovalActionRequest(
            groupId, companyId, userPoid, docId, docKeyPoid, action, comments,
            docInfo, docRef, docDate, targetUserPoid
        );
        SalesQuotationShipApprovalActionRequest request2 = new SalesQuotationShipApprovalActionRequest(
            groupId, companyId, userPoid, docId, docKeyPoid, action, comments,
            docInfo, docRef, docDate, targetUserPoid
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
        String action = "APPROVE";
        String comments = "Approved";
        String docInfo = "Info";
        String docRef = "REF001";
        LocalDate docDate = LocalDate.of(2024, 1, 15);
        BigDecimal targetUserPoid = BigDecimal.valueOf(600);

        SalesQuotationShipApprovalActionRequest request = new SalesQuotationShipApprovalActionRequest(
            groupId, companyId, userPoid, docId, docKeyPoid, action, comments,
            docInfo, docRef, docDate, targetUserPoid
        );

        String toString = request.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("SalesQuotationShipApprovalActionRequest"));
    }
}

