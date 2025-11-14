package com.alsharif.shipchandling.salesinvoice.repository;


import com.alsharif.shipchandling.salesinvoice.entity.SalesInvoiceDtl;
import com.alsharif.shipchandling.salesinvoice.entity.SalesInvoiceDtlId;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SalesInvoiceDtlRepository extends JpaRepository<SalesInvoiceDtl, SalesInvoiceDtlId> {
    List<SalesInvoiceDtl> findByTransactionPoid(Long transactionPoid);
    void deleteByTransactionPoid(Long transactionPoid);
    
    @Query("SELECT MAX(d.detRowId) FROM SalesInvoiceDtl d WHERE d.transactionPoid = :transactionPoid")
    Long findMaxDetRowIdByTransactionPoid(@Param("transactionPoid") Long transactionPoid);
}
