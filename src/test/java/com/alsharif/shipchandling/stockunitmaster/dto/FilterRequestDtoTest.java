package com.alsharif.shipchandling.stockunitmaster.dto;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class FilterRequestDtoTest {

    @Test
    void testRecordCreation() {
        String operator = "AND";
        String isDeleted = "false";
        List<FilterDto> filters = new ArrayList<>();
        filters.add(new FilterDto("name", "test"));

        FilterRequestDto request = new FilterRequestDto(operator, isDeleted, filters);

        assertEquals(operator, request.operator());
        assertEquals(isDeleted, request.isDeleted());
        assertEquals(filters, request.filters());
    }

    @Test
    void testRecordWithNullValues() {
        FilterRequestDto request = new FilterRequestDto(null, null, null);

        assertNull(request.operator());
        assertNull(request.isDeleted());
        assertNull(request.filters());
    }

    @Test
    void testRecordWithEmptyFilters() {
        String operator = "OR";
        String isDeleted = "true";
        List<FilterDto> emptyFilters = new ArrayList<>();

        FilterRequestDto request = new FilterRequestDto(operator, isDeleted, emptyFilters);

        assertEquals(operator, request.operator());
        assertEquals(isDeleted, request.isDeleted());
        assertNotNull(request.filters());
        assertTrue(request.filters().isEmpty());
    }

    @Test
    void testRecordEquality() {
        String operator = "AND";
        String isDeleted = "false";
        List<FilterDto> filters = new ArrayList<>();
        filters.add(new FilterDto("name", "test"));

        FilterRequestDto request1 = new FilterRequestDto(operator, isDeleted, filters);
        FilterRequestDto request2 = new FilterRequestDto(operator, isDeleted, filters);

        assertEquals(request1, request2);
        assertEquals(request1.hashCode(), request2.hashCode());
    }

    @Test
    void testToString() {
        String operator = "OR";
        String isDeleted = "false";
        List<FilterDto> filters = new ArrayList<>();

        FilterRequestDto request = new FilterRequestDto(operator, isDeleted, filters);

        String toString = request.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("FilterRequestDto"));
    }
}

