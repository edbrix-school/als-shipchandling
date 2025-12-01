package com.alsharif.shipchandling.salesquotationsch.repository;

import com.alsharif.shipchandling.salesquotationsch.entity.SalesQuotationSchHdr;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SalesQuotationSchHdrRepository extends JpaRepository<SalesQuotationSchHdr, Long>,
                JpaSpecificationExecutor<SalesQuotationSchHdr> {

        boolean existsByTransactionPoid(Long transactionPoid);

        Optional<SalesQuotationSchHdr> findByTransactionPoid(Long transactionPoid);

        Optional<SalesQuotationSchHdr> findByTransactionPoidAndCompanyPoid(
                        Long transactionPoid, Long companyPoid);

        List<SalesQuotationSchHdr> findByCompanyPoidAndDeletedNotOrDeletedIsNull(
                        Long companyPoid, String deleted);

        @Query("SELECT h FROM SalesQuotationSchHdr h WHERE h.companyPoid = :companyPoid " +
                        "AND (h.deleted IS NULL OR h.deleted <> 'Y') " +
                        "ORDER BY h.transactionPoid DESC")
        List<SalesQuotationSchHdr> findAllActiveByCompanyPoid(@Param("companyPoid") Long companyPoid);

        @Query("SELECT h FROM SalesQuotationSchHdr h WHERE h.companyPoid = :companyPoid " +
                        "AND h.customerPoid = :customerPoid " +
                        "AND (h.deleted IS NULL OR h.deleted <> 'Y')")
        List<SalesQuotationSchHdr> findByCompanyPoidAndCustomerPoid(
                        @Param("companyPoid") Long companyPoid,
                        @Param("customerPoid") Long customerPoid);

        @Query("SELECT h FROM SalesQuotationSchHdr h WHERE h.companyPoid = :companyPoid " +
                        "AND h.docRef = :docRef " +
                        "AND (h.deleted IS NULL OR h.deleted <> 'Y')")
        Optional<SalesQuotationSchHdr> findByCompanyPoidAndDocRef(
                        @Param("companyPoid") Long companyPoid,
                        @Param("docRef") String docRef);

        @Query("SELECT COUNT(h) FROM SalesQuotationSchHdr h WHERE (:companyPoid IS NULL OR h.companyPoid = :companyPoid) "
                        +
                        "AND h.docRef = :docRef " +
                        "AND (:excludeTransactionPoid IS NULL OR h.transactionPoid <> :excludeTransactionPoid) " +
                        "AND (h.deleted IS NULL OR h.deleted <> 'Y')")
        Long countByCompanyPoidAndDocRefExcluding(
                        @Param("companyPoid") Long companyPoid,
                        @Param("docRef") String docRef,
                        @Param("excludeTransactionPoid") Long excludeTransactionPoid);

        Page<SalesQuotationSchHdr> findByCompanyPoidAndDeletedNotOrDeletedIsNull(
                        Long companyPoid, String deleted, Pageable pageable);

        /**
         * Get Sales Quotation SCH with all LOV details in a single query using JOINs
         * Returns Object[] with quotation data and all related LOV details
         */
        @Query(value = "SELECT " +
                        "qtn.TRANSACTION_POID as transactionPoid, qtn.DOC_REF as docRef, qtn.TRANSACTION_DATE as transactionDate, "
                        +
                        "qtn.COMPANY_POID as companyPoid, qtn.CUSTOMER_POID as customerPoid, qtn.ADDRESS_POID as addressPoid, "
                        +
                        "qtn.CURRENCY_CODE as currencyCode, qtn.CURRENCY_RATE as currencyRate, " +
                        "qtn.QUOTATION_STATUS as quotationStatus, qtn.SALESMAN_POID as salesmanPoid, " +
                        "qtn.VALIDITY_FROM_DATE as validityFromDate, qtn.VALIDITY_TO_DATE as validityToDate, " +
                        "qtn.PAYMENT_MODE as paymentMode, qtn.DELIVERY_TERMS as deliveryTerms, " +
                        "qtn.LINE_POID as linePoid, qtn.VESSEL_POID as vesselPoid, qtn.VESSEL_NAME as vesselName, " +
                        "qtn.VOYAGE_REF as voyageRef, qtn.PORT_POID as portPoid, qtn.PORT_DESCRIPTION as portDescription, "
                        +
                        "qtn.REMARKS as remarks, qtn.TOTAL_DISCOUNT as totalDiscount, qtn.TOTAL_AMOUNT as totalAmount, "
                        +
                        "qtn.ACTION_STATUS as actionStatus, qtn.ACTION_DUE_DATE as actionDueDate, " +
                        "qtn.ENQUIRY_REF_NUMBER as enquiryRefNumber, qtn.LOST_REASON as lostReason, " +
                        "qtn.BUSINESS_PROMOTION_VALUE as businessPromotionValue, qtn.PERCENTAGE as percentage, " +
                        "qtn.DETAILS as details, qtn.EXPEACTED_DELIVERY_DATE as expectedDeliveryDate, " +
                        "qtn.VESSEL_AGENT as vesselAgent, qtn.QUOTED_RATE as quotedRate, qtn.RFQ_REF_NO as rfqRefNo, " +
                        "qtn.PERCENTAGE_DISC as percentageDisc, qtn.TOTAL_GP_AMT as totalGpAmt, " +
                        "qtn.TOTAL_GP_PERCENTAGE as totalGpPercentage, qtn.CUSTOMER_REF as customerRef, " +
                        "qtn.DELIVERY_TO_ADDRESS as deliveryToAddress, qtn.SELECT_ALL_DTL as selectAllDtl, " +
                        "qtn.TOT_AMT_PRINT_YN as totAmtPrintYn, qtn.DESCRIPTION_PRINT_YN as descriptionPrintYn, " +
                        "qtn.ADVANCE_DETAIL as advanceDetail, qtn.MTA_INV_POID as mtaInvPoid, " +
                        "qtn.SALES_INV_POID as salesInvPoid, qtn.SALES_INV_DOC_REF as salesInvDocRef, " +
                        "qtn.TOTAL_TAX as totalTax, qtn.PARTY_ADDRESS_DETAILS as partyAddressDetails, " +
                        "qtn.DELETED as deleted, qtn.CREATED_BY as createdBy, qtn.CREATED_DATE as createdDate, " +
                        "qtn.LASTMODIFIED_BY as lastmodifiedBy, qtn.LASTMODIFIED_DATE as lastmodifiedDate, " +
                        // Customer Details
                        "cust.CUSTOMER_POID as custPoid, cust.CUSTOMER_CODE as custCode, cust.CUSTOMER_NAME as custName, "
                        +
                        // Salesman Details
                        "sm.SALESMAN_POID as smPoid, sm.SALESMAN_CODE as smCode, sm.SALESMAN_NAME as smName, " +
                        // Line Details
                        "lm.LINE_POID as lmPoid, lm.LINE_CODE as lmCode, lm.LINE_NAME as lmName, " +
                        // Port Details
                        "pm.PORT_POID as pmPoid, pm.PORT_CODE as pmCode, pm.PORT_NAME as pmName, " +
                        // Vessel Details
                        "vm.VESSEL_POID as vmPoid, vm.VESSEL_CODE as vmCode, vm.VESSEL_NAME as vmName, " +
                        // Print Division Details (from GLOBAL_COMPANY_MASTER_DIV_DTL) - NULL since
                        // field doesn't exist in table
                        "NULL as divPoid, NULL as divCode, NULL as divDescription, " +
                        // Principal Details - NULL since field doesn't exist in table
                        "NULL as prPoid, NULL as prCode, NULL as prName " +
                        "FROM SALES_QUOTATION_HDR qtn " +
                        "LEFT JOIN SALES_CUSTOMER_MASTER cust ON qtn.CUSTOMER_POID = cust.CUSTOMER_POID " +
                        "LEFT JOIN SALES_SALESMAN_MASTER sm ON qtn.SALESMAN_POID = sm.SALESMAN_POID " +
                        "LEFT JOIN SHIP_LINE_MASTER lm ON qtn.LINE_POID = lm.LINE_POID " +
                        "LEFT JOIN SHIP_PORT_MASTER pm ON qtn.PORT_POID = pm.PORT_POID " +
                        "LEFT JOIN SHIP_VESSEL_MASTER vm ON qtn.VESSEL_POID = TO_CHAR(vm.VESSEL_POID) " +
                        "WHERE qtn.TRANSACTION_POID = :transactionPoid AND qtn.COMPANY_POID = :companyPoid", nativeQuery = true)
        List<Object[]> findSalesQuotationSchWithDetails(@Param("transactionPoid") Long transactionPoid,
                        @Param("companyPoid") Long companyPoid);

}
