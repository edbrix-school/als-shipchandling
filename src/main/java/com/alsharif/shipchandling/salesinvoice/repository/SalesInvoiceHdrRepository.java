package com.alsharif.shipchandling.salesinvoice.repository;

import com.alsharif.shipchandling.salesinvoice.entity.SalesInvoiceHdr;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SalesInvoiceHdrRepository extends JpaRepository<SalesInvoiceHdr, Long>,
        JpaSpecificationExecutor<SalesInvoiceHdr> {

    boolean existsByTransactionPoid(Long transactionPoid);

    Optional<SalesInvoiceHdr> findByTransactionPoid(Long transactionPoid);

    Optional<SalesInvoiceHdr> findByTransactionPoidAndGroupPoidAndCompanyPoid(
            Long transactionPoid, Long groupPoid, Long companyPoid);

    boolean existsByDocRefIgnoreCaseAndGroupPoidAndCompanyPoid(String docRef, Long groupPoid, Long companyPoid);

    boolean existsByDocRefIgnoreCaseAndGroupPoidAndCompanyPoidAndTransactionPoidNot(
            String docRef, Long groupPoid, Long companyPoid, Long transactionPoid);

    List<SalesInvoiceHdr> findByGroupPoidAndCompanyPoidAndDeletedNotOrDeletedIsNull(
            Long groupPoid, Long companyPoid, String deleted);

    List<SalesInvoiceHdr> findByGroupPoidAndCompanyPoidAndInvStatusAndDeletedNotOrDeletedIsNull(
            Long groupPoid, Long companyPoid, String invStatus, String deleted);

    List<SalesInvoiceHdr> findByGroupPoidAndCompanyPoidAndVerifiedAndDeletedNotOrDeletedIsNull(
            Long groupPoid, Long companyPoid, String verified, String deleted);

    List<SalesInvoiceHdr> findByGroupPoidAndCompanyPoidAndCustomerPoidAndDeletedNotOrDeletedIsNull(
            Long groupPoid, Long companyPoid, Long customerPoid, String deleted);

    List<SalesInvoiceHdr> findByGroupPoidAndCompanyPoidAndPrincipalPoidAndDeletedNotOrDeletedIsNull(
            Long groupPoid, Long companyPoid, Long principalPoid, String deleted);

    List<SalesInvoiceHdr> findByGroupPoidAndCompanyPoidAndQtnPoidAndDeletedNotOrDeletedIsNull(
            Long groupPoid, Long companyPoid, Long qtnPoid, String deleted);
}
