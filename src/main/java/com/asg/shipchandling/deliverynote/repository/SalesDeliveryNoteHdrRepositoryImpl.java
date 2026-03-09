package com.asg.shipchandling.deliverynote.repository;

import com.asg.shipchandling.deliverynote.entity.SalesDeliveryNoteHdr;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class SalesDeliveryNoteHdrRepositoryImpl {

    @PersistenceContext
    private EntityManager entityManager;

    public Page<Object[]> findAllWithFiltersAndCustomerName(
            Long companyPoid,
            String deliveryStatus,
            Long customerPoid,
            Long salesmanPoid,
            String qtnRefNo,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            String search,
            Pageable pageable) {

        // Build the main query - select all columns explicitly to properly map results
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT dn.TRANSACTION_POID, dn.DOC_REF, dn.TRANSACTION_DATE, dn.COMPANY_POID, ");
        queryBuilder.append("dn.CUSTOMER_POID, dn.CURRENCY_CODE, dn.CURRENCY_RATE, dn.DELIVERY_STATUS, ");
        queryBuilder.append("dn.SALESMAN_POID, dn.PAYMENT_MODE, dn.DELIVERY_TERMS, dn.LINE_POID, ");
        queryBuilder.append("dn.VESSEL_POID, dn.VESSEL_NAME, dn.VOYAGE_REF, dn.PORT_POID, ");
        queryBuilder.append("dn.PORT_DESCRIPTION, dn.QTN_REF_NO, dn.VESSEL_AGENT, dn.DELIVERY_TO_ADDRESS, ");
        queryBuilder.append("dn.DESCRIPTION_PRINT_YN, dn.PARTY_ADDRESS_DETAILS, dn.PRINT_DIVISION_POID, ");
        queryBuilder.append("dn.PARTY_TYPE, dn.PRINCIPAL_POID, dn.TOTAL_DISCOUNT, dn.TOTAL_AMOUNT, ");
        queryBuilder.append("dn.REMARKS, dn.DELETED, dn.CREATED_BY, dn.CREATED_DATE, ");
        queryBuilder.append("dn.LASTMODIFIED_BY, dn.LASTMODIFIED_DATE, scm.CUSTOMER_NAME ");
        queryBuilder.append("FROM SALES_DELIVERY_NOTE_HDR dn ");
        queryBuilder.append("INNER JOIN SALES_CUSTOMER_MASTER scm ON dn.CUSTOMER_POID = scm.CUSTOMER_POID ");
        queryBuilder.append("WHERE dn.COMPANY_POID = :companyPoid ");
        queryBuilder.append("AND (dn.DELETED IS NULL OR dn.DELETED != 'Y') ");
        queryBuilder.append("AND (:deliveryStatus IS NULL OR :deliveryStatus = '' OR dn.DELIVERY_STATUS = :deliveryStatus) ");
        queryBuilder.append("AND (:customerPoid IS NULL OR :customerPoid = 0 OR dn.CUSTOMER_POID = :customerPoid) ");
        queryBuilder.append("AND (:salesmanPoid IS NULL OR :salesmanPoid = 0 OR dn.SALESMAN_POID = :salesmanPoid) ");
        queryBuilder.append("AND (:qtnRefNo IS NULL OR dn.QTN_REF_NO = :qtnRefNo) ");
        queryBuilder.append("AND (:fromDate IS NULL OR dn.TRANSACTION_DATE >= :fromDate) ");
        queryBuilder.append("AND (:toDate IS NULL OR dn.TRANSACTION_DATE <= :toDate) ");
        queryBuilder.append("AND (:search IS NULL OR LOWER(dn.DOC_REF) LIKE '%' || LOWER(:search) || '%' OR ");
        queryBuilder.append("LOWER(dn.VESSEL_NAME) LIKE '%' || LOWER(:search) || '%' OR ");
        queryBuilder.append("LOWER(dn.QTN_REF_NO) LIKE '%' || LOWER(:search) || '%') ");
        queryBuilder.append("ORDER BY dn.TRANSACTION_DATE DESC");

        // Build count query
        StringBuilder countQueryBuilder = new StringBuilder();
        countQueryBuilder.append("SELECT COUNT(*) FROM SALES_DELIVERY_NOTE_HDR dn ");
        countQueryBuilder.append("INNER JOIN SALES_CUSTOMER_MASTER scm ON dn.CUSTOMER_POID = scm.CUSTOMER_POID ");
        countQueryBuilder.append("WHERE dn.COMPANY_POID = :companyPoid ");
        countQueryBuilder.append("AND (dn.DELETED IS NULL OR dn.DELETED != 'Y') ");
        countQueryBuilder.append("AND (:deliveryStatus IS NULL OR :deliveryStatus = '' OR dn.DELIVERY_STATUS = :deliveryStatus) ");
        countQueryBuilder.append("AND (:customerPoid IS NULL OR :customerPoid = 0 OR dn.CUSTOMER_POID = :customerPoid) ");
        countQueryBuilder.append("AND (:salesmanPoid IS NULL OR :salesmanPoid = 0 OR dn.SALESMAN_POID = :salesmanPoid) ");
        countQueryBuilder.append("AND (:qtnRefNo IS NULL OR dn.QTN_REF_NO = :qtnRefNo) ");
        countQueryBuilder.append("AND (:fromDate IS NULL OR dn.TRANSACTION_DATE >= :fromDate) ");
        countQueryBuilder.append("AND (:toDate IS NULL OR dn.TRANSACTION_DATE <= :toDate) ");
        countQueryBuilder.append("AND (:search IS NULL OR LOWER(dn.DOC_REF) LIKE '%' || LOWER(:search) || '%' OR ");
        countQueryBuilder.append("LOWER(dn.VESSEL_NAME) LIKE '%' || LOWER(:search) || '%' OR ");
        countQueryBuilder.append("LOWER(dn.QTN_REF_NO) LIKE '%' || LOWER(:search) || '%')");

        // Execute count query
        Query countQuery = entityManager.createNativeQuery(countQueryBuilder.toString());
        setQueryParameters(countQuery, companyPoid, deliveryStatus, customerPoid, salesmanPoid, 
                          qtnRefNo, fromDate, toDate, search);
        Long totalCount = ((Number) countQuery.getSingleResult()).longValue();

        // Execute main query with pagination - returns Object[] with all columns
        Query mainQuery = entityManager.createNativeQuery(queryBuilder.toString());
        setQueryParameters(mainQuery, companyPoid, deliveryStatus, customerPoid, salesmanPoid, 
                          qtnRefNo, fromDate, toDate, search);
        mainQuery.setFirstResult((int) pageable.getOffset());
        mainQuery.setMaxResults(pageable.getPageSize());

        @SuppressWarnings("unchecked")
        List<Object[]> results = mainQuery.getResultList();

        // Map Object[] to [SalesDeliveryNoteHdr, customerName]
        // Object[] contains: [transactionPoid, docRef, transactionDate, companyPoid, customerPoid, 
        // currencyCode, currencyRate, deliveryStatus, salesmanPoid, paymentMode, deliveryTerms, 
        // linePoid, vesselPoid, vesselName, voyageRef, portPoid, portDescription, qtnRefNo, 
        // vesselAgent, deliveryToAddress, descriptionPrintYn, partyAddressDetails, printDivisionPoid,
        // partyType, principalPoid, totalDiscount, totalAmount, remarks, deleted, createdBy, 
        // createdDate, lastmodifiedBy, lastmodifiedDate, customerName]
        List<Object[]> mappedResults = results.stream()
                .map(row -> {
                    // Create SalesDeliveryNoteHdr entity from row data
                    SalesDeliveryNoteHdr entity = mapRowToEntity(row);
                    // customerName is the last element (index 33, after all 33 entity columns)
                    String customerName = toStringSafe(row[33]);
                    return new Object[]{entity, customerName};
                })
                .toList();

        return new PageImpl<>(mappedResults, pageable, totalCount);
    }

    private SalesDeliveryNoteHdr mapRowToEntity(Object[] row) {
        SalesDeliveryNoteHdr entity = new SalesDeliveryNoteHdr();
        entity.setTransactionPoid(((Number) row[0]).longValue());
        entity.setDocRef(toStringSafe(row[1]));
        entity.setTransactionDate(
                row[2] != null ? ((Timestamp) row[2]).toLocalDateTime() : null
        );
        entity.setCompanyPoid(((Number) row[3]).longValue());
        entity.setCustomerPoid(((Number) row[4]).longValue());
        entity.setCurrencyCode(toStringSafe(row[5]));
        entity.setCurrencyRate(row[6] != null ? new BigDecimal(row[6].toString()) : null);
        entity.setDeliveryStatus(toStringSafe(row[7]));
        entity.setSalesmanPoid(row[8] != null ? ((Number) row[8]).longValue() : null);
        entity.setPaymentMode(toStringSafe(row[9]));
        entity.setDeliveryTerms(toStringSafe(row[10]));
        entity.setLinePoid(row[11] != null ? ((Number) row[11]).longValue() : null);
        entity.setVesselPoid(toStringSafe(row[12]));
        entity.setVesselName(toStringSafe(row[13]));
        entity.setVoyageRef(toStringSafe(row[14]));
        entity.setPortPoid(row[15] != null ? ((Number) row[15]).longValue() : null);
        entity.setPortDescription(toStringSafe(row[16]));
        entity.setQtnRefNo(toStringSafe(row[17]));
        entity.setVesselAgent(toStringSafe(row[18]));
        entity.setDeliveryToAddress(toStringSafe(row[19]));
        entity.setDescriptionPrintYn(toStringSafe(row[20]));
        entity.setPartyAddressDetails(toStringSafe(row[21]));
        entity.setPrintDivisionPoid(row[22] != null ? ((Number) row[22]).longValue() : null);
        entity.setPartyType(toStringSafe(row[23]));
        entity.setPrincipalPoid(row[24] != null ? ((Number) row[24]).longValue() : null);
        entity.setTotalDiscount(row[25] != null ? ((Number) row[25]).longValue() : null);
        entity.setTotalAmount(row[26] != null ? ((Number) row[26]).longValue() : null);
        entity.setRemarks(toStringSafe(row[27]));
        entity.setDeleted(toStringSafe(row[28]));
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

    private void setQueryParameters(Query query, Long companyPoid, String deliveryStatus,
                                    Long customerPoid, Long salesmanPoid, String qtnRefNo,
                                    LocalDateTime fromDate, LocalDateTime toDate, String search) {
        query.setParameter("companyPoid", companyPoid);
        query.setParameter("deliveryStatus", deliveryStatus);
        query.setParameter("customerPoid", customerPoid);
        query.setParameter("salesmanPoid", salesmanPoid);
        query.setParameter("qtnRefNo", qtnRefNo);
        query.setParameter("fromDate", fromDate);
        query.setParameter("toDate", toDate);
        query.setParameter("search", search);
    }
}

