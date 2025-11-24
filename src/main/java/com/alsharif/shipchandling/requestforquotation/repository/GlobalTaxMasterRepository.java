package com.alsharif.shipchandling.requestforquotation.repository;

import com.alsharif.shipchandling.requestforquotation.entity.GlobalTaxMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface GlobalTaxMasterRepository extends JpaRepository<GlobalTaxMaster, Long> {

    @Query("SELECT g FROM GlobalTaxMaster g WHERE g.taxPoid = :taxPoid AND g.active = 'Y'")
    Optional<GlobalTaxMaster> findActiveByTaxPoid(Long taxPoid);
}

