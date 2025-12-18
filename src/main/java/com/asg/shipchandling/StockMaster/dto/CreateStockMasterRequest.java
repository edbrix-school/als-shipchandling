package com.asg.shipchandling.StockMaster.dto;

import jakarta.validation.constraints.Max;
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
    @Size(max = 100, message = "Stock name must not exceed 100 characters")
    private String stockName;

    @Size(max = 100, message = "Stock name 2 must not exceed 100 characters")
    private String stockName2;

    @Size(max = 500, message = "Stock description must not exceed 500 characters")
    private String stockDescription;

    @NotNull(message = "Category is required")
    private Long categoryPoid;

    @NotNull(message = "Stock unit is required")
    private Long stockUnitPoid;

    @NotNull(message = "Purchase stock unit is required")
    private Long purchaseStockUnitPoid;
    private BigDecimal purchaseSalesConversion;
    private BigDecimal stockCost;
    private BigDecimal tagPrice;
    private BigDecimal retailPrice;
    private BigDecimal wholesalePrice;
    @NotNull(message = "Price 1 is required")
    private BigDecimal price1;
    private BigDecimal price2;
    private BigDecimal price3;
    @NotBlank(message = "Currency code is required")
    @Size(max = 20, message = "Currency code must not exceed 20 characters")
    private String currencyCode;

    @NotNull(message = "Tax POID is required")
    private Long taxPoid;

    @NotNull(message = "Input tax POID is required")
    private Long inputTaxPoid;

    @Size(max = 50, message = "Barcode must not exceed 50 characters")
    private String barcode;

    @Size(max = 100, message = "Supplier barcode must not exceed 100 characters")
    private String supplierBarcode;

    @Size(max = 1, message = "Active must be a single character")
    private String active;
    @Size(max = 1, message = "Service item flag must be a single character")
    private String serviceItem;
    @NotBlank(message = "Consumables flag is required")
    @Size(max = 1, message = "Consumables flag must be a single character")
    private String isConsumables;
    @NotBlank(message = "Expiry tracking flag is required")
    @Size(max = 1, message = "Expiry tracking flag must be a single character")
    private String expiryTracking;
    @NotBlank(message = "Print label flag is required")
    @Size(max = 1, message = "Print label flag must be a single character")
    private String printLabel;
    @Size(max = 1, message = "Serial tracking flag must be a single character")
    private String serialNoTracking;
    private BigDecimal wastagePercentage;
    private BigDecimal weight;
    @Max(value = 99999, message = "Sequence number must be 5 digits or less")
    private Integer seqno;
    @Size(max = 200, message = "Remarks must not exceed 200 characters")
    private String remarks;
    @Size(max = 500, message = "Online category name must not exceed 500 characters")
    private String onlineCategoryName;
    @Size(max = 1, message = "Online stock flag must be a single character")
    private String onlineStock;
    @Size(max = 1, message = "Gift card flag must be a single character")
    private String isGiftCard;
    private BigDecimal consumptionQty;
    private Long consumptionUnitPoid;
    private BigDecimal minimumRequiredQty;
    @Size(max = 5, message = "Season code must not exceed 5 characters")
    private String seasonCode;
    @Size(max = 2, message = "Fabric type must not exceed 2 characters")
    private String fabricType;
    @Size(max = 45, message = "Origin must not exceed 45 characters")
    private String origin;
    @Size(max = 30, message = "Composition must not exceed 30 characters")
    private String composition;
    @Size(max = 15, message = "Item size must not exceed 15 characters")
    private String itemSize;
    @Size(max = 200, message = "Stock brand must not exceed 200 characters")
    private String stockBrand;
    @Size(max = 500, message = "Stock color must not exceed 500 characters")
    private String stockColor;
    @Size(max = 2500, message = "Care instructions must not exceed 2500 characters")
    private String stockCareInstructions;
    @Size(max = 2000, message = "Detailed narration must not exceed 2000 characters")
    private String stockDtldNarration;
    @Size(max = 200, message = "Product tags must not exceed 200 characters")
    private String productTags;

    private BigDecimal stockGlPoid;
    private BigDecimal salesGlPoid;
    private BigDecimal costOfSalesGlPoid;
    
    // Detail tables
    private List<CreateStockMasterDtlRequest> supplierDetails;
    private List<CreateStockMasterWarehouseDtlRequest> warehouseDetails;
}
