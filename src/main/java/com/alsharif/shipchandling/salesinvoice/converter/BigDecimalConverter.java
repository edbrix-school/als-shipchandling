package com.alsharif.shipchandling.salesinvoice.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.math.BigDecimal;

@Converter(autoApply = false)
public class BigDecimalConverter implements AttributeConverter<BigDecimal, Object> {

    @Override
    public Object convertToDatabaseColumn(BigDecimal attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.toString();
    }

    @Override
    public BigDecimal convertToEntityAttribute(Object dbData) {
        if (dbData == null) {
            return null;
        }
        if (dbData instanceof BigDecimal) {
            return (BigDecimal) dbData;
        }
        if (dbData instanceof Number) {
            return BigDecimal.valueOf(((Number) dbData).doubleValue());
        }
        if (dbData instanceof String) {
            String str = ((String) dbData).trim();
            if (str.isEmpty()) {
                return null;
            }
            try {
                return new BigDecimal(str);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        // Try to convert via string representation
        try {
            return new BigDecimal(dbData.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

