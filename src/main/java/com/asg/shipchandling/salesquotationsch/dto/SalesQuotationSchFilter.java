package com.asg.shipchandling.salesquotationsch.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesQuotationSchFilter {
    private Long companyPoid;
    private Long customerPoid;
    private Long salesmanPoid;
    private Long linePoid;
    private String quotationStatus;
    private String docRef;
    private String search; // Search across DocRef, CustomerRef, Details
    private Timestamp fromDate;
    private Timestamp toDate;
    private Timestamp validityFromDate;
    private Timestamp validityToDate;
    
    // Pagination
    private Integer page = 0;
    private Integer size = 20;
    private String sortBy = "transactionDate";
    private String sortOrder = "DESC";
}

