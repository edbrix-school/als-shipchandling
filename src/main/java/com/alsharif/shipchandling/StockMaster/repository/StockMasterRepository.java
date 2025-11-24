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

}