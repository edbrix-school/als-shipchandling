package com.alsharif.shipchandling.stockcategory.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDependenciesDto {
    private Long categoryPoid;
    private Boolean canDelete;
    private String reason;
    private Long childCategoryCount;
    private Long stockItemCount;
    private String message;
}
