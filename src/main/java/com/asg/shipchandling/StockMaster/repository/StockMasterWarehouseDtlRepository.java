package com.asg.shipchandling.StockMaster.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.asg.shipchandling.StockMaster.entity.StockMasterWarehouseDtl;
import com.asg.shipchandling.StockMaster.entity.StockMasterWarehouseDtlId;

import java.util.List;

public interface StockMasterWarehouseDtlRepository extends JpaRepository<StockMasterWarehouseDtl, StockMasterWarehouseDtlId> {
    List<StockMasterWarehouseDtl> findByStockPoid(Long stockPoid);
    void deleteByStockPoid(Long stockPoid);

    @Query("SELECT MAX(d.detRowId) FROM StockMasterWarehouseDtl d WHERE d.stockPoid = :stockPoid")
    Long findMaxDetRowIdByStockPoid(@Param("stockPoid") Long stockPoid);
}