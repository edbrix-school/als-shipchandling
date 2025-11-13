package com.alsharif.shipchandling.salesquotation.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SalesQuotationShipDetailDto {

    private BigDecimal transactionPoid;
    private LocalDate transactionDate;
    private String docRef;
    private BigDecimal groupPoid;
    private BigDecimal companyPoid;
    private BigDecimal customerPoid;
    private BigDecimal addressPoid;
    private BigDecimal linePoid;
    private String customerName;
    private String customerContact;
    private String customerEmail;
    private String quotationStatus;
    private String quotationSubject;
    private String quotationType;
    private String paymentMode;
    private LocalDate validityFromDate;
    private LocalDate validityToDate;
    private String remarks;
    private String shippingMode;
    private String termsOfShipment;
    private BigDecimal loadingPortPoid;
    private BigDecimal dischargePortPoid;
    private String placeOfReceipt;
    private String placeOfDelivery;
    private String commodityType;
    private String description;
    private BigDecimal salesmanPoid;
    private BigDecimal totalSellingAmountLocal;
    private BigDecimal totalBuyingAmountLocal;
    private BigDecimal totalTax;
    private BigDecimal totalTaxAmount;
    private String actionStatus;
    private LocalDate actionDueDate;
    private BigDecimal enquiryRefPoid;
    private String surcharges;
    private String routing;
    private String multiPort;
    private String newCustomer;
    private String createdBy;
    private LocalDateTime createdDate;
    private String lastModifiedBy;
    private LocalDateTime lastModifiedDate;
    private List<SalesQuotationShipItemDto> charges = new ArrayList<>();
    private List<SalesQuotationShipEquipmentDto> equipment = new ArrayList<>();

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

    public BigDecimal getGroupPoid() {
        return groupPoid;
    }

    public void setGroupPoid(BigDecimal groupPoid) {
        this.groupPoid = groupPoid;
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

    public BigDecimal getAddressPoid() {
        return addressPoid;
    }

    public void setAddressPoid(BigDecimal addressPoid) {
        this.addressPoid = addressPoid;
    }

    public BigDecimal getLinePoid() {
        return linePoid;
    }

    public void setLinePoid(BigDecimal linePoid) {
        this.linePoid = linePoid;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerContact() {
        return customerContact;
    }

    public void setCustomerContact(String customerContact) {
        this.customerContact = customerContact;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public String getQuotationStatus() {
        return quotationStatus;
    }

    public void setQuotationStatus(String quotationStatus) {
        this.quotationStatus = quotationStatus;
    }

    public String getQuotationSubject() {
        return quotationSubject;
    }

    public void setQuotationSubject(String quotationSubject) {
        this.quotationSubject = quotationSubject;
    }

    public String getQuotationType() {
        return quotationType;
    }

    public void setQuotationType(String quotationType) {
        this.quotationType = quotationType;
    }

    public String getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(String paymentMode) {
        this.paymentMode = paymentMode;
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

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getShippingMode() {
        return shippingMode;
    }

    public void setShippingMode(String shippingMode) {
        this.shippingMode = shippingMode;
    }

    public String getTermsOfShipment() {
        return termsOfShipment;
    }

    public void setTermsOfShipment(String termsOfShipment) {
        this.termsOfShipment = termsOfShipment;
    }

    public BigDecimal getLoadingPortPoid() {
        return loadingPortPoid;
    }

    public void setLoadingPortPoid(BigDecimal loadingPortPoid) {
        this.loadingPortPoid = loadingPortPoid;
    }

    public BigDecimal getDischargePortPoid() {
        return dischargePortPoid;
    }

    public void setDischargePortPoid(BigDecimal dischargePortPoid) {
        this.dischargePortPoid = dischargePortPoid;
    }

    public String getPlaceOfReceipt() {
        return placeOfReceipt;
    }

    public void setPlaceOfReceipt(String placeOfReceipt) {
        this.placeOfReceipt = placeOfReceipt;
    }

    public String getPlaceOfDelivery() {
        return placeOfDelivery;
    }

    public void setPlaceOfDelivery(String placeOfDelivery) {
        this.placeOfDelivery = placeOfDelivery;
    }

    public String getCommodityType() {
        return commodityType;
    }

    public void setCommodityType(String commodityType) {
        this.commodityType = commodityType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getSalesmanPoid() {
        return salesmanPoid;
    }

    public void setSalesmanPoid(BigDecimal salesmanPoid) {
        this.salesmanPoid = salesmanPoid;
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

    public BigDecimal getTotalTaxAmount() {
        return totalTaxAmount;
    }

    public void setTotalTaxAmount(BigDecimal totalTaxAmount) {
        this.totalTaxAmount = totalTaxAmount;
    }

    public String getActionStatus() {
        return actionStatus;
    }

    public void setActionStatus(String actionStatus) {
        this.actionStatus = actionStatus;
    }

    public LocalDate getActionDueDate() {
        return actionDueDate;
    }

    public void setActionDueDate(LocalDate actionDueDate) {
        this.actionDueDate = actionDueDate;
    }

    public BigDecimal getEnquiryRefPoid() {
        return enquiryRefPoid;
    }

    public void setEnquiryRefPoid(BigDecimal enquiryRefPoid) {
        this.enquiryRefPoid = enquiryRefPoid;
    }

    public String getSurcharges() {
        return surcharges;
    }

    public void setSurcharges(String surcharges) {
        this.surcharges = surcharges;
    }

    public String getRouting() {
        return routing;
    }

    public void setRouting(String routing) {
        this.routing = routing;
    }

    public String getMultiPort() {
        return multiPort;
    }

    public void setMultiPort(String multiPort) {
        this.multiPort = multiPort;
    }

    public String getNewCustomer() {
        return newCustomer;
    }

    public void setNewCustomer(String newCustomer) {
        this.newCustomer = newCustomer;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public LocalDateTime getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(LocalDateTime lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public List<SalesQuotationShipItemDto> getCharges() {
        return charges;
    }

    public void setCharges(List<SalesQuotationShipItemDto> charges) {
        this.charges = charges;
    }

    public List<SalesQuotationShipEquipmentDto> getEquipment() {
        return equipment;
    }

    public void setEquipment(List<SalesQuotationShipEquipmentDto> equipment) {
        this.equipment = equipment;
    }
}
