package com.alsharif.shipchandling.commonlov.dto;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class LovResponseTest {

    @Test
    void testDefaultConstructor() {
        LovResponse response = new LovResponse();
        assertNotNull(response);
        assertNull(response.getItems());
    }

    @Test
    void testParameterizedConstructor() {
        List<LovItem> items = new ArrayList<>();
        items.add(new LovItem(1L, "CODE1", "DESC1"));
        items.add(new LovItem(2L, "CODE2", "DESC2"));

        LovResponse response = new LovResponse(items);

        assertNotNull(response.getItems());
        assertEquals(2, response.getItems().size());
        assertEquals(items, response.getItems());
    }

    @Test
    void testSettersAndGetters() {
        LovResponse response = new LovResponse();

        List<LovItem> items = new ArrayList<>();
        items.add(new LovItem(1L, "CODE1", "DESC1"));

        response.setItems(items);

        assertNotNull(response.getItems());
        assertEquals(1, response.getItems().size());
        assertEquals(items, response.getItems());
    }

    @Test
    void testSetItemsWithEmptyList() {
        LovResponse response = new LovResponse();
        List<LovItem> emptyList = new ArrayList<>();

        response.setItems(emptyList);

        assertNotNull(response.getItems());
        assertEquals(0, response.getItems().size());
    }

    @Test
    void testSetItemsWithNull() {
        LovResponse response = new LovResponse(new ArrayList<>());
        response.setItems(null);

        assertNull(response.getItems());
    }

    @Test
    void testParameterizedConstructorWithEmptyList() {
        List<LovItem> emptyList = new ArrayList<>();
        LovResponse response = new LovResponse(emptyList);

        assertNotNull(response.getItems());
        assertEquals(0, response.getItems().size());
    }

    @Test
    void testParameterizedConstructorWithNullList() {
        LovResponse response = new LovResponse(null);

        assertNull(response.getItems());
    }
}

