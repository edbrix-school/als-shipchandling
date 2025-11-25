package com.alsharif.shipchandling.stockcategory.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.sql.Timestamp;

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
    private Long stockGlPoid;
    private Long salesGlPoid;
    private Long costOfSalesGlPoid;
    private BigDecimal outputTaxPoid;
    private BigDecimal inputTaxPoid;
    private BigDecimal costCenterPoid;
    private Integer seqno;
    private String active;
    private String deleted;
    private Long groupPoid;
    private String createdBy;
    private Timestamp createdDate;
    private String lastmodifiedBy;
    private Timestamp lastmodifiedDate;
}