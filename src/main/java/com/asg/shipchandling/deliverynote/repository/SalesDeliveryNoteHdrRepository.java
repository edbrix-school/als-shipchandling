package com.asg.shipchandling.deliverynote.repository;

import com.asg.shipchandling.deliverynote.entity.SalesDeliveryNoteHdr;

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
         * Get Customer LOV details (CUSTOMER_MASTER)
         */
        @Query(value = "SELECT CUSTOMER_POID AS POID, CUSTOMER_CODE AS CODE, CUSTOMER_NAME AS DESCRIPTION " +
                "FROM SALES_CUSTOMER_MASTER " +
                "INNER JOIN GL_MASTER GLMAST ON GLMAST.GL_POID = SALES_CUSTOMER_MASTER.GL_POID " +
                "WHERE CUSTOMER_POID = :poid " +
                "AND NVL(SALES_CUSTOMER_MASTER.DELETED, 'N') = 'N' " +
                "AND NVL(SALES_CUSTOMER_MASTER.ACTIVE, 'Y') = 'Y'", nativeQuery = true)
        List<Object[]> findCustomerLovDetail(@Param("poid") Long poid);

        /**
         * Get Salesman LOV details (SALESMAN)
         */
        @Query(value = "SELECT SALESMAN_POID AS POID, SALESMAN_CODE AS CODE, SALESMAN_NAME AS DESCRIPTION " +
                "FROM SALES_SALESMAN_MASTER " +
                "WHERE SALESMAN_POID = :poid " +
                "AND ACTIVE = 'Y'", nativeQuery = true)
        List<Object[]> findSalesmanLovDetail(@Param("poid") Long poid);

        /**
         * Get Line LOV details (LINE_MASTER)
         */
        @Query(value = "SELECT LINE_POID AS POID, LINE_CODE AS CODE, LINE_NAME AS DESCRIPTION " +
                "FROM SHIP_LINE_MASTER " +
                "WHERE LINE_POID = :poid " +
                "AND ACTIVE = 'Y'", nativeQuery = true)
        List<Object[]> findLineLovDetail(@Param("poid") Long poid);

        /**
         * Get Port LOV details (PORT_MASTER)
         */
        @Query(value = "SELECT PORT_POID AS POID, PORT_CODE AS CODE, PORT_NAME AS DESCRIPTION " +
                "FROM SHIP_PORT_MASTER " +
                "WHERE PORT_POID = :poid " +
                "AND ACTIVE = 'Y'", nativeQuery = true)
        List<Object[]> findPortLovDetail(@Param("poid") Long poid);

        /**
         * Get Vessel LOV details (VESSEL_MASTER)
         */
        @Query(value = "SELECT VESSEL_POID AS POID, VESSEL_CODE AS CODE, VESSEL_NAME || ' ' || VM.IMO_NUMBER AS DESCRIPTION " +
                "FROM SHIP_VESSEL_MASTER VM " +
                "WHERE TO_CHAR(VESSEL_POID) = :poid " +
                "AND NVL(VM.ACTIVE, 'Y') = 'Y'", nativeQuery = true)
        List<Object[]> findVesselLovDetail(@Param("poid") String poid);

        /**
         * Get Print Division LOV details (COMPANY_DIVISION)
         */
        @Query(value = "SELECT DIV_POID AS POID, REMARKS AS CODE, REMARKS AS DESCRIPTION " +
                "FROM GLOBAL_COMPANY_MASTER_DIV_DTL " +
                "WHERE DIV_POID = :poid " +
                "AND COMPANY_POID = :companyPoid", nativeQuery = true)
        List<Object[]> findPrintDivisionLovDetail(@Param("poid") Long poid, @Param("companyPoid") Long companyPoid);

        /**
         * Get Principal LOV details (PRINCIPAL_MASTER_FOR_DN)
         */
        @Query(value = "SELECT PRINCIPAL_POID AS POID, PRINCIPAL_CODE AS CODE, PRINCIPAL_NAME AS DESCRIPTION " +
                "FROM SHIP_PRINCIPAL_MASTER " +
                "WHERE PRINCIPAL_POID = :poid " +
                "AND GL_CODE_POID IS NOT NULL", nativeQuery = true)
        List<Object[]> findPrincipalLovDetail(@Param("poid") Long poid);

}
