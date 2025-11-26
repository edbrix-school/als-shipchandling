package com.alsharif.shipchandling.salesquotation.dto;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class SalesQuotationShipDeliveryOptionValidateRequestTest {

    @Test
    void testRecordCreation() {
        BigDecimal transactionPoid = BigDecimal.valueOf(300);
        BigDecimal detailRowId = BigDecimal.valueOf(400);
        BigDecimal stockPoid = BigDecimal.valueOf(500);
        String selectionFlag = "Y";

        SalesQuotationShipDeliveryOptionValidateRequest request = new SalesQuotationShipDeliveryOptionValidateRequest(
            transactionPoid, detailRowId, stockPoid, selectionFlag
        );

        assertEquals(transactionPoid, request.transactionPoid());
        assertEquals(detailRowId, request.detailRowId());
        assertEquals(stockPoid, request.stockPoid());
        assertEquals(selectionFlag, request.selectionFlag());
    }

    @Test
    void testRecordWithNullValues() {
        SalesQuotationShipDeliveryOptionValidateRequest request = new SalesQuotationShipDeliveryOptionValidateRequest(
            null, null, null, null
        );

        assertNull(request.transactionPoid());
        assertNull(request.detailRowId());
        assertNull(request.stockPoid());
        assertNull(request.selectionFlag());
    }

    @Test
    void testRecordEquality() {
        BigDecimal transactionPoid = BigDecimal.valueOf(300);
        BigDecimal detailRowId = BigDecimal.valueOf(400);
        BigDecimal stockPoid = BigDecimal.valueOf(500);
        String selectionFlag = "Y";

        SalesQuotationShipDeliveryOptionValidateRequest request1 = new SalesQuotationShipDeliveryOptionValidateRequest(
            transactionPoid, detailRowId, stockPoid, selectionFlag
        );
        SalesQuotationShipDeliveryOptionValidateRequest request2 = new SalesQuotationShipDeliveryOptionValidateRequest(
            transactionPoid, detailRowId, stockPoid, selectionFlag
        );

        assertEquals(request1, request2);
        assertEquals(request1.hashCode(), request2.hashCode());
    }

    @Test
    void testToString() {
        BigDecimal transactionPoid = BigDecimal.valueOf(300);
        BigDecimal detailRowId = BigDecimal.valueOf(400);
        BigDecimal stockPoid = BigDecimal.valueOf(500);
        String selectionFlag = "N";

        SalesQuotationShipDeliveryOptionValidateRequest request = new SalesQuotationShipDeliveryOptionValidateRequest(
            transactionPoid, detailRowId, stockPoid, selectionFlag
        );

        String toString = request.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("SalesQuotationShipDeliveryOptionValidateRequest"));
    }
}

