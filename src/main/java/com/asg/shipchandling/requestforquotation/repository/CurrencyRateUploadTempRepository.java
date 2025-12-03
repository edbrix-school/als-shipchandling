package com.asg.shipchandling.requestforquotation.repository;

import com.asg.shipchandling.requestforquotation.entity.CurrencyRateUploadTemp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface CurrencyRateUploadTempRepository extends JpaRepository<CurrencyRateUploadTemp, String> {

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END " +
           "FROM CurrencyRateUploadTemp c " +
           "WHERE UPPER(c.currencyCode) = UPPER(:currencyCode)")
    boolean existsByCurrencyCodeIgnoreCase(@Param("currencyCode") String currencyCode);

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END " +
           "FROM CurrencyRateUploadTemp c " +
           "WHERE UPPER(c.currencyCode) = UPPER(:currencyCode) " +
           "AND c.groupPoid = :groupPoid " +
           "AND c.companyPoid = :companyPoid")
    boolean existsByCurrencyCodeAndContext(@Param("currencyCode") String currencyCode,
                                           @Param("groupPoid") BigDecimal groupPoid,
                                           @Param("companyPoid") BigDecimal companyPoid);
}
