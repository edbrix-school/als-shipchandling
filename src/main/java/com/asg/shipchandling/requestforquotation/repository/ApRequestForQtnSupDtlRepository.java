package com.asg.shipchandling.requestforquotation.repository;

import com.asg.shipchandling.requestforquotation.entity.ApRequestForQtnSupDtl;
import com.asg.shipchandling.requestforquotation.entity.ApRequestForQtnSupDtlId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApRequestForQtnSupDtlRepository extends JpaRepository<ApRequestForQtnSupDtl, ApRequestForQtnSupDtlId> {
    List<ApRequestForQtnSupDtl> findByTransactionPoid(Long transactionPoid);
    void deleteByTransactionPoid(Long transactionPoid);
    
    @Query("SELECT MAX(d.detRowId) FROM ApRequestForQtnSupDtl d WHERE d.transactionPoid = :transactionPoid")
    Long findMaxDetRowIdByTransactionPoid(@Param("transactionPoid") Long transactionPoid);

    // Count how many supplier quotations are linked to an RFQ
    @Query("SELECT COUNT(s) FROM ApRequestForQtnSupDtl s WHERE s.transactionPoid = :transactionPoid")
    int countByTransactionPoid(Long transactionPoid);
}