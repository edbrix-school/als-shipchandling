package com.alsharif.shipchandling.salesquotation.dto;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class SalesQuotationShipTreeResponseTest {

    @Test
    void testRecordCreation() {
        List<Map<String, Object>> nodes = new ArrayList<>();
        Map<String, Object> node1 = new HashMap<>();
        node1.put("id", 1);
        node1.put("name", "Node 1");
        nodes.add(node1);

        SalesQuotationShipTreeResponse response = new SalesQuotationShipTreeResponse(nodes);

        assertEquals(nodes, response.nodes());
        assertEquals(1, response.nodes().size());
        assertEquals(1, response.nodes().get(0).get("id"));
        assertEquals("Node 1", response.nodes().get(0).get("name"));
    }

    @Test
    void testRecordWithNullValues() {
        SalesQuotationShipTreeResponse response = new SalesQuotationShipTreeResponse(null);

        assertNull(response.nodes());
    }

    @Test
    void testRecordWithEmptyList() {
        List<Map<String, Object>> emptyList = new ArrayList<>();

        SalesQuotationShipTreeResponse response = new SalesQuotationShipTreeResponse(emptyList);

        assertNotNull(response.nodes());
        assertTrue(response.nodes().isEmpty());
    }

    @Test
    void testRecordEquality() {
        List<Map<String, Object>> nodes = new ArrayList<>();
        Map<String, Object> node1 = new HashMap<>();
        node1.put("id", 1);
        nodes.add(node1);

        SalesQuotationShipTreeResponse response1 = new SalesQuotationShipTreeResponse(nodes);
        SalesQuotationShipTreeResponse response2 = new SalesQuotationShipTreeResponse(nodes);

        assertEquals(response1, response2);
        assertEquals(response1.hashCode(), response2.hashCode());
    }

    @Test
    void testToString() {
        List<Map<String, Object>> nodes = new ArrayList<>();
        Map<String, Object> node1 = new HashMap<>();
        node1.put("id", 1);
        nodes.add(node1);

        SalesQuotationShipTreeResponse response = new SalesQuotationShipTreeResponse(nodes);

        String toString = response.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("SalesQuotationShipTreeResponse"));
    }
}

