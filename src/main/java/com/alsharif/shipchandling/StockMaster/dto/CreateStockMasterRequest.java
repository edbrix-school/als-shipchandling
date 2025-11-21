package com.alsharif.shipchandling.StockMaster.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class CreateStockMasterRequest {

     private Long stockPoid; 
    @NotBlank(message = "Stock name is required")
    @Size(max = 1000, message = "Stock name must not exceed 1000 characters")
    private String stockName;

    @Size(max = 100, message = "Stock name 2 must not exceed 100 characters")
    private String stockName2;

    @Size(max = 1000, message = "Stock description must not exceed 1000 characters")
    private String stockDescription;

    @NotNull(message = "Category is required")
    private Long categoryPoid;

    @NotNull(message = "Stock unit is required")
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

    @NotNull(message = "Tax POID is required")
    private Long taxPoid;

    @NotNull(message = "Input tax POID is required")
    private Long inputTaxPoid;

    @Size(max = 50, message = "Barcode must not exceed 50 characters")
    private String barcode;

    @Size(max = 50, message = "Supplier barcode must not exceed 50 characters")
    private String supplierBarcode;

    private String active;
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

    private BigDecimal stockGlPoid;
    private BigDecimal salesGlPoid;
    private BigDecimal costOfSalesGlPoid;
    
    // Detail tables
    private List<CreateStockMasterDtlRequest> supplierDetails;
    private List<CreateStockMasterWarehouseDtlRequest> warehouseDetails;
}
