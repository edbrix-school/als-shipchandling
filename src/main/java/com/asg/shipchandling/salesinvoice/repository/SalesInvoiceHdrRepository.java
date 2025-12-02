package com.asg.shipchandling.salesinvoice.repository;

import com.asg.shipchandling.salesinvoice.entity.SalesInvoiceHdr;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SalesInvoiceHdrRepository extends JpaRepository<SalesInvoiceHdr, Long>,
        JpaSpecificationExecutor<SalesInvoiceHdr> {

    boolean existsByTransactionPoid(Long transactionPoid);

    Optional<SalesInvoiceHdr> findByTransactionPoid(Long transactionPoid);

    Optional<SalesInvoiceHdr> findByTransactionPoidAndCompanyPoid(Long transactionPoid, Long companyPoid);

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
            Long groupPoid, Long companyPoid, String qtnPoid, String deleted);

    // Custom method signature - implementation in SalesInvoiceHdrRepositoryImpl
    // This method returns results with joined customer name
    // Note: This method is implemented in SalesInvoiceHdrRepositoryImpl
    // The interface just declares it for Spring Data JPA to find the implementation
    
    /**
     * Get Sales Invoice with all LOV details in a single query using JOINs
     * Returns Object[] with invoice data and all related LOV details
     */
    @Query(value = "SELECT " +
            "inv.TRANSACTION_POID as transactionPoid, inv.DOC_REF as docRef, inv.TRANSACTION_DATE as transactionDate, " +
            "inv.GROUP_POID as groupPoid, inv.COMPANY_POID as companyPoid, inv.PARTY_TYPE as partyType, " +
            "inv.CUSTOMER_POID as customerPoid, inv.PRINCIPAL_POID as principalPoid, " +
            "inv.CUSTOMER_ADDR_POID as customerAddrPoid, inv.CURRENCY_CODE as currencyCode, " +
            "inv.CURRENCY_RATE as currencyRate, inv.INV_AMOUNT as invAmount, inv.CREDIT_DAYS as creditDays, " +
            "inv.DUE_DATE as dueDate, inv.QTN_POID as qtnPoid, inv.STATUS as status, inv.INV_STATUS as invStatus, " +
            "inv.DISCOUNT_PERCENT as discountPercent, inv.DISCOUNT_AMT as discountAmt, inv.INV_DISCOUNT as invDiscount, " +
            "inv.INCENTIVE_PERCENT as incentivePercent, inv.INCENTIVE_AMT as incentiveAmt, inv.INCENTIVE_TO as incentiveTo, " +
            "inv.INCENTIVE_PERCENT2 as incentivePercent2, inv.INCENTIVE_AMT2 as incentiveAmt2, inv.INCENTIVE_TO2 as incentiveTo2, " +
            "inv.INCENTIVE_PERCENT3 as incentivePercent3, inv.INCENTIVE_AMT3 as incentiveAmt3, inv.INCENTIVE_TO3 as incentiveTo3, " +
            "inv.TOTAL_GP_AMT as totalGpAmt, inv.TOTAL_GP_PERCENT as totalGpPercent, inv.TOTAL_COST as totalCost, " +
            "inv.PAYMENT_MODE as paymentMode, inv.DATA_LOAD_TYPE as dataLoadType, inv.VESSEL_NAME as vesselName, " +
            "inv.PORT_NAME as portName, inv.DESCRIPTION_PRINT_YN as descriptionPrintYn, inv.DELIVERY_TO_ADDRESS as deliveryToAddress, " +
            "inv.DETAILS as details, inv.REMARKS as remarks, inv.LPO_DETAILS as lpoDetails, inv.LPO_NUMBER as lpoNumber, " +
            "inv.CONTRACT_REF_NUMBER as contractRefNumber, inv.COST_REF_NUMBER as costRefNumber, inv.FDA_REF as fdaRef, " +
            "inv.PRINT_DIVISION_POID as printDivisionPoid, inv.DN_POID as dnPoid, inv.VERIFIED as verified, " +
            "inv.PJ_LOAD_STATUS as pjLoadStatus, inv.POST_WITH_SPL_RIGHTS as postWithSplRights, inv.AUTHORIZED_ID as authorizedId, " +
            "inv.DELETED as deleted, inv.CREATED_BY as createdBy, inv.CREATED_DATE as createdDate, " +
            "inv.LASTMODIFIED_BY as lastmodifiedBy, inv.LASTMODIFIED_DATE as lastmodifiedDate, " +
            // Customer Details - from SALES_CUSTOMER_MASTER when partyType is CUSTOMER, 
            // or from SHIP_PRINCIPAL_MASTER when partyType is PRINCIPAL (using customerPoid as PRINCIPAL_POID)
            "CASE WHEN inv.PARTY_TYPE = 'CUSTOMER' THEN cust.CUSTOMER_POID " +
            "     WHEN inv.PARTY_TYPE = 'PRINCIPAL' THEN prFromCust.PRINCIPAL_POID " +
            "     ELSE NULL END as custPoid, " +
            "CASE WHEN inv.PARTY_TYPE = 'CUSTOMER' THEN cust.CUSTOMER_CODE " +
            "     WHEN inv.PARTY_TYPE = 'PRINCIPAL' THEN prFromCust.PRINCIPAL_CODE " +
            "     ELSE NULL END as custCode, " +
            "CASE WHEN inv.PARTY_TYPE = 'CUSTOMER' THEN cust.CUSTOMER_NAME " +
            "     WHEN inv.PARTY_TYPE = 'PRINCIPAL' THEN prFromCust.PRINCIPAL_NAME " +
            "     ELSE NULL END as custName, " +
            // Principal Details (from SHIP_PRINCIPAL_MASTER using principalPoid)
            "pr.PRINCIPAL_POID as prPoid, pr.PRINCIPAL_CODE as prCode, pr.PRINCIPAL_NAME as prName, " +
            // Quotation Details (from SALES_QUOTATION_HDR with joins)
            "qtn.TRANSACTION_POID as qtnDetailPoid, qtn.DOC_REF as qtnDetailCode, " +
            "qtn.VESSEL_NAME || '-' || NVL(gam.ADDRESS_NAME, '') as qtnDetailDescription, " +
            // Print Division Details (from GLOBAL_COMPANY_MASTER_DIV_DTL)
            "div.DIV_POID as divDetailPoid, div.REMARKS as divDetailCode, div.REMARKS as divDetailDescription, " +
            // Delivery Note Details (from SALES_DELIVERY_NOTE_HDR)
            "CASE WHEN inv.DN_POID IS NOT NULL THEN TO_NUMBER(inv.DN_POID) ELSE NULL END as dnDetailPoid, " +
            "dn.DOC_REF as dnDetailCode, " +
            "'VOY:-' || NVL(dn.VOYAGE_REF, '') || ' CUST:' || NVL(get_SALES_CUSTOMER_NAME(dn.CUSTOMER_POID), '') as dnDetailDescription, " +
            // FDA Details (from PDA_FDA_HDR)
            "CASE WHEN inv.FDA_REF IS NOT NULL THEN TO_NUMBER(inv.FDA_REF) ELSE NULL END as fdaDetailPoid, " +
            "fda.DOC_REF as fdaDetailCode, " +
            "NVL(fdaVessel.VESSEL_NAME, '') || ' / ' || NVL(fda.VOYAGE_NO, '') || ' / ' || NVL(fdaPr.PRINCIPAL_NAME, '') as fdaDetailDescription " +
            "FROM AR_SCH_SALES_INVOICE_HDR inv " +
            "LEFT JOIN SALES_CUSTOMER_MASTER cust ON inv.CUSTOMER_POID = cust.CUSTOMER_POID " +
            "LEFT JOIN SHIP_PRINCIPAL_MASTER prFromCust ON inv.CUSTOMER_POID = prFromCust.PRINCIPAL_POID " +
            "LEFT JOIN SHIP_PRINCIPAL_MASTER pr ON inv.PRINCIPAL_POID = pr.PRINCIPAL_POID " +
            "LEFT JOIN SALES_QUOTATION_HDR qtn ON inv.QTN_POID = TO_CHAR(qtn.TRANSACTION_POID) " +
            "LEFT JOIN GLOBAL_ADDRESS_DETAILS gad ON gad.ADDRESS_POID = qtn.CUSTOMER_POID " +
            "LEFT JOIN GLOBAL_ADDRESS_MASTER gam ON gam.ADDRESS_MASTER_POID = gad.ADDRESS_MASTER_POID " +
            "LEFT JOIN GLOBAL_COMPANY_MASTER_DIV_DTL div ON inv.PRINT_DIVISION_POID = div.DIV_POID AND inv.COMPANY_POID = div.COMPANY_POID " +
            "LEFT JOIN SALES_DELIVERY_NOTE_HDR dn ON inv.DN_POID = TO_CHAR(dn.TRANSACTION_POID) " +
            "LEFT JOIN PDA_FDA_HDR fda ON inv.FDA_REF = TO_CHAR(fda.TRANSACTION_POID) " +
            "LEFT JOIN SHIP_VESSEL_MASTER fdaVessel ON fda.VESSEL_POID = fdaVessel.VESSEL_POID " +
            "LEFT JOIN SHIP_PRINCIPAL_MASTER fdaPr ON fda.PRINCIPAL_POID = fdaPr.PRINCIPAL_POID " +
            "WHERE inv.TRANSACTION_POID = :transactionPoid AND inv.COMPANY_POID = :companyPoid", nativeQuery = true)
    List<Object[]> findSalesInvoiceWithDetails(@Param("transactionPoid") Long transactionPoid, @Param("companyPoid") Long companyPoid);
}
