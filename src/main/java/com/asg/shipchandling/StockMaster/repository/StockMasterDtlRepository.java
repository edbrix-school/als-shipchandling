package com.asg.shipchandling.StockMaster.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.asg.shipchandling.StockMaster.entity.StockMasterDTLEntity;
import com.asg.shipchandling.StockMaster.entity.StockMasterDtlId;

import java.util.List;

public interface StockMasterDtlRepository extends JpaRepository<StockMasterDTLEntity, StockMasterDtlId> {
    List<StockMasterDTLEntity> findByStockPoid(Long stockPoid);
    void deleteByStockPoid(Long stockPoid);

 @Query("SELECT MAX(w.detRowId) FROM StockMasterDTLEntity w WHERE w.stockPoid = :stockPoid")
    Long findMaxDetRowIdByStockPoid(@Param("stockPoid") Long stockPoid);

@Query("SELECT COUNT(d) FROM StockMasterDTLEntity d WHERE d.stockPoid = :stockPoid AND d.remarks = :remarks")
Long countByStockPoidAndRemarks(@Param("stockPoid") Long stockPoid, @Param("remarks") String remarks);


}