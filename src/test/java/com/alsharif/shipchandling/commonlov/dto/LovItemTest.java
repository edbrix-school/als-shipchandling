package com.alsharif.shipchandling.commonlov.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LovItemTest {

    @Test
    void testDefaultConstructor() {
        LovItem item = new LovItem();
        assertNotNull(item);
        assertNull(item.getPoid());
        assertNull(item.getCode());
        assertNull(item.getDescription());
    }

    @Test
    void testParameterizedConstructor() {
        Long poid = 123L;
        String code = "TEST_CODE";
        String description = "Test Description";

        LovItem item = new LovItem(poid, code, description);

        assertEquals(poid, item.getPoid());
        assertEquals(code, item.getCode());
        assertEquals(description, item.getDescription());
    }

    @Test
    void testSettersAndGetters() {
        LovItem item = new LovItem();

        Long poid = 456L;
        String code = "CODE_456";
        String description = "Description 456";

        item.setPoid(poid);
        item.setCode(code);
        item.setDescription(description);

        assertEquals(poid, item.getPoid());
        assertEquals(code, item.getCode());
        assertEquals(description, item.getDescription());
    }

    @Test
    void testSettersWithNullValues() {
        LovItem item = new LovItem(1L, "CODE", "DESC");

        item.setPoid(null);
        item.setCode(null);
        item.setDescription(null);

        assertNull(item.getPoid());
        assertNull(item.getCode());
        assertNull(item.getDescription());
    }

    @Test
    void testParameterizedConstructorWithNullValues() {
        LovItem item = new LovItem(null, null, null);

        assertNull(item.getPoid());
        assertNull(item.getCode());
        assertNull(item.getDescription());
    }
}

