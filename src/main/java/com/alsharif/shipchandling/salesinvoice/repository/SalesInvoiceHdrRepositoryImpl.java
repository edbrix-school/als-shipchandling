package com.alsharif.shipchandling.salesinvoice.repository;

import com.alsharif.shipchandling.salesinvoice.dto.FilterDto;
import com.alsharif.shipchandling.salesinvoice.dto.FilterRequestDto;
import com.alsharif.shipchandling.salesinvoice.entity.SalesInvoiceHdr;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class SalesInvoiceHdrRepositoryImpl {

    @PersistenceContext
    private EntityManager entityManager;

    public Page<Object[]> findAllForList(
            Long companyPoid,
            FilterRequestDto filterRequest,
            Pageable pageable) {
        
        // Build the main query - select only required fields for list
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT inv.TRANSACTION_POID, ");
        queryBuilder.append("inv.TRANSACTION_DATE, ");
        queryBuilder.append("inv.DOC_REF, ");
        queryBuilder.append("inv.QTN_POID, ");
        queryBuilder.append("CASE WHEN inv.PARTY_TYPE = 'CUSTOMER' THEN scm.CUSTOMER_NAME ");
        queryBuilder.append("     WHEN inv.PARTY_TYPE = 'PRINCIPAL' THEN pr.PRINCIPAL_NAME ");
        queryBuilder.append("     ELSE NULL END AS PARTY_NAME, ");
        queryBuilder.append("inv.VESSEL_NAME, ");
        queryBuilder.append("inv.DELETED, ");
        queryBuilder.append("inv.CREATED_BY, ");
        queryBuilder.append("inv.LASTMODIFIED_BY, ");
        queryBuilder.append("inv.CREATED_DATE, ");
        queryBuilder.append("inv.LASTMODIFIED_DATE ");
        queryBuilder.append("FROM AR_SCH_SALES_INVOICE_HDR inv ");
        queryBuilder.append("LEFT JOIN SALES_CUSTOMER_MASTER scm ON inv.CUSTOMER_POID = scm.CUSTOMER_POID ");
        queryBuilder.append("LEFT JOIN SHIP_PRINCIPAL_MASTER pr ON inv.PRINCIPAL_POID = pr.PRINCIPAL_POID ");
        queryBuilder.append("WHERE inv.COMPANY_POID = :companyPoid ");

        // Handle isDeleted filter
        String isDeleted = filterRequest != null && filterRequest.isDeleted() != null ? filterRequest.isDeleted() : "N";
        if ("Y".equalsIgnoreCase(isDeleted)) {
            queryBuilder.append("AND inv.DELETED = 'Y' ");
        } else {
            queryBuilder.append("AND (inv.DELETED IS NULL OR inv.DELETED != 'Y') ");
        }

        // Build dynamic filters - collect valid filters first
        List<FilterDto> validFilters = new ArrayList<>();
        if (filterRequest != null && filterRequest.filters() != null && !filterRequest.filters().isEmpty()) {
            for (FilterDto filter : filterRequest.filters()) {
                String searchField = filter.searchField();
                String searchValue = filter.searchValue();
                
                if (searchField != null && searchValue != null && !searchValue.trim().isEmpty()) {
                    validFilters.add(filter);
                }
            }
        }
        
        // Build filter conditions
        List<String> filterConditions = new ArrayList<>();
        String operator = null;
        if (!validFilters.isEmpty()) {
            operator = filterRequest.operator() != null ? filterRequest.operator().toUpperCase() : "OR";
            
            for (int i = 0; i < validFilters.size(); i++) {
                FilterDto filter = validFilters.get(i);
                String condition = buildListFilterCondition(filter.searchField(), i);
                if (condition != null) {
                    filterConditions.add(condition);
                }
            }
            
            // Combine filter conditions
            if (!filterConditions.isEmpty()) {
                boolean isAndOperator = "AND".equals(operator);
                queryBuilder.append("AND (");
                for (int i = 0; i < filterConditions.size(); i++) {
                    if (i > 0) {
                        queryBuilder.append(isAndOperator ? " AND " : " OR ");
                    }
                    queryBuilder.append(filterConditions.get(i));
                }
                queryBuilder.append(") ");
            }
        }

        // Add ORDER BY clause based on Pageable sort
        if (pageable.getSort().isSorted()) {
            queryBuilder.append("ORDER BY ");
            boolean first = true;
            for (org.springframework.data.domain.Sort.Order order : pageable.getSort()) {
                if (!first) {
                    queryBuilder.append(", ");
                }
                String property = order.getProperty();
                // Map entity property names to database column names
                String dbColumn = mapPropertyToDbColumn(property);
                queryBuilder.append(dbColumn).append(" ").append(order.getDirection().name());
                first = false;
            }
        } else {
            // Default sorting
            queryBuilder.append("ORDER BY inv.TRANSACTION_DATE DESC, inv.DOC_REF ASC");
        }

        // Build count query
        StringBuilder countQueryBuilder = new StringBuilder();
        countQueryBuilder.append("SELECT COUNT(*) FROM AR_SCH_SALES_INVOICE_HDR inv ");
        countQueryBuilder.append("LEFT JOIN SALES_CUSTOMER_MASTER scm ON inv.CUSTOMER_POID = scm.CUSTOMER_POID ");
        countQueryBuilder.append("LEFT JOIN SHIP_PRINCIPAL_MASTER pr ON inv.PRINCIPAL_POID = pr.PRINCIPAL_POID ");
        countQueryBuilder.append("WHERE inv.COMPANY_POID = :companyPoid ");
        
        // Handle isDeleted filter in count query
        if ("Y".equalsIgnoreCase(isDeleted)) {
            countQueryBuilder.append("AND inv.DELETED = 'Y' ");
        } else {
            countQueryBuilder.append("AND (inv.DELETED IS NULL OR inv.DELETED != 'Y') ");
        }
        
        // Add same filter conditions to count query
        if (!filterConditions.isEmpty()) {
            if (operator == null) {
                operator = filterRequest != null && filterRequest.operator() != null ? filterRequest.operator().toUpperCase() : "OR";
            }
            boolean isAndOperator = "AND".equals(operator);
            countQueryBuilder.append("AND (");
            for (int i = 0; i < filterConditions.size(); i++) {
                if (i > 0) {
                    countQueryBuilder.append(isAndOperator ? " AND " : " OR ");
                }
                countQueryBuilder.append(filterConditions.get(i));
            }
            countQueryBuilder.append(") ");
        }

        // Execute count query
        Query countQuery = entityManager.createNativeQuery(countQueryBuilder.toString());
        countQuery.setParameter("companyPoid", companyPoid);
        setFilterQueryParameters(countQuery, validFilters);
        Long totalCount = ((Number) countQuery.getSingleResult()).longValue();

        // Execute main query with pagination
        Query mainQuery = entityManager.createNativeQuery(queryBuilder.toString());
        mainQuery.setParameter("companyPoid", companyPoid);
        setFilterQueryParameters(mainQuery, validFilters);
        mainQuery.setFirstResult((int) pageable.getOffset());
        mainQuery.setMaxResults(pageable.getPageSize());

        @SuppressWarnings("unchecked")
        List<Object[]> results = mainQuery.getResultList();

        return new PageImpl<>(results, pageable, totalCount);
    }
    
    private String buildListFilterCondition(String searchField, int paramIndex) {
        String upperField = searchField.toUpperCase();
        String paramName = "filterParam" + paramIndex;
        
        // Map search fields to database columns
        switch (upperField) {
            case "VESSELNAME":
            case "VESSEL_NAME":
                return "LOWER(inv.VESSEL_NAME) LIKE '%' || LOWER(:" + paramName + ") || '%'";
            case "DOCREF":
            case "DOC_REF":
                return "LOWER(inv.DOC_REF) LIKE '%' || LOWER(:" + paramName + ") || '%'";
            case "QTNREF":
            case "QTN_REF":
                return "inv.QTN_POID = :" + paramName;
            case "INVSTATUS":
            case "INV_STATUS":
                return "inv.INV_STATUS = :" + paramName;
            case "STATUS":
                return "inv.STATUS = :" + paramName;
            case "PORTNAME":
            case "PORT_NAME":
                return "LOWER(inv.PORT_NAME) LIKE '%' || LOWER(:" + paramName + ") || '%'";
            case "PARTYNAME":
            case "PARTY_NAME":
                return "(LOWER(scm.CUSTOMER_NAME) LIKE '%' || LOWER(:" + paramName + ") || '%' OR " +
                       "LOWER(pr.PRINCIPAL_NAME) LIKE '%' || LOWER(:" + paramName + ") || '%')";
            case "PARTYTYPE":
            case "PARTY_TYPE":
                return "inv.PARTY_TYPE = :" + paramName;
            default:
                return null;
        }
    }

    public Page<Object[]> findAllWithFiltersAndCustomerName(
            Long groupPoid,
            Long companyPoid,
            FilterRequestDto filterRequest,
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

        // Handle isDeleted filter
        String isDeleted = filterRequest != null && filterRequest.isDeleted() != null ? filterRequest.isDeleted() : "N";
        if ("Y".equalsIgnoreCase(isDeleted)) {
            queryBuilder.append("AND inv.DELETED = 'Y' ");
        } else {
            queryBuilder.append("AND (inv.DELETED IS NULL OR inv.DELETED != 'Y') ");
        }

        // Build dynamic filters - collect valid filters first
        List<FilterDto> validFilters = new ArrayList<>();
        if (filterRequest != null && filterRequest.filters() != null && !filterRequest.filters().isEmpty()) {
            for (FilterDto filter : filterRequest.filters()) {
                String searchField = filter.searchField();
                String searchValue = filter.searchValue();
                
                if (searchField != null && searchValue != null && !searchValue.trim().isEmpty()) {
                    validFilters.add(filter);
                }
            }
        }
        
        // Build filter conditions
        List<String> filterConditions = new ArrayList<>();
        String operator = null;
        if (!validFilters.isEmpty()) {
            operator = filterRequest.operator() != null ? filterRequest.operator().toUpperCase() : "OR";
            
            for (int i = 0; i < validFilters.size(); i++) {
                FilterDto filter = validFilters.get(i);
                String condition = buildFilterCondition(filter.searchField(), i);
                if (condition != null) {
                    filterConditions.add(condition);
                }
            }
            
            // Combine filter conditions
            if (!filterConditions.isEmpty()) {
                boolean isAndOperator = "AND".equals(operator);
                queryBuilder.append("AND (");
                for (int i = 0; i < filterConditions.size(); i++) {
                    if (i > 0) {
                        queryBuilder.append(isAndOperator ? " AND " : " OR ");
                    }
                    queryBuilder.append(filterConditions.get(i));
                }
                queryBuilder.append(") ");
            }
        }

        queryBuilder.append("ORDER BY inv.TRANSACTION_DATE DESC, inv.DOC_REF ASC");

        // Build count query
        StringBuilder countQueryBuilder = new StringBuilder();
        countQueryBuilder.append("SELECT COUNT(*) FROM AR_SCH_SALES_INVOICE_HDR inv ");
        countQueryBuilder.append("LEFT JOIN SALES_CUSTOMER_MASTER scm ON inv.CUSTOMER_POID = scm.CUSTOMER_POID ");
        countQueryBuilder.append("WHERE inv.GROUP_POID = :groupPoid ");
        countQueryBuilder.append("AND inv.COMPANY_POID = :companyPoid ");
        
        // Handle isDeleted filter in count query
        if ("Y".equalsIgnoreCase(isDeleted)) {
            countQueryBuilder.append("AND inv.DELETED = 'Y' ");
        } else {
            countQueryBuilder.append("AND (inv.DELETED IS NULL OR inv.DELETED != 'Y') ");
        }
        
        // Add same filter conditions to count query
        if (!filterConditions.isEmpty()) {
            if (operator == null) {
                operator = filterRequest != null && filterRequest.operator() != null ? filterRequest.operator().toUpperCase() : "OR";
            }
            boolean isAndOperator = "AND".equals(operator);
            countQueryBuilder.append("AND (");
            for (int i = 0; i < filterConditions.size(); i++) {
                if (i > 0) {
                    countQueryBuilder.append(isAndOperator ? " AND " : " OR ");
                }
                countQueryBuilder.append(filterConditions.get(i));
            }
            countQueryBuilder.append(") ");
        }

        // Execute count query
        Query countQuery = entityManager.createNativeQuery(countQueryBuilder.toString());
        countQuery.setParameter("groupPoid", groupPoid);
        countQuery.setParameter("companyPoid", companyPoid);
        setFilterQueryParameters(countQuery, validFilters);
        Long totalCount = ((Number) countQuery.getSingleResult()).longValue();

        // Execute main query with pagination - returns Object[] with all columns
        Query mainQuery = entityManager.createNativeQuery(queryBuilder.toString());
        mainQuery.setParameter("groupPoid", groupPoid);
        mainQuery.setParameter("companyPoid", companyPoid);
        setFilterQueryParameters(mainQuery, validFilters);
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
    
    private String buildFilterCondition(String searchField, int paramIndex) {
        String upperField = searchField.toUpperCase();
        String paramName = "filterParam" + paramIndex;
        
        // Map common field names to database columns
        switch (upperField) {
            case "DOC_REF":
            case "DOCREF":
                return "LOWER(inv.DOC_REF) LIKE '%' || LOWER(:" + paramName + ") || '%'";
            case "INV_STATUS":
            case "INVSTATUS":
                return "inv.INV_STATUS = :" + paramName;
            case "VERIFIED":
                return "inv.VERIFIED = :" + paramName;
            case "CUSTOMER_POID":
            case "CUSTOMERPOID":
                return "inv.CUSTOMER_POID = :" + paramName;
            case "PRINCIPAL_POID":
            case "PRINCIPALPOID":
                return "inv.PRINCIPAL_POID = :" + paramName;
            case "VESSEL_NAME":
            case "VESSELNAME":
                return "LOWER(inv.VESSEL_NAME) LIKE '%' || LOWER(:" + paramName + ") || '%'";
            case "PORT_NAME":
            case "PORTNAME":
                return "LOWER(inv.PORT_NAME) LIKE '%' || LOWER(:" + paramName + ") || '%'";
            case "PARTY_TYPE":
            case "PARTYTYPE":
                return "inv.PARTY_TYPE = :" + paramName;
            case "CURRENCY_CODE":
            case "CURRENCYCODE":
                return "inv.CURRENCY_CODE = :" + paramName;
            case "QTN_POID":
            case "QTNPOID":
                return "inv.QTN_POID = :" + paramName;
            case "GLOBALSEARCH":
                return "(LOWER(inv.DOC_REF) LIKE '%' || LOWER(:" + paramName + ") || '%' OR " +
                       "LOWER(inv.VESSEL_NAME) LIKE '%' || LOWER(:" + paramName + ") || '%' OR " +
                       "LOWER(inv.PORT_NAME) LIKE '%' || LOWER(:" + paramName + ") || '%' OR " +
                       "LOWER(scm.CUSTOMER_NAME) LIKE '%' || LOWER(:" + paramName + ") || '%')";
            default:
                return null;
        }
    }
    
    private String mapPropertyToDbColumn(String property) {
        // Map entity property names to database column names
        switch (property.toLowerCase()) {
            case "transactiondate":
            case "date":
                return "inv.TRANSACTION_DATE";
            case "docref":
                return "inv.DOC_REF";
            case "qtnpoid":
            case "qtnref":
                return "inv.QTN_POID";
            case "customername":
            case "partyname":
                return "PARTY_NAME"; // This is the CASE expression alias
            case "vesselname":
                return "inv.VESSEL_NAME";
            case "createddate":
                return "inv.CREATED_DATE";
            case "lastmodifieddate":
            case "updateddate":
                return "inv.LASTMODIFIED_DATE";
            default:
                return "inv." + property.toUpperCase();
        }
    }
    
    private void setFilterQueryParameters(Query query, List<FilterDto> validFilters) {
        if (validFilters != null && !validFilters.isEmpty()) {
            for (int i = 0; i < validFilters.size(); i++) {
                FilterDto filter = validFilters.get(i);
                String searchField = filter.searchField();
                String searchValue = filter.searchValue();
                String paramName = "filterParam" + i;
                String upperField = searchField.toUpperCase();
                
                // Set parameter based on field type
                if ("CUSTOMER_POID".equals(upperField) || "CUSTOMERPOID".equals(upperField) ||
                    "PRINCIPAL_POID".equals(upperField) || "PRINCIPALPOID".equals(upperField)) {
                    try {
                        query.setParameter(paramName, Long.parseLong(searchValue));
                    } catch (NumberFormatException e) {
                        // Skip invalid number - set to null to avoid query error
                        query.setParameter(paramName, null);
                    }
                } else {
                    query.setParameter(paramName, searchValue);
                }
            }
        }
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

}

