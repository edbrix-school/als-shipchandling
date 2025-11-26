package com.alsharif.shipchandling.salesquotation.dto;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class SalesQuotationShipApprovalResponseTest {

    @Test
    void testRecordCreation() {
        String status = "APPROVED";
        String message = "Approval successful";
        BigDecimal nextApprover = BigDecimal.valueOf(500);

        SalesQuotationShipApprovalResponse response = new SalesQuotationShipApprovalResponse(
            status, message, nextApprover
        );

        assertEquals(status, response.status());
        assertEquals(message, response.message());
        assertEquals(nextApprover, response.nextApprover());
    }

    @Test
    void testRecordWithNullValues() {
        SalesQuotationShipApprovalResponse response = new SalesQuotationShipApprovalResponse(
            null, null, null
        );

        assertNull(response.status());
        assertNull(response.message());
        assertNull(response.nextApprover());
    }

    @Test
    void testRecordEquality() {
        String status = "APPROVED";
        String message = "Approval successful";
        BigDecimal nextApprover = BigDecimal.valueOf(500);

        SalesQuotationShipApprovalResponse response1 = new SalesQuotationShipApprovalResponse(
            status, message, nextApprover
        );
        SalesQuotationShipApprovalResponse response2 = new SalesQuotationShipApprovalResponse(
            status, message, nextApprover
        );

        assertEquals(response1, response2);
        assertEquals(response1.hashCode(), response2.hashCode());
    }

    @Test
    void testToString() {
        String status = "REJECTED";
        String message = "Approval rejected";
        BigDecimal nextApprover = null;

        SalesQuotationShipApprovalResponse response = new SalesQuotationShipApprovalResponse(
            status, message, nextApprover
        );

        String toString = response.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("SalesQuotationShipApprovalResponse"));
    }
}

