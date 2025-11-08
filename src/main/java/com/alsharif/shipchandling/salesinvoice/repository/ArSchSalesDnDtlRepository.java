package com.alsharif.shipchandling.salesinvoice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.alsharif.shipchandling.salesinvoice.entity.ArSchSalesDnDtl;
import com.alsharif.shipchandling.salesinvoice.entity.ArSchSalesDnDtlId;

import java.util.List;

@Repository
public interface ArSchSalesDnDtlRepository extends JpaRepository<ArSchSalesDnDtl, ArSchSalesDnDtlId> {
    List<ArSchSalesDnDtl> findByTransactionPoid(Long transactionPoid);
    void deleteByTransactionPoid(Long transactionPoid);
    
    @Query("SELECT MAX(d.detRowId) FROM ArSchSalesDnDtl d WHERE d.transactionPoid = :transactionPoid")
    Long findMaxDetRowIdByTransactionPoid(@Param("transactionPoid") Long transactionPoid);
}
