package com.alsharif.shipchandling.stockcategory.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockCategoryGlValuesDto {
    private Long parentCategoryPoid;
    private String parentCategoryCode;
    private String parentCategoryName;
    private BigDecimal stockGlPoid;
    private BigDecimal salesGlPoid;
    private BigDecimal costOfSalesGlPoid;
}
