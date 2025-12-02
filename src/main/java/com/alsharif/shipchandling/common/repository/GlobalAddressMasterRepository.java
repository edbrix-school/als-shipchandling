package com.alsharif.shipchandling.common.repository;

import com.alsharif.shipchandling.common.entity.GlobalAddressMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GlobalAddressMasterRepository extends JpaRepository<GlobalAddressMaster, Long> {
}

