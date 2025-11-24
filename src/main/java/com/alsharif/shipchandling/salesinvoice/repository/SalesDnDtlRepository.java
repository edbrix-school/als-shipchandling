package com.alsharif.shipchandling.salesinvoice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.alsharif.shipchandling.salesinvoice.entity.SalesDnDtl;
import com.alsharif.shipchandling.salesinvoice.entity.SalesDnDtlId;

import java.util.List;

@Repository
public interface SalesDnDtlRepository extends JpaRepository<SalesDnDtl, SalesDnDtlId> {
    List<SalesDnDtl> findByTransactionPoid(Long transactionPoid);
    void deleteByTransactionPoid(Long transactionPoid);
    
    @Query("SELECT MAX(d.detRowId) FROM SalesDnDtl d WHERE d.transactionPoid = :transactionPoid")
    Long findMaxDetRowIdByTransactionPoid(@Param("transactionPoid") Long transactionPoid);
}
