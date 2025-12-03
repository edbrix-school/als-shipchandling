package com.asg.shipchandling.StockMaster.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockMasterDto {
    private Long stockPoid;
    private String stockCode;
    private String stockName;
    private String stockName2;
    private String stockDescription;
    private Long categoryPoid;
    private Long stockUnitPoid;
    private Long purchaseStockUnitPoid;
    private BigDecimal purchaseSalesConversion;
    private BigDecimal stockCost;
    private BigDecimal tagPrice;
    private BigDecimal retailPrice;
    private BigDecimal wholesalePrice;
    private BigDecimal price1;
    private BigDecimal price2;
    private BigDecimal price3;
    private String currencyCode;
    private Long taxPoid;
    private Long inputTaxPoid;
    private String barcode;
    private String supplierBarcode;
    private BigDecimal stockGlPoid;
    private BigDecimal salesGlPoid;
    private BigDecimal costOfSalesGlPoid;
    private String active;
    private String deleted;
    private String serviceItem;
    private String isConsumables;
    private String expiryTracking;
    private String printLabel;
    private String serialNoTracking;
    private BigDecimal wastagePercentage;
    private BigDecimal weight;
    private Integer seqno;
    private String remarks;
    private String onlineCategoryName;
    private String onlineStock;
    private String isGiftCard;
    private BigDecimal consumptionQty;
    private Long consumptionUnitPoid;
    private BigDecimal minimumRequiredQty;
    private String seasonCode;
    private String fabricType;
    private String origin;
    private String composition;
    private String itemSize;
    private String stockBrand;
    private String stockColor;
    private String stockCareInstructions;
    private String stockDtldNarration;
    private String productTags;
    private Long groupPoid;
    private String createdBy;
    private Timestamp createdDate;
    private String lastmodifiedBy;
    private Timestamp lastmodifiedDate;
    
    // Detail tables
   private List<StockMasterDtlDto> supplierDetails;
   private List<StockMasterWarehouseDtlDto> warehouseDetails;
}
