package com.alsharif.shipchandling.stockcategory.dto.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStockCategoryRequest {

    @NotBlank(message = "Category code is required")
    @Size(max = 20, message = "Category code must not exceed 20 characters")
    private String categoryCode;

    @NotBlank(message = "Category name is required")
    @Size(max = 100, message = "Category name must not exceed 100 characters")
    private String categoryName;

    @Size(max = 100, message = "Category name 2 must not exceed 100 characters")
    private String categoryName2;

    @NotBlank(message = "Category type is required")
    private String categoryType;

    private Long parentCategoryPoid;

    private BigDecimal outputTaxPoid;
    private BigDecimal inputTaxPoid;
    private BigDecimal costCenterPoid;
    private Integer seqno;
    private String active;
}
