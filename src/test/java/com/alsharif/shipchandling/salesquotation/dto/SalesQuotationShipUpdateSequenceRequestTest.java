package com.alsharif.shipchandling.salesquotation.dto;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class SalesQuotationShipUpdateSequenceRequestTest {

    @Test
    void testRecordCreation() {
        BigDecimal groupId = BigDecimal.valueOf(100);
        BigDecimal companyId = BigDecimal.valueOf(200);
        BigDecimal userPoid = BigDecimal.valueOf(500);
        String tableName = "SALES_QUOTATION";
        BigDecimal currentSeqNo = BigDecimal.valueOf(10);
        String masterVoSqlName = "MasterVO";
        BigDecimal docKeyPoid = BigDecimal.valueOf(300);

        SalesQuotationShipUpdateSequenceRequest request = new SalesQuotationShipUpdateSequenceRequest(
            groupId, companyId, userPoid, tableName, currentSeqNo, masterVoSqlName, docKeyPoid
        );

        assertEquals(groupId, request.groupId());
        assertEquals(companyId, request.companyId());
        assertEquals(userPoid, request.userPoid());
        assertEquals(tableName, request.tableName());
        assertEquals(currentSeqNo, request.currentSeqNo());
        assertEquals(masterVoSqlName, request.masterVoSqlName());
        assertEquals(docKeyPoid, request.docKeyPoid());
    }

    @Test
    void testRecordWithNullValues() {
        SalesQuotationShipUpdateSequenceRequest request = new SalesQuotationShipUpdateSequenceRequest(
            null, null, null, null, null, null, null
        );

        assertNull(request.groupId());
        assertNull(request.companyId());
        assertNull(request.userPoid());
        assertNull(request.tableName());
        assertNull(request.currentSeqNo());
        assertNull(request.masterVoSqlName());
        assertNull(request.docKeyPoid());
    }

    @Test
    void testRecordEquality() {
        BigDecimal groupId = BigDecimal.valueOf(100);
        BigDecimal companyId = BigDecimal.valueOf(200);
        BigDecimal userPoid = BigDecimal.valueOf(500);
        String tableName = "SALES_QUOTATION";
        BigDecimal currentSeqNo = BigDecimal.valueOf(10);
        String masterVoSqlName = "MasterVO";
        BigDecimal docKeyPoid = BigDecimal.valueOf(300);

        SalesQuotationShipUpdateSequenceRequest request1 = new SalesQuotationShipUpdateSequenceRequest(
            groupId, companyId, userPoid, tableName, currentSeqNo, masterVoSqlName, docKeyPoid
        );
        SalesQuotationShipUpdateSequenceRequest request2 = new SalesQuotationShipUpdateSequenceRequest(
            groupId, companyId, userPoid, tableName, currentSeqNo, masterVoSqlName, docKeyPoid
        );

        assertEquals(request1, request2);
        assertEquals(request1.hashCode(), request2.hashCode());
    }

    @Test
    void testToString() {
        BigDecimal groupId = BigDecimal.valueOf(100);
        BigDecimal companyId = BigDecimal.valueOf(200);
        BigDecimal userPoid = BigDecimal.valueOf(500);
        String tableName = "SALES_QUOTATION";
        BigDecimal currentSeqNo = BigDecimal.valueOf(10);
        String masterVoSqlName = "MasterVO";
        BigDecimal docKeyPoid = BigDecimal.valueOf(300);

        SalesQuotationShipUpdateSequenceRequest request = new SalesQuotationShipUpdateSequenceRequest(
            groupId, companyId, userPoid, tableName, currentSeqNo, masterVoSqlName, docKeyPoid
        );

        String toString = request.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("SalesQuotationShipUpdateSequenceRequest"));
    }
}

