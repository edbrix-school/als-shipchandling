package com.alsharif.shipchandling.deliverynote.repository;

import com.alsharif.shipchandling.deliverynote.entity.SalesDeliveryNoteHdr;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public interface SalesDeliveryNoteHdrRepository extends JpaRepository<SalesDeliveryNoteHdr, Long>,
        JpaSpecificationExecutor<SalesDeliveryNoteHdr> {

    boolean existsByTransactionPoid(Long transactionPoid);

    Optional<SalesDeliveryNoteHdr> findByTransactionPoid(Long transactionPoid);

    Optional<SalesDeliveryNoteHdr> findByTransactionPoidAndGroupPoidAndCompanyPoid(
            Long transactionPoid, Long groupPoid, Long companyPoid);

    List<SalesDeliveryNoteHdr> findByGroupPoidAndCompanyPoidAndDeletedNotOrDeletedIsNull(
            Long groupPoid, Long companyPoid, String deleted);

    List<SalesDeliveryNoteHdr> findByGroupPoidAndCompanyPoidAndDeliveryStatusAndDeletedNotOrDeletedIsNull(
            Long groupPoid, Long companyPoid, String deliveryStatus, String deleted);

    List<SalesDeliveryNoteHdr> findByGroupPoidAndCompanyPoidAndCustomerPoidAndDeletedNotOrDeletedIsNull(
            Long groupPoid, Long companyPoid, Long customerPoid, String deleted);

    List<SalesDeliveryNoteHdr> findByGroupPoidAndCompanyPoidAndTransactionDateBetweenAndDeletedNotOrDeletedIsNull(
            Long groupPoid, Long companyPoid, Timestamp fromDate, Timestamp toDate, String deleted);

    List<SalesDeliveryNoteHdr> findByGroupPoidAndCompanyPoidAndQtnRefNoAndDeletedNotOrDeletedIsNull(
            Long groupPoid, Long companyPoid, String qtnRefNo, String deleted);

    boolean existsByDocRefIgnoreCaseAndGroupPoid(String docRef, Long groupPoid);

    boolean existsByDocRefIgnoreCaseAndGroupPoidAndTransactionPoidNot(
            String docRef, Long groupPoid, Long transactionPoid);
}
