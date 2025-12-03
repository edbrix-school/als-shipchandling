package com.asg.shipchandling.StockMaster.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockDetailsResponse {
    // Stock Master fields
    private Long stockPoid;
    private String stockCode;
    private String stockName;
    private String stockName2;
    private String stockDescription;
    private Long stockUnitPoid;
    private BigDecimal stockCost;
    private BigDecimal tagPrice;
    private BigDecimal retailPrice;
    private BigDecimal wholesalePrice;
    private BigDecimal price1;
    private BigDecimal price2;
    private BigDecimal price3;
    private String currencyCode;
    private String barcode;
    private String active;
    private String deleted;

    // Category Details
    private CategoryDetailDto categoryDetails;

    // Tax Details
    private TaxDetailDto taxDetails;

    // Unit Details
    private UnitDetailDto unitDetails;

    // Inner class for Category Details
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryDetailDto {
        private Long categoryPoid;
        private String categoryCode;
        private String categoryName;
    }

    // Inner class for Tax Details
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaxDetailDto {
        private Long taxPoid;
        private String taxCode;
        private String taxName;
        private BigDecimal taxPercentage;
    }

    // Inner class for Unit Details
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UnitDetailDto {
        private Long unitPoid;
        private String unitCode;
        private String unitName;
    }
}
