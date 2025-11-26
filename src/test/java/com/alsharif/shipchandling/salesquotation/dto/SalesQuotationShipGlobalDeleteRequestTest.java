package com.alsharif.shipchandling.salesquotation.dto;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class SalesQuotationShipGlobalDeleteRequestTest {

    @Test
    void testRecordCreation() {
        BigDecimal groupId = BigDecimal.valueOf(100);
        BigDecimal companyId = BigDecimal.valueOf(200);
        BigDecimal userPoid = BigDecimal.valueOf(500);
        String docId = "DOC001";
        String masterVoSqlName = "MasterVO";
        BigDecimal docKeyPoid = BigDecimal.valueOf(300);
        String tableName = "SALES_QUOTATION";
        String operationMode = "DELETE";
        LocalDate docDate = LocalDate.of(2024, 1, 15);
        String docType = "QUOTATION";

        SalesQuotationShipGlobalDeleteRequest request = new SalesQuotationShipGlobalDeleteRequest(
            groupId, companyId, userPoid, docId, masterVoSqlName, docKeyPoid,
            tableName, operationMode, docDate, docType
        );

        assertEquals(groupId, request.groupId());
        assertEquals(companyId, request.companyId());
        assertEquals(userPoid, request.userPoid());
        assertEquals(docId, request.docId());
        assertEquals(masterVoSqlName, request.masterVoSqlName());
        assertEquals(docKeyPoid, request.docKeyPoid());
        assertEquals(tableName, request.tableName());
        assertEquals(operationMode, request.operationMode());
        assertEquals(docDate, request.docDate());
        assertEquals(docType, request.docType());
    }

    @Test
    void testRecordWithNullValues() {
        SalesQuotationShipGlobalDeleteRequest request = new SalesQuotationShipGlobalDeleteRequest(
            null, null, null, null, null, null, null, null, null, null
        );

        assertNull(request.groupId());
        assertNull(request.companyId());
        assertNull(request.userPoid());
        assertNull(request.docId());
        assertNull(request.masterVoSqlName());
        assertNull(request.docKeyPoid());
        assertNull(request.tableName());
        assertNull(request.operationMode());
        assertNull(request.docDate());
        assertNull(request.docType());
    }

    @Test
    void testRecordEquality() {
        BigDecimal groupId = BigDecimal.valueOf(100);
        BigDecimal companyId = BigDecimal.valueOf(200);
        BigDecimal userPoid = BigDecimal.valueOf(500);
        String docId = "DOC001";
        String masterVoSqlName = "MasterVO";
        BigDecimal docKeyPoid = BigDecimal.valueOf(300);
        String tableName = "SALES_QUOTATION";
        String operationMode = "DELETE";
        LocalDate docDate = LocalDate.of(2024, 1, 15);
        String docType = "QUOTATION";

        SalesQuotationShipGlobalDeleteRequest request1 = new SalesQuotationShipGlobalDeleteRequest(
            groupId, companyId, userPoid, docId, masterVoSqlName, docKeyPoid,
            tableName, operationMode, docDate, docType
        );
        SalesQuotationShipGlobalDeleteRequest request2 = new SalesQuotationShipGlobalDeleteRequest(
            groupId, companyId, userPoid, docId, masterVoSqlName, docKeyPoid,
            tableName, operationMode, docDate, docType
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
        String masterVoSqlName = "MasterVO";
        BigDecimal docKeyPoid = BigDecimal.valueOf(300);
        String tableName = "SALES_QUOTATION";
        String operationMode = "DELETE";
        LocalDate docDate = LocalDate.of(2024, 1, 15);
        String docType = "QUOTATION";

        SalesQuotationShipGlobalDeleteRequest request = new SalesQuotationShipGlobalDeleteRequest(
            groupId, companyId, userPoid, docId, masterVoSqlName, docKeyPoid,
            tableName, operationMode, docDate, docType
        );

        String toString = request.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("SalesQuotationShipGlobalDeleteRequest"));
    }
}

