package com.alsharif.shipchandling.salesquotation.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class SalesQuotationShipFilter {

    private BigDecimal companyPoid;
    private BigDecimal customerPoid;
    private BigDecimal salesmanPoid;
    private BigDecimal linePoid;
    private List<BigDecimal> linePoids; // For line access control
    private String quotationStatus;
    private String quotationType;
    private String docRef;
    private String search; // Search across DocRef, CustomerName, Description
    private LocalDate fromDate;
    private LocalDate toDate;
    private LocalDate validityFromDate;
    private LocalDate validityToDate;
    
    // Pagination
    private Integer page = 0;
    private Integer size = 20;
    private String sortBy = "transactionDate";
    private String sortOrder = "DESC";

    public BigDecimal getCompanyPoid() {
        return companyPoid;
    }

    public void setCompanyPoid(BigDecimal companyPoid) {
        this.companyPoid = companyPoid;
    }

    public BigDecimal getCustomerPoid() {
        return customerPoid;
    }

    public void setCustomerPoid(BigDecimal customerPoid) {
        this.customerPoid = customerPoid;
    }

    public BigDecimal getSalesmanPoid() {
        return salesmanPoid;
    }

    public void setSalesmanPoid(BigDecimal salesmanPoid) {
        this.salesmanPoid = salesmanPoid;
    }

    public BigDecimal getLinePoid() {
        return linePoid;
    }

    public void setLinePoid(BigDecimal linePoid) {
        this.linePoid = linePoid;
    }

    public List<BigDecimal> getLinePoids() {
        return linePoids;
    }

    public void setLinePoids(List<BigDecimal> linePoids) {
        this.linePoids = linePoids;
    }

    public String getQuotationStatus() {
        return quotationStatus;
    }

    public void setQuotationStatus(String quotationStatus) {
        this.quotationStatus = quotationStatus;
    }

    public String getQuotationType() {
        return quotationType;
    }

    public void setQuotationType(String quotationType) {
        this.quotationType = quotationType;
    }

    public String getDocRef() {
        return docRef;
    }

    public void setDocRef(String docRef) {
        this.docRef = docRef;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public LocalDate getFromDate() {
        return fromDate;
    }

    public void setFromDate(LocalDate fromDate) {
        this.fromDate = fromDate;
    }

    public LocalDate getToDate() {
        return toDate;
    }

    public void setToDate(LocalDate toDate) {
        this.toDate = toDate;
    }

    public LocalDate getValidityFromDate() {
        return validityFromDate;
    }

    public void setValidityFromDate(LocalDate validityFromDate) {
        this.validityFromDate = validityFromDate;
    }

    public LocalDate getValidityToDate() {
        return validityToDate;
    }

    public void setValidityToDate(LocalDate validityToDate) {
        this.validityToDate = validityToDate;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }
}
