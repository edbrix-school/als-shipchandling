package com.asg.shipchandling.salesquotationsch.repository;

import com.asg.shipchandling.salesquotationsch.entity.SalesQuotationSchHdr;
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
         * Get Sales Quotation SCH header data only (without LOV details)
         * Returns Object[] with quotation header fields
         */
        @Query(value = "SELECT " +
                        "qtn.TRANSACTION_POID, qtn.DOC_REF, qtn.TRANSACTION_DATE, " +
                        "qtn.COMPANY_POID, qtn.CUSTOMER_POID, qtn.ADDRESS_POID, " +
                        "qtn.CURRENCY_CODE, qtn.CURRENCY_RATE, " +
                        "qtn.QUOTATION_STATUS, qtn.SALESMAN_POID, " +
                        "qtn.VALIDITY_FROM_DATE, qtn.VALIDITY_TO_DATE, " +
                        "qtn.PAYMENT_MODE, qtn.DELIVERY_TERMS, " +
                        "qtn.LINE_POID, qtn.VESSEL_POID, qtn.VESSEL_NAME, " +
                        "qtn.VOYAGE_REF, qtn.PORT_POID, qtn.PORT_DESCRIPTION, " +
                        "qtn.REMARKS, qtn.TOTAL_DISCOUNT, qtn.TOTAL_AMOUNT, " +
                        "qtn.ACTION_STATUS, qtn.ACTION_DUE_DATE, " +
                        "qtn.ENQUIRY_REF_NUMBER, qtn.LOST_REASON, " +
                        "qtn.BUSINESS_PROMOTION_VALUE, qtn.PERCENTAGE, " +
                        "qtn.DETAILS, qtn.EXPEACTED_DELIVERY_DATE, " +
                        "qtn.VESSEL_AGENT, qtn.QUOTED_RATE, qtn.RFQ_REF_NO, " +
                        "qtn.PERCENTAGE_DISC, qtn.TOTAL_GP_AMT, " +
                        "qtn.TOTAL_GP_PERCENTAGE, qtn.CUSTOMER_REF, " +
                        "qtn.DELIVERY_TO_ADDRESS, qtn.SELECT_ALL_DTL, " +
                        "qtn.TOT_AMT_PRINT_YN, qtn.DESCRIPTION_PRINT_YN, " +
                        "qtn.ADVANCE_DETAIL, qtn.MTA_INV_POID, " +
                        "qtn.SALES_INV_POID, qtn.SALES_INV_DOC_REF, " +
                        "qtn.TOTAL_TAX, qtn.PARTY_ADDRESS_DETAILS, " +
                        "qtn.DELETED, qtn.CREATED_BY, qtn.CREATED_DATE, " +
                        "qtn.LASTMODIFIED_BY, qtn.LASTMODIFIED_DATE " +
                        "FROM SALES_QUOTATION_HDR qtn " +
                        "WHERE qtn.TRANSACTION_POID = :transactionPoid AND qtn.COMPANY_POID = :companyPoid", nativeQuery = true)
        List<Object[]> findSalesQuotationSchHeader(@Param("transactionPoid") Long transactionPoid,
                        @Param("companyPoid") Long companyPoid);

        /**
         * Get Customer Details LOV (using ADDRESS_MASTER)
         * Returns Object[] with [ADDRESS_MASTER_POID, ADDRESS_MASTER_POID, ADDRESS_NAME]
         */
        @Query(value = "SELECT am.ADDRESS_MASTER_POID, am.ADDRESS_MASTER_POID, am.ADDRESS_NAME " +
                        "FROM GLOBAL_ADDRESS_MASTER am " +
                        "WHERE am.ADDRESS_MASTER_POID = :addressPoid " +
                        "AND (am.ACTIVE = 'Y' OR am.ACTIVE IS NULL) " +
                        "AND (am.DELETED = 'N' OR am.DELETED IS NULL)", nativeQuery = true)
        List<Object[]> findCustomerDetailsLov(@Param("addressPoid") Long addressPoid);

        /**
         * Get Customer Details LOV by CUSTOMER_POID (using ADDRESS_MASTER)
         * Returns Object[] with [ADDRESS_MASTER_POID, ADDRESS_MASTER_POID, ADDRESS_NAME]
         */
        @Query(value = "SELECT am.ADDRESS_MASTER_POID, am.ADDRESS_MASTER_POID, am.ADDRESS_NAME " +
                        "FROM GLOBAL_ADDRESS_MASTER am " +
                        "WHERE am.ADDRESS_MASTER_POID = :customerPoid " +
                        "AND NVL(am.ACTIVE, 'Y') = 'Y' " +
                        "AND NVL(am.DELETED, 'N') = 'N'", nativeQuery = true)
        List<Object[]> findCustomerDetailsLovByCustomerPoid(@Param("customerPoid") Long customerPoid);

        /**
         * Get Salesman Details LOV
         * Returns Object[] with [SALESMAN_POID, SALESMAN_CODE, SALESMAN_NAME]
         */
        @Query(value = "SELECT sm.SALESMAN_POID, sm.SALESMAN_CODE, sm.SALESMAN_NAME " +
                        "FROM SALES_SALESMAN_MASTER sm " +
                        "WHERE sm.SALESMAN_POID = :salesmanPoid", nativeQuery = true)
        List<Object[]> findSalesmanDetailsLov(@Param("salesmanPoid") Long salesmanPoid);

        /**
         * Get Line Details LOV
         * Returns Object[] with [LINE_POID, LINE_CODE, LINE_NAME]
         */
        @Query(value = "SELECT lm.LINE_POID, lm.LINE_CODE, lm.LINE_NAME " +
                        "FROM SHIP_LINE_MASTER lm " +
                        "WHERE lm.LINE_POID = :linePoid", nativeQuery = true)
        List<Object[]> findLineDetailsLov(@Param("linePoid") Long linePoid);

        /**
         * Get Port Details LOV
         * Returns Object[] with [PORT_POID, PORT_CODE, PORT_NAME]
         */
        @Query(value = "SELECT pm.PORT_POID, pm.PORT_CODE, pm.PORT_NAME " +
                        "FROM SHIP_PORT_MASTER pm " +
                        "WHERE pm.PORT_POID = :portPoid", nativeQuery = true)
        List<Object[]> findPortDetailsLov(@Param("portPoid") Long portPoid);

        /**
         * Get Vessel Details LOV
         * Returns Object[] with [VESSEL_POID, VESSEL_CODE, VESSEL_NAME]
         */
        @Query(value = "SELECT vm.VESSEL_POID, vm.VESSEL_CODE, vm.VESSEL_NAME " +
                        "FROM SHIP_VESSEL_MASTER vm " +
                        "WHERE TO_CHAR(vm.VESSEL_POID) = :vesselPoid", nativeQuery = true)
        List<Object[]> findVesselDetailsLov(@Param("vesselPoid") String vesselPoid);

        /**
         * Get Address Details (contact person, email, mobile)
         * Returns Object[] with [ADDRESS_POID, CONTACT_PERSON, EMAIL1, MOBILE]
         * NOTE: SALES_QUOTATION_HDR.ADDRESS_POID stores ADDRESS_MASTER_POID
         */
        @Query(value = "SELECT address_master_poid, contact_person, email1, mobile " +
                        "FROM ( " +
                        "  SELECT d.address_master_poid, d.contact_person, d.email1, d.mobile, " +
                        "    ROW_NUMBER() OVER ( " +
                        "      PARTITION BY d.address_master_poid " +
                        "      ORDER BY " +
                        "        CASE WHEN UPPER(d.address_type) = 'SALES' THEN 0 ELSE 1 END, " +
                        "        CASE WHEN d.contact_person IS NOT NULL AND TRIM(d.contact_person) IS NOT NULL THEN 0 ELSE 1 END, " +
                        "        CASE WHEN d.email1 IS NOT NULL AND TRIM(d.email1) IS NOT NULL THEN 0 ELSE 1 END, " +
                        "        CASE WHEN d.mobile IS NOT NULL AND TRIM(d.mobile) IS NOT NULL THEN 0 ELSE 1 END, " +
                        "        d.address_poid " +
                        "    ) rn " +
                        "  FROM global_address_details d " +
                        "  WHERE d.address_master_poid = :addressPoid " +
                        ") " +
                        "WHERE rn = 1", nativeQuery = true)
        List<Object[]> findAddressDetails(@Param("addressPoid") Long addressPoid);

        /**
         * Find customer POIDs by customer name pattern
         * Used for filtering quotations by customer name
         */
        @Query(value = "SELECT DISTINCT c.CUSTOMER_POID FROM SALES_CUSTOMER_MASTER c " +
                "WHERE UPPER(c.CUSTOMER_NAME) LIKE :pattern", nativeQuery = true)
        List<Long> findCustomerPoidsByName(@Param("pattern") String pattern);

        /**
         * Get customer name by customer POID
         */
        @Query(value = "SELECT CUSTOMER_NAME FROM SALES_CUSTOMER_MASTER WHERE CUSTOMER_POID = :customerPoid", nativeQuery = true)
        String findCustomerNameByPoid(@Param("customerPoid") Long customerPoid);

}
