package com.alsharif.shipchandling.salesinvoice.converter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class BigDecimalConverterTest {

    private BigDecimalConverter converter;

    @BeforeEach
    void setUp() {
        converter = new BigDecimalConverter();
    }

    @Test
    void testConvertToDatabaseColumn_WithValidBigDecimal() {
        // Arrange
        BigDecimal value = new BigDecimal("123.45");

        // Act
        Object result = converter.convertToDatabaseColumn(value);

        // Assert
        assertNotNull(result);
        assertEquals("123.45", result.toString());
    }

    @Test
    void testConvertToDatabaseColumn_WithNull() {
        // Act
        Object result = converter.convertToDatabaseColumn(null);

        // Assert
        assertNull(result);
    }

    @Test
    void testConvertToDatabaseColumn_WithZero() {
        // Arrange
        BigDecimal value = BigDecimal.ZERO;

        // Act
        Object result = converter.convertToDatabaseColumn(value);

        // Assert
        assertNotNull(result);
        assertEquals("0", result.toString());
    }

    @Test
    void testConvertToDatabaseColumn_WithNegativeValue() {
        // Arrange
        BigDecimal value = new BigDecimal("-123.45");

        // Act
        Object result = converter.convertToDatabaseColumn(value);

        // Assert
        assertNotNull(result);
        assertEquals("-123.45", result.toString());
    }

    @Test
    void testConvertToDatabaseColumn_WithLargeValue() {
        // Arrange
        BigDecimal value = new BigDecimal("999999999.999999999");

        // Act
        Object result = converter.convertToDatabaseColumn(value);

        // Assert
        assertNotNull(result);
        assertEquals("999999999.999999999", result.toString());
    }

    @Test
    void testConvertToEntityAttribute_WithBigDecimal() {
        // Arrange
        BigDecimal value = new BigDecimal("123.45");

        // Act
        BigDecimal result = converter.convertToEntityAttribute(value);

        // Assert
        assertNotNull(result);
        assertEquals(value, result);
    }

    @Test
    void testConvertToEntityAttribute_WithNull() {
        // Act
        BigDecimal result = converter.convertToEntityAttribute(null);

        // Assert
        assertNull(result);
    }

    @Test
    void testConvertToEntityAttribute_WithInteger() {
        // Arrange
        Integer value = 123;

        // Act
        BigDecimal result = converter.convertToEntityAttribute(value);

        // Assert
        assertNotNull(result);
        assertEquals(0, new BigDecimal("123").compareTo(result));
    }

    @Test
    void testConvertToEntityAttribute_WithLong() {
        // Arrange
        Long value = 123456789L;

        // Act
        BigDecimal result = converter.convertToEntityAttribute(value);

        // Assert
        assertNotNull(result);
        assertEquals(new BigDecimal("123456789"), result);
    }

    @Test
    void testConvertToEntityAttribute_WithDouble() {
        // Arrange
        Double value = 123.45;

        // Act
        BigDecimal result = converter.convertToEntityAttribute(value);

        // Assert
        assertNotNull(result);
        assertEquals(123.45, result.doubleValue(), 0.001);
    }

    @Test
    void testConvertToEntityAttribute_WithFloat() {
        // Arrange
        Float value = 123.45f;

        // Act
        BigDecimal result = converter.convertToEntityAttribute(value);

        // Assert
        assertNotNull(result);
        assertEquals(123.45f, result.floatValue(), 0.001);
    }

    @Test
    void testConvertToEntityAttribute_WithString_ValidNumber() {
        // Arrange
        String value = "123.45";

        // Act
        BigDecimal result = converter.convertToEntityAttribute(value);

        // Assert
        assertNotNull(result);
        assertEquals(new BigDecimal("123.45"), result);
    }

    @Test
    void testConvertToEntityAttribute_WithString_EmptyString() {
        // Arrange
        String value = "";

        // Act
        BigDecimal result = converter.convertToEntityAttribute(value);

        // Assert
        assertNull(result);
    }

    @Test
    void testConvertToEntityAttribute_WithString_WhitespaceOnly() {
        // Arrange
        String value = "   ";

        // Act
        BigDecimal result = converter.convertToEntityAttribute(value);

        // Assert
        assertNull(result);
    }

    @Test
    void testConvertToEntityAttribute_WithString_InvalidNumber() {
        // Arrange
        String value = "invalid";

        // Act
        BigDecimal result = converter.convertToEntityAttribute(value);

        // Assert
        assertNull(result);
    }

    @Test
    void testConvertToEntityAttribute_WithString_NegativeNumber() {
        // Arrange
        String value = "-123.45";

        // Act
        BigDecimal result = converter.convertToEntityAttribute(value);

        // Assert
        assertNotNull(result);
        assertEquals(new BigDecimal("-123.45"), result);
    }

    @Test
    void testConvertToEntityAttribute_WithString_Zero() {
        // Arrange
        String value = "0";

        // Act
        BigDecimal result = converter.convertToEntityAttribute(value);

        // Assert
        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    void testConvertToEntityAttribute_WithString_LargeNumber() {
        // Arrange
        String value = "999999999.999999999";

        // Act
        BigDecimal result = converter.convertToEntityAttribute(value);

        // Assert
        assertNotNull(result);
        assertEquals(new BigDecimal("999999999.999999999"), result);
    }

    @Test
    void testConvertToEntityAttribute_WithString_WithLeadingZeros() {
        // Arrange
        String value = "00123.45";

        // Act
        BigDecimal result = converter.convertToEntityAttribute(value);

        // Assert
        assertNotNull(result);
        assertEquals(new BigDecimal("123.45"), result);
    }

    @Test
    void testConvertToEntityAttribute_WithString_ScientificNotation() {
        // Arrange
        String value = "1.23E2";

        // Act
        BigDecimal result = converter.convertToEntityAttribute(value);

        // Assert
        assertNotNull(result);
        assertEquals(0, new BigDecimal("123").compareTo(result));
    }

    @Test
    void testConvertToEntityAttribute_WithObject_ToStringConversion() {
        // Arrange
        Object value = new Object() {
            @Override
            public String toString() {
                return "123.45";
            }
        };

        // Act
        BigDecimal result = converter.convertToEntityAttribute(value);

        // Assert
        assertNotNull(result);
        assertEquals(new BigDecimal("123.45"), result);
    }

    @Test
    void testConvertToEntityAttribute_WithObject_InvalidToString() {
        // Arrange
        Object value = new Object() {
            @Override
            public String toString() {
                return "invalid";
            }
        };

        // Act
        BigDecimal result = converter.convertToEntityAttribute(value);

        // Assert
        assertNull(result);
    }

    @Test
    void testRoundTripConversion() {
        // Arrange
        BigDecimal original = new BigDecimal("123.456789");

        // Act
        Object dbValue = converter.convertToDatabaseColumn(original);
        BigDecimal result = converter.convertToEntityAttribute(dbValue);

        // Assert
        assertNotNull(result);
        assertEquals(original, result);
    }

    @Test
    void testConvertToEntityAttribute_WithString_WithTrailingZeros() {
        // Arrange
        String value = "123.45000";

        // Act
        BigDecimal result = converter.convertToEntityAttribute(value);

        // Assert
        assertNotNull(result);
        assertEquals(0, new BigDecimal("123.45").compareTo(result.stripTrailingZeros()));
    }

    @Test
    void testConvertToEntityAttribute_WithByte() {
        // Arrange
        Byte value = (byte) 123;

        // Act
        BigDecimal result = converter.convertToEntityAttribute(value);

        // Assert
        assertNotNull(result);
        assertEquals(0, new BigDecimal("123").compareTo(result));
    }

    @Test
    void testConvertToEntityAttribute_WithShort() {
        // Arrange
        Short value = (short) 12345;

        // Act
        BigDecimal result = converter.convertToEntityAttribute(value);

        // Assert
        assertNotNull(result);
        assertEquals(0, new BigDecimal("12345").compareTo(result));
    }
}

