package com.alsharif.shipchandling.salesinvoice.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.alsharif.shipchandling.salesinvoice.entity.ArSchSalesInvCostbkdDtl;
import com.alsharif.shipchandling.salesinvoice.entity.ArSchSalesInvCostbkdDtlId;

import java.util.List;

@Repository
public interface ArSchSalesInvCostbkdDtlRepository extends JpaRepository<ArSchSalesInvCostbkdDtl, ArSchSalesInvCostbkdDtlId> {
    List<ArSchSalesInvCostbkdDtl> findByTransactionPoid(Long transactionPoid);
}
