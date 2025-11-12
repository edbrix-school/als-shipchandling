package com.alsharif.shipchandling.requestforquotation.repository;

import com.alsharif.shipchandling.requestforquotation.entity.ApRequestForQtnHdr;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApRequestForQtnHdrRepository extends JpaRepository<ApRequestForQtnHdr, Long>,
        JpaSpecificationExecutor<ApRequestForQtnHdr> {

    boolean existsByTransactionPoid(Long transactionPoid);

    Optional<ApRequestForQtnHdr> findByTransactionPoid(Long transactionPoid);

    Optional<ApRequestForQtnHdr> findByTransactionPoidAndGroupPoidAndCompanyPoid(
            Long transactionPoid, Long groupPoid, Long companyPoid);

    List<ApRequestForQtnHdr> findByGroupPoidAndCompanyPoidAndDeletedNotOrDeletedIsNull(
            Long groupPoid, Long companyPoid, String deleted);

    List<ApRequestForQtnHdr> findByGroupPoidAndCompanyPoidAndStatusAndDeletedNotOrDeletedIsNull(
            Long groupPoid, Long companyPoid, String status, String deleted);
}
