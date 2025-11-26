package com.alsharif.shipchandling.salesquotation.controller;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SalesQuotationShipControllerApplyLocalChargesRequestTest {

    @Test
    void testRecordCreation() {
        String loginUser = "testuser";

        SalesQuotationShipController.ApplyLocalChargesRequest request = 
            new SalesQuotationShipController.ApplyLocalChargesRequest(loginUser);

        assertEquals(loginUser, request.loginUser());
    }

    @Test
    void testRecordWithNullValues() {
        SalesQuotationShipController.ApplyLocalChargesRequest request = 
            new SalesQuotationShipController.ApplyLocalChargesRequest(null);

        assertNull(request.loginUser());
    }

    @Test
    void testRecordEquality() {
        String loginUser = "testuser";

        SalesQuotationShipController.ApplyLocalChargesRequest request1 = 
            new SalesQuotationShipController.ApplyLocalChargesRequest(loginUser);
        SalesQuotationShipController.ApplyLocalChargesRequest request2 = 
            new SalesQuotationShipController.ApplyLocalChargesRequest(loginUser);

        assertEquals(request1, request2);
        assertEquals(request1.hashCode(), request2.hashCode());
    }

    @Test
    void testToString() {
        String loginUser = "testuser";

        SalesQuotationShipController.ApplyLocalChargesRequest request = 
            new SalesQuotationShipController.ApplyLocalChargesRequest(loginUser);

        String toString = request.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("ApplyLocalChargesRequest"));
    }
}

