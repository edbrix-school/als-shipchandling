package com.alsharif.shipchandling.requestforquotation.repository;


import com.alsharif.shipchandling.requestforquotation.entity.ApRequestForQtnItemDtl;
import com.alsharif.shipchandling.requestforquotation.entity.ApRequestForQtnItemDtlId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApRequestForQtnItemDtlRepository extends JpaRepository<ApRequestForQtnItemDtl, ApRequestForQtnItemDtlId> {
    List<ApRequestForQtnItemDtl> findByTransactionPoid(Long transactionPoid);
    void deleteByTransactionPoid(Long transactionPoid);
    
    @Query("SELECT MAX(d.detRowId) FROM ApRequestForQtnItemDtl d WHERE d.transactionPoid = :transactionPoid")
    Long findMaxDetRowIdByTransactionPoid(@Param("transactionPoid") Long transactionPoid);


}