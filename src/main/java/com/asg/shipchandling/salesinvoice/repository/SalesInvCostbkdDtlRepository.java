package com.asg.shipchandling.salesinvoice.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.asg.shipchandling.salesinvoice.entity.SalesInvCostbkdDtl;
import com.asg.shipchandling.salesinvoice.entity.SalesInvCostbkdDtlId;

import java.util.List;

@Repository
public interface SalesInvCostbkdDtlRepository extends JpaRepository<SalesInvCostbkdDtl, SalesInvCostbkdDtlId> {
    List<SalesInvCostbkdDtl> findByTransactionPoid(Long transactionPoid);
}
