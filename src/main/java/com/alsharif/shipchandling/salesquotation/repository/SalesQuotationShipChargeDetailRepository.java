package com.alsharif.shipchandling.salesquotation.repository;

import com.alsharif.shipchandling.salesquotation.entity.SalesQuotationShipChargeDetail;
import com.alsharif.shipchandling.salesquotation.entity.SalesQuotationShipChargeDetailId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface SalesQuotationShipChargeDetailRepository extends JpaRepository<SalesQuotationShipChargeDetail, SalesQuotationShipChargeDetailId> {
    
    @Query("select c from SalesQuotationShipChargeDetail c where c.id.transactionPoid = :transactionPoid order by c.id.detailRowId")
    List<SalesQuotationShipChargeDetail> findByTransactionPoid(@Param("transactionPoid") BigDecimal transactionPoid);
    
    @Query("select max(c.id.detailRowId) from SalesQuotationShipChargeDetail c where c.id.transactionPoid = :transactionPoid")
    Optional<BigDecimal> findMaxDetailRowIdByTransactionPoid(@Param("transactionPoid") BigDecimal transactionPoid);
}

