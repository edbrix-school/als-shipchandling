package com.alsharif.shipchandling.deliverynote.repository;

import com.alsharif.shipchandling.deliverynote.entity.SalesDeliveryNoteHdr;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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

        Page<SalesDeliveryNoteHdr> findByCompanyPoidAndDeletedNotOrDeletedIsNull(
                        Long companyPoid, String deleted, Pageable pageable);

        // Custom method signature - implementation in SalesDeliveryNoteHdrRepositoryImpl
        // This method returns results with joined customer name
        // Note: This method is implemented in SalesDeliveryNoteHdrRepositoryImpl
        // The interface just declares it for Spring Data JPA to find the implementation

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

        /**
         * Get Delivery Note with all LOV details in a single query using JOINs
         * Returns Object[] with delivery note data and all related LOV details
         */
        @Query(value = "SELECT " +
                "dn.TRANSACTION_POID as transactionPoid, dn.DOC_REF as docRef, dn.TRANSACTION_DATE as transactionDate, " +
                "dn.COMPANY_POID as companyPoid, dn.CUSTOMER_POID as customerPoid, " +
                "dn.CURRENCY_CODE as currencyCode, dn.CURRENCY_RATE as currencyRate, " +
                "dn.DELIVERY_STATUS as deliveryStatus, dn.SALESMAN_POID as salesmanPoid, " +
                "dn.PAYMENT_MODE as paymentMode, dn.DELIVERY_TERMS as deliveryTerms, " +
                "dn.LINE_POID as linePoid, dn.VESSEL_POID as vesselPoid, dn.VESSEL_NAME as vesselName, " +
                "dn.VOYAGE_REF as voyageRef, dn.PORT_POID as portPoid, dn.PORT_DESCRIPTION as portDescription, " +
                "dn.QTN_REF_NO as qtnRefNo, dn.VESSEL_AGENT as vesselAgent, " +
                "dn.DELIVERY_TO_ADDRESS as deliveryToAddress, dn.DESCRIPTION_PRINT_YN as descriptionPrintYn, " +
                "dn.PARTY_ADDRESS_DETAILS as partyAddressDetails, dn.PRINT_DIVISION_POID as printDivisionPoid, " +
                "dn.PARTY_TYPE as partyType, dn.PRINCIPAL_POID as principalPoid, " +
                "dn.TOTAL_DISCOUNT as totalDiscount, dn.TOTAL_AMOUNT as totalAmount, " +
                "dn.REMARKS as remarks, dn.DELETED as deleted, " +
                "dn.CREATED_BY as createdBy, dn.CREATED_DATE as createdDate, " +
                "dn.LASTMODIFIED_BY as lastmodifiedBy, dn.LASTMODIFIED_DATE as lastmodifiedDate, " +
                // Customer Details
                "cust.CUSTOMER_POID as custPoid, cust.CUSTOMER_CODE as custCode, cust.CUSTOMER_NAME as custName, " +
                // Salesman Details
                "sm.SALESMAN_POID as smPoid, sm.SALESMAN_CODE as smCode, sm.SALESMAN_NAME as smName, " +
                // Line Details
                "lm.LINE_POID as lmPoid, lm.LINE_CODE as lmCode, lm.LINE_NAME as lmName, " +
                // Port Details
                "pm.PORT_POID as pmPoid, pm.PORT_CODE as pmCode, pm.PORT_NAME as pmName, " +
                // Vessel Details
                "vm.VESSEL_POID as vmPoid, vm.VESSEL_CODE as vmCode, vm.VESSEL_NAME as vmName, " +
                // Print Division Details (from GLOBAL_COMPANY_MASTER_DIV_DTL)
                "div.DIV_POID as divPoid, div.REMARKS as divCode, div.REMARKS as divDescription, " +
                // Principal Details
                "pr.PRINCIPAL_POID as prPoid, pr.PRINCIPAL_CODE as prCode, pr.PRINCIPAL_NAME as prName " +
                "FROM SALES_DELIVERY_NOTE_HDR dn " +
                "LEFT JOIN SALES_CUSTOMER_MASTER cust ON dn.CUSTOMER_POID = cust.CUSTOMER_POID " +
                "LEFT JOIN SALES_SALESMAN_MASTER sm ON dn.SALESMAN_POID = sm.SALESMAN_POID " +
                "LEFT JOIN SHIP_LINE_MASTER lm ON dn.LINE_POID = lm.LINE_POID " +
                "LEFT JOIN SHIP_PORT_MASTER pm ON dn.PORT_POID = pm.PORT_POID " +
                "LEFT JOIN SHIP_VESSEL_MASTER vm ON dn.VESSEL_POID = vm.VESSEL_POID " +
                "LEFT JOIN GLOBAL_COMPANY_MASTER_DIV_DTL div ON dn.PRINT_DIVISION_POID = div.DIV_POID AND dn.COMPANY_POID = div.COMPANY_POID " +
                "LEFT JOIN SHIP_PRINCIPAL_MASTER pr ON dn.PRINCIPAL_POID = pr.PRINCIPAL_POID " +
                "WHERE dn.TRANSACTION_POID = :transactionPoid AND dn.COMPANY_POID = :companyPoid", nativeQuery = true)
        List<Object[]> findDeliveryNoteWithDetails(@Param("transactionPoid") Long transactionPoid, @Param("companyPoid") Long companyPoid);

}
