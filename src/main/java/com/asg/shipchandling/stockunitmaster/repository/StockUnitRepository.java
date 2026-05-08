package com.asg.shipchandling.stockunitmaster.repository;

import com.asg.shipchandling.stockunitmaster.entity.StockUnitMaster;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StockUnitRepository
        extends JpaRepository<StockUnitMaster, Long>, JpaSpecificationExecutor<StockUnitMaster> {

    boolean existsByStockUnitPoid(Long stockUnitPoid);

    boolean existsByStockUnitCode(String stockUnitCode);

    boolean existsByStockUnitName(String stockUnitName);

    boolean existsByStockUnitCodeIgnoreCaseAndStockUnitPoidNot(String stockUnitCode, Long stockUnitPoid);

    boolean existsByStockUnitNameIgnoreCaseAndStockUnitPoidNot(String stockUnitName, Long stockUnitPoid);

    StockUnitMaster findByStockUnitPoid(Long stockUnitPoid);

    List<StockUnitMaster> findAll();

    boolean existsByStockUnitCodeIgnoreCaseAndGroupPoid(String stockUnitCode, Long groupPoid);

    boolean existsByStockUnitCodeIgnoreCaseAndGroupPoidAndStockUnitPoidNot(String stockUnitCode, Long groupPoid,
            Long stockUnitPoid);

    boolean existsBystockUnitNameIgnoreCaseAndGroupPoid(String stockUnitName, Long groupPoid);

    boolean existsBystockUnitNameIgnoreCaseAndGroupPoidAndStockUnitPoidNot(String stockUnitName, Long groupPoid,
            Long stockUnitPoid);

    @Query("SELECT COUNT(s) FROM StockMasterEntity s WHERE s.stockUnitPoid = :stockUnitPoid")
    Long countStockItemsByStockUnitPoid(@Param("stockUnitPoid") Long stockUnitPoid);

    Optional<StockUnitMaster> findByStockUnitPoidAndGroupPoid(Long stockUnitPoid, Long groupPoid);

        @Query("SELECT s FROM StockUnitMaster s WHERE s.groupPoid = :groupPoid " +
           "AND (s.deleted IS NULL OR s.deleted = 'N') " +
           "AND s.active = 'Y' " +
           "ORDER BY s.seqNo ASC, s.stockUnitCode ASC")
    List<StockUnitMaster> findActiveUnitsByGroupPoid(@Param("groupPoid") Long groupPoid);

    @Query("SELECT s FROM StockUnitMaster s WHERE UPPER(s.stockUnitCode) LIKE UPPER(:codePattern) " +
           "AND (s.deleted IS NULL OR s.deleted <> 'Y') " +
           "ORDER BY s.stockUnitCode ASC")
    List<StockUnitMaster> findByStockUnitCodeContains(@Param("codePattern") String codePattern);
}
