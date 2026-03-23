package com.asg.shipchandling.stockcategory.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockCategoryMasterDto {
    private Long categoryPoid;
    private String categoryCode;
    private String categoryName;
    private String categoryName2;
    private String categoryType;
    private Long parentCategoryPoid;
    private PoidDetailsDto parentCategoryPoidDetails;
    private Long stockGlPoid;
    private PoidDetailsDto stockGlPoidDetails;
    private Long salesGlPoid;
    private PoidDetailsDto salesGlPoidDetails;
    private Long costOfSalesGlPoid;
    private PoidDetailsDto costOfSalesGlPoidDetails;
    private BigDecimal outputTaxPoid;
    private PoidDetailsDto outputTaxPoidDetails;
    private BigDecimal inputTaxPoid;
    private PoidDetailsDto inputTaxPoidDetails;
    private BigDecimal costCenterPoid;
    private PoidDetailsDto costCenterPoidDetails;
    private Integer seqno;
    private String active;
    private String deleted;
    private Long groupPoid;
    private String createdBy;
    private LocalDateTime createdDate;
    private String lastmodifiedBy;
    private LocalDateTime lastmodifiedDate;
}