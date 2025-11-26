package com.alsharif.shipchandling.salesquotation.dto;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class SalesQuotationShipResetSequenceRequestTest {

    @Test
    void testRecordCreation() {
        BigDecimal groupId = BigDecimal.valueOf(100);
        BigDecimal companyId = BigDecimal.valueOf(200);
        BigDecimal userPoid = BigDecimal.valueOf(500);
        String tableName = "SALES_QUOTATION";
        String masterVoSqlName = "MasterVO";

        SalesQuotationShipResetSequenceRequest request = new SalesQuotationShipResetSequenceRequest(
            groupId, companyId, userPoid, tableName, masterVoSqlName
        );

        assertEquals(groupId, request.groupId());
        assertEquals(companyId, request.companyId());
        assertEquals(userPoid, request.userPoid());
        assertEquals(tableName, request.tableName());
        assertEquals(masterVoSqlName, request.masterVoSqlName());
    }

    @Test
    void testRecordWithNullValues() {
        SalesQuotationShipResetSequenceRequest request = new SalesQuotationShipResetSequenceRequest(
            null, null, null, null, null
        );

        assertNull(request.groupId());
        assertNull(request.companyId());
        assertNull(request.userPoid());
        assertNull(request.tableName());
        assertNull(request.masterVoSqlName());
    }

    @Test
    void testRecordEquality() {
        BigDecimal groupId = BigDecimal.valueOf(100);
        BigDecimal companyId = BigDecimal.valueOf(200);
        BigDecimal userPoid = BigDecimal.valueOf(500);
        String tableName = "SALES_QUOTATION";
        String masterVoSqlName = "MasterVO";

        SalesQuotationShipResetSequenceRequest request1 = new SalesQuotationShipResetSequenceRequest(
            groupId, companyId, userPoid, tableName, masterVoSqlName
        );
        SalesQuotationShipResetSequenceRequest request2 = new SalesQuotationShipResetSequenceRequest(
            groupId, companyId, userPoid, tableName, masterVoSqlName
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
        String masterVoSqlName = "MasterVO";

        SalesQuotationShipResetSequenceRequest request = new SalesQuotationShipResetSequenceRequest(
            groupId, companyId, userPoid, tableName, masterVoSqlName
        );

        String toString = request.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("SalesQuotationShipResetSequenceRequest"));
    }
}

