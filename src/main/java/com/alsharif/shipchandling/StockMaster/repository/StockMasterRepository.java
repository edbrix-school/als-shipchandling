package com.alsharif.shipchandling.StockMaster.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.alsharif.shipchandling.StockMaster.entity.StockMasterEntity;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface StockMasterRepository extends JpaRepository<StockMasterEntity, Long>, JpaSpecificationExecutor<StockMasterEntity> {

    boolean existsByStockCodeIgnoreCaseAndGroupPoidAndStockPoidNot(
            String stockCode, Long groupPoid, Long stockPoid);

    boolean existsByStockCodeIgnoreCaseAndGroupPoid(String stockCode, Long groupPoid);

    boolean existsByStockNameAndGroupPoid(String stockName, Long groupPoid);

    boolean existsByStockNameAndGroupPoidAndStockPoidNot(String stockName, Long groupPoid, Long excludeStockPoid);

    Optional<StockMasterEntity> findByStockPoidAndGroupPoid(Long stockPoid, Long groupPoid);

      Optional<StockMasterEntity> findByStockPoid(Long stockPoid);

     Optional<StockMasterEntity> findByBarcodeAndGroupPoidAndDeletedNot(String barcode, Long groupPoid, String deleted);

//     Optional<StockMasterEntity> findBySupplierBarcodeAndGroupPoidAndDeletedNot(String supplierBarcode, Long groupPoid, String deleted);


    @Query("SELECT s FROM StockMasterEntity s " +
       "WHERE (s.barcode = :barcode OR s.supplierBarcode = :barcode) " +
       "AND s.groupPoid = :groupPoid " +
       "AND s.deleted <> 'Y'")
        Optional<StockMasterEntity> findByBarcodeOrSupplierBarcode(
        @Param("barcode") String barcode,
        @Param("groupPoid") Long groupPoid);

    /**
     * Get Stock Master with all detail objects in a single query using JOINs
     * Returns a Map with stock master data and all related details
     */
    @Query(value = "SELECT " +
            "sm.STOCK_POID as stockPoid, sm.STOCK_CODE as stockCode, sm.STOCK_NAME as stockName, " +
            "sm.STOCK_NAME2 as stockName2, sm.STOCK_DESCRIPTION as stockDescription, " +
            "sm.CATEGORY_POID as categoryPoid, sm.STOCK_UNIT_POID as stockUnitPoid, " +
            "sm.PURCHASE_STOCK_UNIT_POID as purchaseStockUnitPoid, sm.CONSUMPTION_UNIT_POID as consumptionUnitPoid, " +
            "sm.PURCHASE_SALES_CONVERSION as purchaseSalesConversion, sm.STOCK_COST as stockCost, " +
            "sm.TAG_PRICE as tagPrice, sm.RETAIL_PRICE as retailPrice, sm.WHOLESALE_PRICE as wholesalePrice, " +
            "sm.PRICE1 as price1, sm.PRICE2 as price2, sm.PRICE3 as price3, sm.CURRENCY_CODE as currencyCode, " +
            "sm.TAX_POID as taxPoid, sm.INPUT_TAX_POID as inputTaxPoid, " +
            "sm.BARCODE as barcode, sm.SUPPLIER_BARCODE as supplierBarcode, " +
            "sm.STOCK_GL_POID as stockGlPoid, sm.SALES_GL_POID as salesGlPoid, sm.COST_OF_SALES_GL_POID as costOfSalesGlPoid, " +
            "sm.ACTIVE as active, sm.DELETED as deleted, sm.SERVICE_ITEM as serviceItem, " +
            "sm.IS_CONSUMABLES as isConsumables, sm.EXPIRY_TRACKING as expiryTracking, " +
            "sm.PRINT_LABEL as printLabel, sm.SERIAL_NO_TRACKING as serialNoTracking, " +
            "sm.WASTAGE_PERCENTAGE as wastagePercentage, sm.WEIGHT as weight, sm.SEQNO as seqno, " +
            "sm.REMARKS as remarks, sm.ONLINE_CATEGORY_NAME as onlineCategoryName, " +
            "sm.ONLINE_STOCK as onlineStock, sm.IS_GIFT_CARD as isGiftCard, " +
            "sm.CONSUMPTION_QTY as consumptionQty, sm.MINIMUM_REQUIRED_QTY as minimumRequiredQty, " +
            "sm.SEASON_CODE as seasonCode, sm.FABRIC_TYPE as fabricType, sm.ORIGIN as origin, " +
            "sm.COMPOSITION as composition, sm.ITEM_SIZE as itemSize, sm.STOCK_BRAND as stockBrand, " +
            "sm.STOCK_COLOR as stockColor, sm.STOCK_CARE_INSTRUCTIONS as stockCareInstructions, " +
            "sm.STOCK_DTLD_NARRATION as stockDtldNarration, sm.PRODUCT_TAGS as productTags, " +
            "sm.GROUP_POID as groupPoid, sm.CREATED_BY as createdBy, sm.CREATED_DATE as createdDate, " +
            "sm.LASTMODIFIED_BY as lastmodifiedBy, sm.LASTMODIFIED_DATE as lastmodifiedDate, " +
            // Stock Unit Details (using unique aliases)
            "su1.STOCK_UNIT_POID as su1Poid, su1.STOCK_UNIT_CODE as su1Code, su1.STOCK_UNIT_NAME as su1Name, " +
            // Purchase Stock Unit Details
            "su2.STOCK_UNIT_POID as su2Poid, su2.STOCK_UNIT_CODE as su2Code, su2.STOCK_UNIT_NAME as su2Name, " +
            // Consumption Unit Details
            "su3.STOCK_UNIT_POID as su3Poid, su3.STOCK_UNIT_CODE as su3Code, su3.STOCK_UNIT_NAME as su3Name, " +
            // Tax Details (Output Tax)
            "tax1.TAX_POID as tax1Poid, tax1.TAX_CODE as tax1Code, tax1.TAX_NAME as tax1Name, " +
            // Input Tax Details
            "tax2.TAX_POID as tax2Poid, tax2.TAX_CODE as tax2Code, tax2.TAX_NAME as tax2Name, " +
            // Stock GL Details
            "gl1.GL_POID as gl1Poid, gl1.GL_CODE as gl1Code, gl1.GL_DESCRIPTION as gl1Description, " +
            // Sales GL Details
            "gl2.GL_POID as gl2Poid, gl2.GL_CODE as gl2Code, gl2.GL_DESCRIPTION as gl2Description, " +
            // Cost of Sales GL Details
            "gl3.GL_POID as gl3Poid, gl3.GL_CODE as gl3Code, gl3.GL_DESCRIPTION as gl3Description " +
            "FROM STOCK_MASTER sm " +
            "LEFT JOIN STOCK_UNIT_MASTER su1 ON sm.STOCK_UNIT_POID = su1.STOCK_UNIT_POID " +
            "LEFT JOIN STOCK_UNIT_MASTER su2 ON sm.PURCHASE_STOCK_UNIT_POID = su2.STOCK_UNIT_POID " +
            "LEFT JOIN STOCK_UNIT_MASTER su3 ON sm.CONSUMPTION_UNIT_POID = su3.STOCK_UNIT_POID " +
            "LEFT JOIN GLOBAL_TAX_MASTER tax1 ON sm.TAX_POID = tax1.TAX_POID " +
            "LEFT JOIN GLOBAL_TAX_MASTER tax2 ON sm.INPUT_TAX_POID = tax2.TAX_POID " +
            "LEFT JOIN GL_MASTER gl1 ON TO_NUMBER(sm.STOCK_GL_POID) = gl1.GL_POID " +
            "LEFT JOIN GL_MASTER gl2 ON TO_NUMBER(sm.SALES_GL_POID) = gl2.GL_POID " +
            "LEFT JOIN GL_MASTER gl3 ON TO_NUMBER(sm.COST_OF_SALES_GL_POID) = gl3.GL_POID " +
            "WHERE sm.STOCK_POID = :stockPoid", nativeQuery = true)
    List<Object[]> findStockMasterWithDetails(@Param("stockPoid") Long stockPoid);

}