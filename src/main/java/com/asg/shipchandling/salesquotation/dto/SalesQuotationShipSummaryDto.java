package com.asg.shipchandling.salesquotation.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class SalesQuotationShipSummaryDto {

    private BigDecimal transactionPoid;
    private LocalDate transactionDate;
    private String docRef;
    private BigDecimal companyPoid;
    private BigDecimal customerPoid;
    private String customerName;
    private String salesmanName;
    private String quotationStatus;
    private LocalDate validityToDate;
    private BigDecimal totalSellingAmountLocal;
    private BigDecimal totalBuyingAmountLocal;
    private BigDecimal totalTax;
    private String currencyCode;

    public BigDecimal getTransactionPoid() {
        return transactionPoid;
    }

    public void setTransactionPoid(BigDecimal transactionPoid) {
        this.transactionPoid = transactionPoid;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDate transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getDocRef() {
        return docRef;
    }

    public void setDocRef(String docRef) {
        this.docRef = docRef;
    }

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

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getSalesmanName() {
        return salesmanName;
    }

    public void setSalesmanName(String salesmanName) {
        this.salesmanName = salesmanName;
    }

    public String getQuotationStatus() {
        return quotationStatus;
    }

    public void setQuotationStatus(String quotationStatus) {
        this.quotationStatus = quotationStatus;
    }

    public LocalDate getValidityToDate() {
        return validityToDate;
    }

    public void setValidityToDate(LocalDate validityToDate) {
        this.validityToDate = validityToDate;
    }

    public BigDecimal getTotalSellingAmountLocal() {
        return totalSellingAmountLocal;
    }

    public void setTotalSellingAmountLocal(BigDecimal totalSellingAmountLocal) {
        this.totalSellingAmountLocal = totalSellingAmountLocal;
    }

    public BigDecimal getTotalBuyingAmountLocal() {
        return totalBuyingAmountLocal;
    }

    public void setTotalBuyingAmountLocal(BigDecimal totalBuyingAmountLocal) {
        this.totalBuyingAmountLocal = totalBuyingAmountLocal;
    }

    public BigDecimal getTotalTax() {
        return totalTax;
    }

    public void setTotalTax(BigDecimal totalTax) {
        this.totalTax = totalTax;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }
}
