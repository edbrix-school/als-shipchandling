package com.alsharif.shipchandling.stockunitmaster.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FilterDtoTest {

    @Test
    void testRecordCreation() {
        String searchField = "name";
        String searchValue = "test";

        FilterDto filter = new FilterDto(searchField, searchValue);

        assertEquals(searchField, filter.searchField());
        assertEquals(searchValue, filter.searchValue());
    }

    @Test
    void testRecordWithNullValues() {
        FilterDto filter = new FilterDto(null, null);

        assertNull(filter.searchField());
        assertNull(filter.searchValue());
    }

    @Test
    void testRecordEquality() {
        String searchField = "name";
        String searchValue = "test";

        FilterDto filter1 = new FilterDto(searchField, searchValue);
        FilterDto filter2 = new FilterDto(searchField, searchValue);

        assertEquals(filter1, filter2);
        assertEquals(filter1.hashCode(), filter2.hashCode());
    }

    @Test
    void testRecordInequality() {
        FilterDto filter1 = new FilterDto("name", "test1");
        FilterDto filter2 = new FilterDto("name", "test2");

        assertNotEquals(filter1, filter2);
    }

    @Test
    void testToString() {
        String searchField = "code";
        String searchValue = "ABC123";

        FilterDto filter = new FilterDto(searchField, searchValue);

        String toString = filter.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("FilterDto"));
    }
}

