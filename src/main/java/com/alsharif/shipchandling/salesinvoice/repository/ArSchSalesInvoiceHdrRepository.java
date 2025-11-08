package com.alsharif.shipchandling.salesinvoice.repository;

import com.alsharif.shipchandling.salesinvoice.entity.ArSchSalesInvoiceHdr;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArSchSalesInvoiceHdrRepository extends JpaRepository<ArSchSalesInvoiceHdr, Long>,
        JpaSpecificationExecutor<ArSchSalesInvoiceHdr> {

    boolean existsByTransactionPoid(Long transactionPoid);

    Optional<ArSchSalesInvoiceHdr> findByTransactionPoid(Long transactionPoid);

    Optional<ArSchSalesInvoiceHdr> findByTransactionPoidAndGroupPoidAndCompanyPoid(
            Long transactionPoid, Long groupPoid, Long companyPoid);

    boolean existsByDocRefIgnoreCaseAndGroupPoidAndCompanyPoid(String docRef, Long groupPoid, Long companyPoid);

    boolean existsByDocRefIgnoreCaseAndGroupPoidAndCompanyPoidAndTransactionPoidNot(
            String docRef, Long groupPoid, Long companyPoid, Long transactionPoid);

    List<ArSchSalesInvoiceHdr> findByGroupPoidAndCompanyPoidAndDeletedNotOrDeletedIsNull(
            Long groupPoid, Long companyPoid, String deleted);

    List<ArSchSalesInvoiceHdr> findByGroupPoidAndCompanyPoidAndInvStatusAndDeletedNotOrDeletedIsNull(
            Long groupPoid, Long companyPoid, String invStatus, String deleted);

    List<ArSchSalesInvoiceHdr> findByGroupPoidAndCompanyPoidAndVerifiedAndDeletedNotOrDeletedIsNull(
            Long groupPoid, Long companyPoid, String verified, String deleted);

    List<ArSchSalesInvoiceHdr> findByGroupPoidAndCompanyPoidAndCustomerPoidAndDeletedNotOrDeletedIsNull(
            Long groupPoid, Long companyPoid, Long customerPoid, String deleted);

    List<ArSchSalesInvoiceHdr> findByGroupPoidAndCompanyPoidAndPrincipalPoidAndDeletedNotOrDeletedIsNull(
            Long groupPoid, Long companyPoid, Long principalPoid, String deleted);

    List<ArSchSalesInvoiceHdr> findByGroupPoidAndCompanyPoidAndQtnPoidAndDeletedNotOrDeletedIsNull(
            Long groupPoid, Long companyPoid, Long qtnPoid, String deleted);
}
