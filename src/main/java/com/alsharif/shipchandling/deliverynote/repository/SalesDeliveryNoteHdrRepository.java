package com.alsharif.shipchandling.deliverynote.repository;

import com.alsharif.shipchandling.deliverynote.entity.SalesDeliveryNoteHdr;
import com.alsharif.shipchandling.exceptions.CustomException;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public interface SalesDeliveryNoteHdrRepository extends JpaRepository<SalesDeliveryNoteHdr, Long>,
                JpaSpecificationExecutor<SalesDeliveryNoteHdr> {

        boolean existsByTransactionPoid(Long transactionPoid);

        Optional<SalesDeliveryNoteHdr> findByTransactionPoid(Long transactionPoid);

        Optional<SalesDeliveryNoteHdr> findByTransactionPoidAndCompanyPoid(
                        Long transactionPoid, Long companyPoid);

        List<SalesDeliveryNoteHdr> findByCompanyPoidAndDeletedNotOrDeletedIsNull(
                        Long companyPoid, String deleted);

        List<SalesDeliveryNoteHdr> findByCompanyPoidAndDeliveryStatusAndDeletedNotOrDeletedIsNull(
                        Long companyPoid, String deliveryStatus, String deleted);

        List<SalesDeliveryNoteHdr> findByCompanyPoidAndCustomerPoidAndDeletedNotOrDeletedIsNull(
                        Long companyPoid, Long customerPoid, String deleted);

        List<SalesDeliveryNoteHdr> findByCompanyPoidAndTransactionDateBetweenAndDeletedNotOrDeletedIsNull(
                        Long companyPoid, Timestamp fromDate, Timestamp toDate, String deleted);

        List<SalesDeliveryNoteHdr> findByCompanyPoidAndQtnRefNoAndDeletedNotOrDeletedIsNull(
                        Long companyPoid, String qtnRefNo, String deleted);

        boolean existsByDocRefIgnoreCase(String docRef);

        boolean existsByDocRefIgnoreCaseAndTransactionPoidNot(
                        String docRef, Long transactionPoid);

}
