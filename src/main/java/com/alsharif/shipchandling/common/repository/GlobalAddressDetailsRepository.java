package com.alsharif.shipchandling.common.repository;

import com.alsharif.shipchandling.common.entity.GlobalAddressDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GlobalAddressDetailsRepository extends JpaRepository<GlobalAddressDetails, Long> {
}

