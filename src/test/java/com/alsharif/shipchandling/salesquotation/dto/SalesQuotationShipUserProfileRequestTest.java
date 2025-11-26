package com.alsharif.shipchandling.salesquotation.dto;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class SalesQuotationShipUserProfileRequestTest {

    @Test
    void testRecordCreation() {
        BigDecimal userPoid = BigDecimal.valueOf(500);
        String settingName = "theme";
        String settingValue = "dark";

        SalesQuotationShipUserProfileRequest request = new SalesQuotationShipUserProfileRequest(
            userPoid, settingName, settingValue
        );

        assertEquals(userPoid, request.userPoid());
        assertEquals(settingName, request.settingName());
        assertEquals(settingValue, request.settingValue());
    }

    @Test
    void testRecordWithNullValues() {
        SalesQuotationShipUserProfileRequest request = new SalesQuotationShipUserProfileRequest(
            null, null, null
        );

        assertNull(request.userPoid());
        assertNull(request.settingName());
        assertNull(request.settingValue());
    }

    @Test
    void testRecordEquality() {
        BigDecimal userPoid = BigDecimal.valueOf(500);
        String settingName = "theme";
        String settingValue = "dark";

        SalesQuotationShipUserProfileRequest request1 = new SalesQuotationShipUserProfileRequest(
            userPoid, settingName, settingValue
        );
        SalesQuotationShipUserProfileRequest request2 = new SalesQuotationShipUserProfileRequest(
            userPoid, settingName, settingValue
        );

        assertEquals(request1, request2);
        assertEquals(request1.hashCode(), request2.hashCode());
    }

    @Test
    void testToString() {
        BigDecimal userPoid = BigDecimal.valueOf(500);
        String settingName = "language";
        String settingValue = "en";

        SalesQuotationShipUserProfileRequest request = new SalesQuotationShipUserProfileRequest(
            userPoid, settingName, settingValue
        );

        String toString = request.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("SalesQuotationShipUserProfileRequest"));
    }
}

