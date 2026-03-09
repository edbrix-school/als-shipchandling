package com.asg.shipchandling.salesquotationsch.spec;

import com.asg.shipchandling.salesquotationsch.entity.SalesQuotationSchHdr;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;


public final class SalesQuotationSchSpecifications {

    private SalesQuotationSchSpecifications() {
    }

    public static Specification<SalesQuotationSchHdr> notDeleted() {
        return (root, query, cb) -> cb.or(
                cb.isNull(root.get("deleted")),
                cb.notEqual(cb.upper(root.get("deleted")), "Y")
        );
    }

    public static Specification<SalesQuotationSchHdr> companyIs(Long companyPoid) {
        return companyPoid == null ? null : (root, query, cb) -> cb.equal(root.get("companyPoid"), companyPoid);
    }

    public static Specification<SalesQuotationSchHdr> customerIs(Long customerPoid) {
        return customerPoid == null ? null : (root, query, cb) -> cb.equal(root.get("customerPoid"), customerPoid);
    }

    public static Specification<SalesQuotationSchHdr> salesmanIs(Long salesmanPoid) {
        return salesmanPoid == null ? null : (root, query, cb) -> cb.equal(root.get("salesmanPoid"), salesmanPoid);
    }

    public static Specification<SalesQuotationSchHdr> lineIs(Long linePoid) {
        return linePoid == null ? null : (root, query, cb) -> cb.equal(root.get("linePoid"), linePoid);
    }

    public static Specification<SalesQuotationSchHdr> statusIs(String status) {
        return (status == null || status.isBlank()) ? null : (root, query, cb) ->
                cb.equal(cb.upper(root.get("quotationStatus")), status.trim().toUpperCase());
    }

    public static Specification<SalesQuotationSchHdr> docRefLike(String docRef) {
        return (docRef == null || docRef.isBlank()) ? null : (root, query, cb) ->
                cb.like(cb.upper(root.get("docRef")), "%" + docRef.trim().toUpperCase() + "%");
    }

    public static Specification<SalesQuotationSchHdr> transactionDateFrom(LocalDateTime fromDate) {
        return fromDate == null ? null : (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("transactionDate"), fromDate);
    }

    public static Specification<SalesQuotationSchHdr> transactionDateTo(LocalDateTime toDate) {
        return toDate == null ? null : (root, query, cb) -> cb.lessThanOrEqualTo(root.get("transactionDate"), toDate);
    }
    
    public static Specification<SalesQuotationSchHdr> validityFromDate(LocalDateTime validityFromDate) {
        return validityFromDate == null ? null : (root, query, cb) -> 
                cb.greaterThanOrEqualTo(root.get("validityFromDate"), validityFromDate);
    }
    
    public static Specification<SalesQuotationSchHdr> validityToDate(LocalDateTime validityToDate) {
        return validityToDate == null ? null : (root, query, cb) -> 
                cb.lessThanOrEqualTo(root.get("validityToDate"), validityToDate);
    }
    
    /**
     * Search across multiple fields: DocRef, CustomerRef, Details
     * Case-insensitive search
     */
    public static Specification<SalesQuotationSchHdr> searchText(String searchText) {
        if (searchText == null || searchText.isBlank()) {
            return null;
        }
        String searchPattern = "%" + searchText.trim().toUpperCase() + "%";
        return (root, query, cb) -> {
            // Handle null fields in search
            return cb.or(
                    cb.like(cb.upper(cb.coalesce(root.get("docRef"), "")), searchPattern),
                    cb.like(cb.upper(cb.coalesce(root.get("customerRef"), "")), searchPattern),
                    cb.like(cb.upper(cb.coalesce(root.get("details"), "")), searchPattern)
            );
        };
    }
    
}

