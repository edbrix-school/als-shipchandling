package com.asg.shipchandling.salesquotationsch.repository;

import com.asg.shipchandling.salesquotationsch.dto.SalesQuotationSchItemDtlId;
import com.asg.shipchandling.salesquotationsch.entity.SalesQuotationSchItemDtl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SalesQuotationSchItemDtlRepository extends JpaRepository<SalesQuotationSchItemDtl, SalesQuotationSchItemDtlId> {

        List<SalesQuotationSchItemDtl> findByTransactionPoid(Long transactionPoid);

        @Modifying
        @Query("DELETE FROM SalesQuotationSchItemDtl d WHERE d.transactionPoid = :transactionPoid")
        void deleteByTransactionPoid(@Param("transactionPoid") Long transactionPoid);

        @Query("SELECT COALESCE(MAX(d.detRowId), 0) FROM SalesQuotationSchItemDtl d WHERE d.transactionPoid = :transactionPoid")
        Long getMaxDetRowIdByTransactionPoid(@Param("transactionPoid") Long transactionPoid);

        @Query("SELECT d FROM SalesQuotationSchItemDtl d WHERE d.transactionPoid = :transactionPoid " +
                        "ORDER BY d.detRowId ASC")
        List<SalesQuotationSchItemDtl> findByTransactionPoidOrderByDetRowId(@Param("transactionPoid") Long transactionPoid);
}

