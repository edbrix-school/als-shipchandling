package com.alsharif.shipchandling.salesquotation.dto;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class SalesQuotationShipGlPostingResponseTest {

    @Test
    void testRecordCreation() {
        List<Map<String, Object>> glHeaders = new ArrayList<>();
        Map<String, Object> header1 = new HashMap<>();
        header1.put("id", 1);
        glHeaders.add(header1);

        List<Map<String, Object>> glLines = new ArrayList<>();
        Map<String, Object> line1 = new HashMap<>();
        line1.put("lineId", 100);
        glLines.add(line1);

        List<Map<String, Object>> taxBreakup = new ArrayList<>();
        List<Map<String, Object>> additionalInfo = new ArrayList<>();

        SalesQuotationShipGlPostingResponse response = new SalesQuotationShipGlPostingResponse(
            glHeaders, glLines, taxBreakup, additionalInfo
        );

        assertEquals(glHeaders, response.glHeaders());
        assertEquals(glLines, response.glLines());
        assertEquals(taxBreakup, response.taxBreakup());
        assertEquals(additionalInfo, response.additionalInfo());
    }

    @Test
    void testRecordWithNullValues() {
        SalesQuotationShipGlPostingResponse response = new SalesQuotationShipGlPostingResponse(
            null, null, null, null
        );

        assertNull(response.glHeaders());
        assertNull(response.glLines());
        assertNull(response.taxBreakup());
        assertNull(response.additionalInfo());
    }

    @Test
    void testRecordWithEmptyLists() {
        List<Map<String, Object>> emptyList = new ArrayList<>();

        SalesQuotationShipGlPostingResponse response = new SalesQuotationShipGlPostingResponse(
            emptyList, emptyList, emptyList, emptyList
        );

        assertNotNull(response.glHeaders());
        assertTrue(response.glHeaders().isEmpty());
        assertNotNull(response.glLines());
        assertTrue(response.glLines().isEmpty());
        assertNotNull(response.taxBreakup());
        assertTrue(response.taxBreakup().isEmpty());
        assertNotNull(response.additionalInfo());
        assertTrue(response.additionalInfo().isEmpty());
    }

    @Test
    void testRecordEquality() {
        List<Map<String, Object>> glHeaders = new ArrayList<>();
        List<Map<String, Object>> glLines = new ArrayList<>();
        List<Map<String, Object>> taxBreakup = new ArrayList<>();
        List<Map<String, Object>> additionalInfo = new ArrayList<>();

        SalesQuotationShipGlPostingResponse response1 = new SalesQuotationShipGlPostingResponse(
            glHeaders, glLines, taxBreakup, additionalInfo
        );
        SalesQuotationShipGlPostingResponse response2 = new SalesQuotationShipGlPostingResponse(
            glHeaders, glLines, taxBreakup, additionalInfo
        );

        assertEquals(response1, response2);
        assertEquals(response1.hashCode(), response2.hashCode());
    }

    @Test
    void testToString() {
        List<Map<String, Object>> glHeaders = new ArrayList<>();
        List<Map<String, Object>> glLines = new ArrayList<>();
        List<Map<String, Object>> taxBreakup = new ArrayList<>();
        List<Map<String, Object>> additionalInfo = new ArrayList<>();

        SalesQuotationShipGlPostingResponse response = new SalesQuotationShipGlPostingResponse(
            glHeaders, glLines, taxBreakup, additionalInfo
        );

        String toString = response.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("SalesQuotationShipGlPostingResponse"));
    }
}

