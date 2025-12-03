package com.asg.shipchandling.salesquotation.repository;

import com.asg.shipchandling.salesquotation.entity.SalesQuotationShipHeader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;

public interface SalesQuotationShipHeaderRepository extends JpaRepository<SalesQuotationShipHeader, BigDecimal>,
        JpaSpecificationExecutor<SalesQuotationShipHeader> {

    @Query("select h from SalesQuotationShipHeader h where h.transactionPoid = :transactionPoid and " +
            "(h.deleted is null or upper(h.deleted) <> 'Y')")
    Optional<SalesQuotationShipHeader> findActiveWithDetails(@Param("transactionPoid") BigDecimal transactionPoid);
    
    @Query("select h from SalesQuotationShipHeader h where h.transactionPoid = :transactionPoid and " +
            "h.companyPoid = :companyPoid and " +
            "(h.deleted is null or upper(h.deleted) <> 'Y')")
    Optional<SalesQuotationShipHeader> findActiveWithDetailsByCompany(
            @Param("transactionPoid") BigDecimal transactionPoid,
            @Param("companyPoid") BigDecimal companyPoid);
}
