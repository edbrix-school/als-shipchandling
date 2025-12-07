package com.asg.shipchandling.stockcategory.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockCategoryGlValuesDto {
    private Long parentCategoryPoid;
    private String parentCategoryCode;
    private String parentCategoryName;
    private Long stockGlPoid;
    private Long salesGlPoid;
    private Long costOfSalesGlPoid;
}
