package com.asg.shipchandling.stockcategory.dto.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ValidParentCategory
public class CreateStockCategoryRequest {

    // categoryCode is auto-generated, not required in request

    @NotBlank(message = "Category name is required")
    @Size(max = 100, message = "Category name must not exceed 100 characters")
    private String categoryName;

    @Size(max = 100, message = "Category name 2 must not exceed 100 characters")
    private String categoryName2;

    @NotBlank(message = "Category type is required")
    @Pattern(regexp = "GROUP|SUB_GROUP", message = "Category type must be either GROUP or SUB_GROUP")
    private String categoryType;

    private Long parentCategoryPoid;  // Required if categoryType is SUB_GROUP (validated by @ValidParentCategory)

    private BigDecimal outputTaxPoid;
    private BigDecimal inputTaxPoid;
    private BigDecimal costCenterPoid;
    
    private Integer seqno;
    
    @Pattern(regexp = "Y|N", message = "Active must be either Y or N")
    private String active;  // Defaults to "Y" if not provided
}
