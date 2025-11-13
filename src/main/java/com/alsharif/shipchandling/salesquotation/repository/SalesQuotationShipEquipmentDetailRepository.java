package com.alsharif.shipchandling.salesquotation.repository;

import com.alsharif.shipchandling.salesquotation.entity.SalesQuotationShipEquipmentDetail;
import com.alsharif.shipchandling.salesquotation.entity.SalesQuotationShipEquipmentDetailId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface SalesQuotationShipEquipmentDetailRepository extends JpaRepository<SalesQuotationShipEquipmentDetail, SalesQuotationShipEquipmentDetailId> {
    
    @Query("select e from SalesQuotationShipEquipmentDetail e where e.id.transactionPoid = :transactionPoid order by e.id.detailRowId")
    List<SalesQuotationShipEquipmentDetail> findByTransactionPoid(@Param("transactionPoid") BigDecimal transactionPoid);
    
    @Query("select max(e.id.detailRowId) from SalesQuotationShipEquipmentDetail e where e.id.transactionPoid = :transactionPoid")
    Optional<BigDecimal> findMaxDetailRowIdByTransactionPoid(@Param("transactionPoid") BigDecimal transactionPoid);
}

