package com.alsharif.shipchandling.salesinvoice.repository;


import com.alsharif.shipchandling.salesinvoice.entity.ArSchSalesInvoiceDtl;
import com.alsharif.shipchandling.salesinvoice.entity.ArSchSalesInvoiceDtlId;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArSchSalesInvoiceDtlRepository extends JpaRepository<ArSchSalesInvoiceDtl, ArSchSalesInvoiceDtlId> {
    List<ArSchSalesInvoiceDtl> findByTransactionPoid(Long transactionPoid);
    void deleteByTransactionPoid(Long transactionPoid);
    
    @Query("SELECT MAX(d.detRowId) FROM ArSchSalesInvoiceDtl d WHERE d.transactionPoid = :transactionPoid")
    Long findMaxDetRowIdByTransactionPoid(@Param("transactionPoid") Long transactionPoid);
}
