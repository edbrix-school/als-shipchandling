package com.alsharif.shipchandling.requestforquotation.repository;

import com.alsharif.shipchandling.requestforquotation.entity.CurrencyRateUploadTemp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CurrencyRateUploadTempRepository extends JpaRepository<CurrencyRateUploadTemp, String> {
}
