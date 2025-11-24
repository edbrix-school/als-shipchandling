package com.alsharif.shipchandling.StockMaster.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.sql.Timestamp;


@Entity
@Table(name = "STOCK_MASTER")
@Data
@NoArgsConstructor
@AllArgsConstructor
// @SequenceGenerator(name = "stock_seq", sequenceName = "STOCK_POID_SEQ", allocationSize = 1)
public class StockMasterEntity {
    @Id
    // @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "stock_seq")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "STOCK_POID", nullable = false)
    private Long stockPoid;
    

    @Column(name = "STOCK_CODE", length = 100, unique = true)
    private String stockCode;

    @Column(name = "STOCK_NAME", length = 1000, nullable = false)
    private String stockName;

    @Column(name = "STOCK_NAME2", length = 100)
    private String stockName2;

    @Column(name = "STOCK_DESCRIPTION", length = 1000)
    private String stockDescription;

    @Column(name = "CATEGORY_POID")
    private Long categoryPoid;

    @Column(name = "STOCK_UNIT_POID")
    private Long stockUnitPoid;

    @Column(name = "PURCHASE_STOCK_UNIT_POID")
    private Long purchaseStockUnitPoid;

    @Column(name = "PURCHASE_SALES_CONVERSION")
    private BigDecimal purchaseSalesConversion;

    @Column(name = "STOCK_COST")
    private BigDecimal stockCost;

    @Column(name = "TAG_PRICE")
    private BigDecimal tagPrice;

    @Column(name = "RETAIL_PRICE")
    private BigDecimal retailPrice;

    @Column(name = "WHOLESALE_PRICE")
    private BigDecimal wholesalePrice;

    @Column(name = "PRICE1")
    private BigDecimal price1;

    @Column(name = "PRICE2")
    private BigDecimal price2;

    @Column(name = "PRICE3")
    private BigDecimal price3;

    @Column(name = "CURRENCY_CODE", length = 20)
    private String currencyCode;

    @Column(name = "TAX_POID")
    private Long taxPoid;

    @Column(name = "INPUT_TAX_POID")
    private Long inputTaxPoid;

    @Column(name = "BARCODE", length = 50)
    private String barcode;

    @Column(name = "SUPPLIER_BARCODE", length = 50)
    private String supplierBarcode;

    @Column(name = "STOCK_GL_POID")
    private BigDecimal stockGlPoid;

    @Column(name = "SALES_GL_POID")
    private BigDecimal salesGlPoid;

    @Column(name = "COST_OF_SALES_GL_POID")
    private BigDecimal costOfSalesGlPoid;

    @Column(name = "ACTIVE", length = 1)
    private String active = "Y";

    @Column(name = "DELETED", length = 1)
    private String deleted = "N";

    @Column(name = "SERVICE_ITEM", length = 1)
    private String serviceItem = "N";

    @Column(name = "IS_CONSUMABLES", length = 1)
    private String isConsumables = "N";

    @Column(name = "EXPIRY_TRACKING", length = 1)
    private String expiryTracking = "N";

    @Column(name = "PRINT_LABEL", length = 1)
    private String printLabel = "N";

    @Column(name = "SERIAL_NO_TRACKING", length = 1)
    private String serialNoTracking = "N";

    @Column(name = "WASTAGE_PERCENTAGE")
    private BigDecimal wastagePercentage;

    @Column(name = "WEIGHT")
    private BigDecimal weight;

    @Column(name = "SEQNO")
    private Integer seqno;

    @Column(name = "REMARKS", length = 1000)
    private String remarks;

    @Column(name = "ONLINE_CATEGORY_NAME", length = 200)
    private String onlineCategoryName;

    @Column(name = "ONLINE_STOCK", length = 1)
    private String onlineStock;

    @Column(name = "IS_GIFT_CARD", length = 1)
    private String isGiftCard = "N";

    @Column(name = "CONSUMPTION_QTY")
    private BigDecimal consumptionQty;

    @Column(name = "CONSUMPTION_UNIT_POID")
    private Long consumptionUnitPoid;

    @Column(name = "MINIMUM_REQUIRED_QTY")
    private BigDecimal minimumRequiredQty;

    @Column(name = "SEASON_CODE", length = 50)
    private String seasonCode;

    @Column(name = "FABRIC_TYPE", length = 50)
    private String fabricType;

    @Column(name = "ORIGIN", length = 50)
    private String origin;

    @Column(name = "COMPOSITION", length = 200)
    private String composition;

    @Column(name = "ITEM_SIZE", length = 50)
    private String itemSize;

    @Column(name = "STOCK_BRAND", length = 100)
    private String stockBrand;

    @Column(name = "STOCK_COLOR", length = 50)
    private String stockColor;

    @Column(name = "STOCK_CARE_INSTRUCTIONS", length = 500)
    private String stockCareInstructions;

    @Column(name = "STOCK_DTLD_NARRATION", length = 2000)
    private String stockDtldNarration;

    @Column(name = "PRODUCT_TAGS", length = 500)
    private String productTags;

    @Column(name = "GROUP_POID", nullable = false)
    private Long groupPoid;

    @Column(name = "CREATED_BY", length = 20)
    private String createdBy;

    @CreationTimestamp
    @Column(name = "CREATED_DATE")
    private Timestamp createdDate;

    @Column(name = "LASTMODIFIED_BY", length = 20)
    private String lastmodifiedBy;

    @UpdateTimestamp
    @Column(name = "LASTMODIFIED_DATE")
    private Timestamp lastmodifiedDate;
}
