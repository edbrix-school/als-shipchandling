package com.asg.shipchandling.salesquotation.spec;

import com.asg.shipchandling.salesquotation.entity.SalesQuotationShipHeader;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public final class SalesQuotationShipSpecifications {

    private SalesQuotationShipSpecifications() {
    }

    public static Specification<SalesQuotationShipHeader> notDeleted() {
        return (root, query, cb) -> cb.or(
                cb.isNull(root.get("deleted")),
                cb.notEqual(cb.upper(root.get("deleted")), "Y")
        );
    }

    public static Specification<SalesQuotationShipHeader> companyIs(BigDecimal companyPoid) {
        return companyPoid == null ? null : (root, query, cb) -> cb.equal(root.get("companyPoid"), companyPoid);
    }

    public static Specification<SalesQuotationShipHeader> customerIs(BigDecimal customerPoid) {
        return customerPoid == null ? null : (root, query, cb) -> cb.equal(root.get("customerPoid"), customerPoid);
    }

    public static Specification<SalesQuotationShipHeader> salesmanIs(BigDecimal salesmanPoid) {
        return salesmanPoid == null ? null : (root, query, cb) -> cb.equal(root.get("salesmanPoid"), salesmanPoid);
    }

    public static Specification<SalesQuotationShipHeader> lineIs(BigDecimal linePoid) {
        return linePoid == null ? null : (root, query, cb) -> cb.equal(root.get("linePoid"), linePoid);
    }

    public static Specification<SalesQuotationShipHeader> statusIs(String status) {
        return (status == null || status.isBlank()) ? null : (root, query, cb) ->
                cb.equal(cb.upper(root.get("quotationStatus")), status.trim().toUpperCase());
    }

    public static Specification<SalesQuotationShipHeader> docRefLike(String docRef) {
        return (docRef == null || docRef.isBlank()) ? null : (root, query, cb) ->
                cb.like(cb.upper(root.get("docRef")), "%" + docRef.trim().toUpperCase() + "%");
    }

    public static Specification<SalesQuotationShipHeader> transactionDateFrom(LocalDate fromDate) {
        return fromDate == null ? null : (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("transactionDate"), fromDate);
    }

    public static Specification<SalesQuotationShipHeader> transactionDateTo(LocalDate toDate) {
        return toDate == null ? null : (root, query, cb) -> cb.lessThanOrEqualTo(root.get("transactionDate"), toDate);
    }
    
    public static Specification<SalesQuotationShipHeader> validityFromDate(LocalDate validityFromDate) {
        return validityFromDate == null ? null : (root, query, cb) -> 
                cb.greaterThanOrEqualTo(root.get("validityFromDate"), validityFromDate);
    }
    
    public static Specification<SalesQuotationShipHeader> validityToDate(LocalDate validityToDate) {
        return validityToDate == null ? null : (root, query, cb) -> 
                cb.lessThanOrEqualTo(root.get("validityToDate"), validityToDate);
    }
    
    public static Specification<SalesQuotationShipHeader> quotationTypeIs(String quotationType) {
        return (quotationType == null || quotationType.isBlank()) ? null : (root, query, cb) ->
                cb.equal(cb.upper(root.get("quotationType")), quotationType.trim().toUpperCase());
    }
    
    /**
     * Search across multiple fields: DocRef, CustomerName, Description
     * Case-insensitive search
     */
    public static Specification<SalesQuotationShipHeader> searchText(String searchText) {
        if (searchText == null || searchText.isBlank()) {
            return null;
        }
        String searchPattern = "%" + searchText.trim().toUpperCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.upper(root.get("docRef")), searchPattern),
                cb.like(cb.upper(root.get("customerName")), searchPattern),
                cb.like(cb.upper(root.get("description")), searchPattern)
        );
    }
    
    /**
     * Filter by list of line POIDs (for line access control)
     */
    public static Specification<SalesQuotationShipHeader> lineIn(List<BigDecimal> linePoids) {
        return (linePoids == null || linePoids.isEmpty()) ? null : 
                (root, query, cb) -> root.get("linePoid").in(linePoids);
    }
}
