package com.alsharif.shipchandling.salesinvoice.repository;

import com.alsharif.shipchandling.salesinvoice.entity.SalesInvoiceHdr;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public class SalesInvoiceHdrRepositoryImpl {

    @PersistenceContext
    private EntityManager entityManager;

    public Page<Object[]> findAllWithFiltersAndCustomerName(
            Long groupPoid,
            Long companyPoid,
            String invStatus,
            String verified,
            Long customerPoid,
            Long principalPoid,
            String qtnPoid,
            Timestamp fromDate,
            Timestamp toDate,
            String search,
            Pageable pageable) {

        // Build the main query - select all columns explicitly to properly map results
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT inv.TRANSACTION_POID, inv.DOC_REF, inv.TRANSACTION_DATE, inv.GROUP_POID, ");
        queryBuilder.append("inv.COMPANY_POID, inv.PARTY_TYPE, inv.CUSTOMER_POID, inv.PRINCIPAL_POID, ");
        queryBuilder.append("inv.CUSTOMER_ADDR_POID, inv.CURRENCY_CODE, inv.CURRENCY_RATE, inv.INV_AMOUNT, ");
        queryBuilder.append("inv.CREDIT_DAYS, inv.DUE_DATE, inv.QTN_POID, inv.STATUS, inv.INV_STATUS, ");
        queryBuilder.append("inv.DISCOUNT_PERCENT, inv.DISCOUNT_AMT, inv.INV_DISCOUNT, inv.INCENTIVE_PERCENT, ");
        queryBuilder.append("inv.INCENTIVE_AMT, inv.INCENTIVE_TO, inv.INCENTIVE_PERCENT2, inv.INCENTIVE_AMT2, ");
        queryBuilder.append("inv.INCENTIVE_TO2, inv.INCENTIVE_PERCENT3, inv.INCENTIVE_AMT3, inv.INCENTIVE_TO3, ");
        queryBuilder.append("inv.TOTAL_GP_AMT, inv.TOTAL_GP_PERCENT, inv.TOTAL_COST, inv.PAYMENT_MODE, ");
        queryBuilder.append("inv.DATA_LOAD_TYPE, inv.VESSEL_NAME, inv.PORT_NAME, inv.DESCRIPTION_PRINT_YN, ");
        queryBuilder.append("inv.DELIVERY_TO_ADDRESS, inv.DETAILS, inv.REMARKS, inv.LPO_DETAILS, inv.LPO_NUMBER, ");
        queryBuilder.append("inv.CONTRACT_REF_NUMBER, inv.COST_REF_NUMBER, inv.FDA_REF, inv.PRINT_DIVISION_POID, ");
        queryBuilder.append("inv.DN_POID, inv.VERIFIED, inv.PJ_LOAD_STATUS, inv.POST_WITH_SPL_RIGHTS, ");
        queryBuilder.append("inv.AUTHORIZED_ID, inv.DELETED, inv.CREATED_BY, inv.CREATED_DATE, ");
        queryBuilder.append("inv.LASTMODIFIED_BY, inv.LASTMODIFIED_DATE, ");
        queryBuilder.append("CASE WHEN inv.CUSTOMER_POID IS NOT NULL THEN scm.CUSTOMER_NAME ELSE NULL END AS CUSTOMER_NAME ");
        queryBuilder.append("FROM AR_SCH_SALES_INVOICE_HDR inv ");
        queryBuilder.append("LEFT JOIN SALES_CUSTOMER_MASTER scm ON inv.CUSTOMER_POID = scm.CUSTOMER_POID ");
        queryBuilder.append("WHERE inv.GROUP_POID = :groupPoid ");
        queryBuilder.append("AND inv.COMPANY_POID = :companyPoid ");
        queryBuilder.append("AND (inv.DELETED IS NULL OR inv.DELETED != 'Y') ");
        queryBuilder.append("AND (:invStatus IS NULL OR :invStatus = '' OR inv.INV_STATUS = :invStatus) ");
        queryBuilder.append("AND (:verified IS NULL OR :verified = '' OR inv.VERIFIED = :verified) ");
        queryBuilder.append("AND (:customerPoid IS NULL OR :customerPoid = 0 OR inv.CUSTOMER_POID = :customerPoid) ");
        queryBuilder.append("AND (:principalPoid IS NULL OR :principalPoid = 0 OR inv.PRINCIPAL_POID = :principalPoid) ");
        queryBuilder.append("AND (:qtnPoid IS NULL OR :qtnPoid = 0 OR inv.QTN_POID = :qtnPoid) ");
        queryBuilder.append("AND (:fromDate IS NULL OR inv.TRANSACTION_DATE >= :fromDate) ");
        queryBuilder.append("AND (:toDate IS NULL OR inv.TRANSACTION_DATE <= :toDate) ");
        queryBuilder.append("AND (:search IS NULL OR LOWER(inv.DOC_REF) LIKE '%' || LOWER(:search) || '%' OR ");
        queryBuilder.append("LOWER(inv.VESSEL_NAME) LIKE '%' || LOWER(:search) || '%' OR ");
        queryBuilder.append("LOWER(inv.PORT_NAME) LIKE '%' || LOWER(:search) || '%') ");
        queryBuilder.append("ORDER BY inv.TRANSACTION_DATE DESC, inv.DOC_REF ASC");

        // Build count query
        StringBuilder countQueryBuilder = new StringBuilder();
        countQueryBuilder.append("SELECT COUNT(*) FROM AR_SCH_SALES_INVOICE_HDR inv ");
        countQueryBuilder.append("LEFT JOIN SALES_CUSTOMER_MASTER scm ON inv.CUSTOMER_POID = scm.CUSTOMER_POID ");
        countQueryBuilder.append("WHERE inv.GROUP_POID = :groupPoid ");
        countQueryBuilder.append("AND inv.COMPANY_POID = :companyPoid ");
        countQueryBuilder.append("AND (inv.DELETED IS NULL OR inv.DELETED != 'Y') ");
        countQueryBuilder.append("AND (:invStatus IS NULL OR :invStatus = '' OR inv.INV_STATUS = :invStatus) ");
        countQueryBuilder.append("AND (:verified IS NULL OR :verified = '' OR inv.VERIFIED = :verified) ");
        countQueryBuilder.append("AND (:customerPoid IS NULL OR :customerPoid = 0 OR inv.CUSTOMER_POID = :customerPoid) ");
        countQueryBuilder.append("AND (:principalPoid IS NULL OR :principalPoid = 0 OR inv.PRINCIPAL_POID = :principalPoid) ");
        countQueryBuilder.append("AND (:qtnPoid IS NULL OR :qtnPoid = 0 OR inv.QTN_POID = :qtnPoid) ");
        countQueryBuilder.append("AND (:fromDate IS NULL OR inv.TRANSACTION_DATE >= :fromDate) ");
        countQueryBuilder.append("AND (:toDate IS NULL OR inv.TRANSACTION_DATE <= :toDate) ");
        countQueryBuilder.append("AND (:search IS NULL OR LOWER(inv.DOC_REF) LIKE '%' || LOWER(:search) || '%' OR ");
        countQueryBuilder.append("LOWER(inv.VESSEL_NAME) LIKE '%' || LOWER(:search) || '%' OR ");
        countQueryBuilder.append("LOWER(inv.PORT_NAME) LIKE '%' || LOWER(:search) || '%')");

        // Execute count query
        Query countQuery = entityManager.createNativeQuery(countQueryBuilder.toString());
        setQueryParameters(countQuery, groupPoid, companyPoid, invStatus, verified, customerPoid, 
                          principalPoid, qtnPoid, fromDate, toDate, search);
        Long totalCount = ((Number) countQuery.getSingleResult()).longValue();

        // Execute main query with pagination - returns Object[] with all columns
        Query mainQuery = entityManager.createNativeQuery(queryBuilder.toString());
        setQueryParameters(mainQuery, groupPoid, companyPoid, invStatus, verified, customerPoid, 
                          principalPoid, qtnPoid, fromDate, toDate, search);
        mainQuery.setFirstResult((int) pageable.getOffset());
        mainQuery.setMaxResults(pageable.getPageSize());

        @SuppressWarnings("unchecked")
        List<Object[]> results = mainQuery.getResultList();

        // Map Object[] to [SalesInvoiceHdr, customerName]
        // Object[] contains all entity columns + customerName at the end (57 columns total: 0-55 entity, 56 customerName)
        List<Object[]> mappedResults = results.stream()
                .map(row -> {
                    // Create SalesInvoiceHdr entity from row data
                    SalesInvoiceHdr entity = mapRowToEntity(row);
                    // Detach the entity to prevent Hibernate from trying to load relationships
                    entityManager.detach(entity);
                    // customerName is the last element (index 56, after all 56 entity columns)
                    String customerName = toStringSafe(row[56]);
                    return new Object[]{entity, customerName};
                })
                .toList();

        return new PageImpl<>(mappedResults, pageable, totalCount);
    }

    private SalesInvoiceHdr mapRowToEntity(Object[] row) {
        SalesInvoiceHdr entity = new SalesInvoiceHdr();
        entity.setTransactionPoid(((Number) row[0]).longValue());
        entity.setDocRef(toStringSafe(row[1]));
        entity.setTransactionDate((java.sql.Timestamp) row[2]);
        entity.setGroupPoid(((Number) row[3]).longValue());
        entity.setCompanyPoid(((Number) row[4]).longValue());
        entity.setPartyType(toStringSafe(row[5]));
        entity.setCustomerPoid(row[6] != null ? ((Number) row[6]).longValue() : null);
        entity.setPrincipalPoid(row[7] != null ? ((Number) row[7]).longValue() : null);
        entity.setCustomerAddrPoid(row[8] != null ? ((Number) row[8]).longValue() : null);
        entity.setCurrencyCode(toStringSafe(row[9]));
        entity.setCurrencyRate(row[10] != null ? ((Number) row[10]).longValue() : null);
        entity.setInvAmount(row[11] != null ? ((Number) row[11]).longValue() : null);
        entity.setCreditDays(row[12] != null ? ((Number) row[12]).longValue() : null);
        entity.setDueDate(row[13] != null ? (java.sql.Timestamp) row[13] : null);
        entity.setQtnPoid(toStringSafe(row[14]));
        entity.setStatus(toStringSafe(row[15]));
        entity.setInvStatus(toStringSafe(row[16]));
        entity.setDiscountPercent(row[17] != null ? ((Number) row[17]).longValue() : null);
        entity.setDiscountAmt(row[18] != null ? ((Number) row[18]).longValue() : null);
        entity.setInvDiscount(row[19] != null ? ((Number) row[19]).longValue() : null);
        entity.setIncentivePercent(row[20] != null ? ((Number) row[20]).longValue() : null);
        entity.setIncentiveAmt(row[21] != null ? ((Number) row[21]).longValue() : null);
        entity.setIncentiveTo(toStringSafe(row[22]));
        entity.setIncentivePercent2(row[23] != null ? ((Number) row[23]).longValue() : null);
        entity.setIncentiveAmt2(row[24] != null ? ((Number) row[24]).longValue() : null);
        entity.setIncentiveTo2(toStringSafe(row[25]));
        entity.setIncentivePercent3(row[26] != null ? ((Number) row[26]).longValue() : null);
        entity.setIncentiveAmt3(row[27] != null ? ((Number) row[27]).longValue() : null);
        entity.setIncentiveTo3(toStringSafe(row[28]));
        entity.setTotalGpAmt(row[29] != null ? ((Number) row[29]).longValue() : null);
        entity.setTotalGpPercent(row[30] != null ? ((Number) row[30]).longValue() : null);
        entity.setTotalCost(row[31] != null ? ((Number) row[31]).longValue() : null);
        entity.setPaymentMode(toStringSafe(row[32]));
        entity.setDataLoadType(toStringSafe(row[33]));
        entity.setVesselName(toStringSafe(row[34]));
        entity.setPortName(toStringSafe(row[35]));
        entity.setDescriptionPrintYn(toStringSafe(row[36]));
        entity.setDeliveryToAddress(toStringSafe(row[37]));
        entity.setDetails(toStringSafe(row[38]));
        entity.setRemarks(toStringSafe(row[39]));
        entity.setLpoDetails(toStringSafe(row[40]));
        entity.setLpoNumber(toStringSafe(row[41]));
        entity.setContractRefNumber(toStringSafe(row[42]));
        entity.setCostRefNumber(toStringSafe(row[43]));
        entity.setFdaRef(toStringSafe(row[44]));
        entity.setPrintDivisionPoid(row[45] != null ? ((Number) row[45]).longValue() : null);
        entity.setDnPoid(toStringSafe(row[46]));
        entity.setVerified(toStringSafe(row[47]));
        entity.setPjLoadStatus(toStringSafe(row[48]));
        entity.setPostWithSplRights(toStringSafe(row[49]));
        entity.setAuthorizedId(toStringSafe(row[50]));
        entity.setDeleted(toStringSafe(row[51]));
        entity.setCreatedBy(toStringSafe(row[52]));
        entity.setCreatedDate(row[53] != null ? (java.sql.Timestamp) row[53] : null);
        entity.setLastmodifiedBy(toStringSafe(row[54]));
        entity.setLastmodifiedDate(row[55] != null ? (java.sql.Timestamp) row[55] : null);
        // Note: row[56] is CUSTOMER_NAME, which is extracted separately
        return entity;
    }

    /**
     * Safely converts Object to String, handling Character types from Oracle CHAR columns
     */
    private String toStringSafe(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String) {
            return (String) value;
        }
        if (value instanceof Character) {
            return String.valueOf((Character) value);
        }
        return value.toString();
    }

    private void setQueryParameters(Query query, Long groupPoid, Long companyPoid, String invStatus,
                                    String verified, Long customerPoid, Long principalPoid, String qtnPoid,
                                    Timestamp fromDate, Timestamp toDate, String search) {
        query.setParameter("groupPoid", groupPoid);
        query.setParameter("companyPoid", companyPoid);
        query.setParameter("invStatus", invStatus);
        query.setParameter("verified", verified);
        query.setParameter("customerPoid", customerPoid);
        query.setParameter("principalPoid", principalPoid);
        query.setParameter("qtnPoid", qtnPoid);
        query.setParameter("fromDate", fromDate);
        query.setParameter("toDate", toDate);
        query.setParameter("search", search);
    }
}

