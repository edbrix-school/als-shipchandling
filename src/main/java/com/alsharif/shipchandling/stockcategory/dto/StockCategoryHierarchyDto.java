package com.alsharif.shipchandling.stockcategory.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockCategoryHierarchyDto {
    private Long categoryPoid;
    private String categoryCode;
    private String categoryName;
    private String categoryType;
    private Integer level; // 0 = root, 1 = first level, etc.
}
