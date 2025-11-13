package com.alsharif.shipchandling.salesquotation.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SalesQuotationShipCommand {

    private BigDecimal transactionPoid;
    private LocalDate transactionDate;
    private BigDecimal groupPoid;
    private BigDecimal companyPoid;
    private BigDecimal userPoid;  // User POID for stored procedures
    private String userId;  // User ID string for audit fields (CREATED_BY, LASTMODIFIED_BY)
    private String docRef;
    private BigDecimal customerPoid;
    private BigDecimal addressPoid;
    private BigDecimal linePoid;
    private String quotationStatus;
    private BigDecimal salesmanPoid;
    private LocalDate validityFromDate;
    private LocalDate validityToDate;
    private BigDecimal loadingPortPoid;
    private BigDecimal dischargePortPoid;
    private String placeOfReceipt;
    private String placeOfDelivery;
    private String commodityType;
    private String description;
    private String shippingMode;
    private String termsOfShipment;
    private BigDecimal elsewherePoid;
    private String vesselFrequency;
    private String surcharges;
    private String remarks;
    private String actionStatus;
    private LocalDate actionDueDate;
    private String deleted;
    private String createdBy;
    private LocalDateTime createdDate;
    private String lastModifiedBy;
    private LocalDateTime lastModifiedDate;
    private BigDecimal termsConditionPoid;
    private BigDecimal enquiryRefPoid;
    private String lostReason;
    private String incoTerms;
    private String cargoType;
    private BigDecimal totalSellingAmountLocal;
    private BigDecimal totalBuyingAmountLocal;
    private BigDecimal totalTax;
    private BigDecimal freeDaysPol;
    private BigDecimal freeDaysPod;
    private String transitDays;
    private String importExport;
    private String shippingTerms;
    private String termsOfFreight;
    private String linePrintYn;
    private String routing;
    private BigDecimal quotationCompany;
    private BigDecimal quotationDivision;
    private String printRemarks;
    private String multiPort;
    private String newCustomer;
    private String customerName;
    private String customerContact;
    private String customerEmail;
    private String quotationSubject;
    private String quotationType;
    private BigDecimal vesselPoid;
    private List<SalesQuotationShipChargeRequest> charges = new ArrayList<>();
    private List<SalesQuotationShipEquipmentRequest> equipment = new ArrayList<>();

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

    public BigDecimal getCompanyPoid() {
        return companyPoid;
    }

    public void setCompanyPoid(BigDecimal companyPoid) {
        this.companyPoid = companyPoid;
    }

    public BigDecimal getGroupPoid() {
        return groupPoid;
    }

    public void setGroupPoid(BigDecimal groupPoid) {
        this.groupPoid = groupPoid;
    }

    public BigDecimal getUserPoid() {
        return userPoid;
    }

    public void setUserPoid(BigDecimal userPoid) {
        this.userPoid = userPoid;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDocRef() {
        return docRef;
    }

    public void setDocRef(String docRef) {
        this.docRef = docRef;
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

    public String getQuotationStatus() {
        return quotationStatus;
    }

    public void setQuotationStatus(String quotationStatus) {
        this.quotationStatus = quotationStatus;
    }

    public BigDecimal getSalesmanPoid() {
        return salesmanPoid;
    }

    public void setSalesmanPoid(BigDecimal salesmanPoid) {
        this.salesmanPoid = salesmanPoid;
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

    public BigDecimal getElsewherePoid() {
        return elsewherePoid;
    }

    public void setElsewherePoid(BigDecimal elsewherePoid) {
        this.elsewherePoid = elsewherePoid;
    }

    public String getVesselFrequency() {
        return vesselFrequency;
    }

    public void setVesselFrequency(String vesselFrequency) {
        this.vesselFrequency = vesselFrequency;
    }

    public String getSurcharges() {
        return surcharges;
    }

    public void setSurcharges(String surcharges) {
        this.surcharges = surcharges;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
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

    public String getDeleted() {
        return deleted;
    }

    public void setDeleted(String deleted) {
        this.deleted = deleted;
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

    public BigDecimal getTermsConditionPoid() {
        return termsConditionPoid;
    }

    public void setTermsConditionPoid(BigDecimal termsConditionPoid) {
        this.termsConditionPoid = termsConditionPoid;
    }

    public BigDecimal getEnquiryRefPoid() {
        return enquiryRefPoid;
    }

    public void setEnquiryRefPoid(BigDecimal enquiryRefPoid) {
        this.enquiryRefPoid = enquiryRefPoid;
    }

    public String getLostReason() {
        return lostReason;
    }

    public void setLostReason(String lostReason) {
        this.lostReason = lostReason;
    }

    public String getIncoTerms() {
        return incoTerms;
    }

    public void setIncoTerms(String incoTerms) {
        this.incoTerms = incoTerms;
    }

    public String getCargoType() {
        return cargoType;
    }

    public void setCargoType(String cargoType) {
        this.cargoType = cargoType;
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

    public BigDecimal getFreeDaysPol() {
        return freeDaysPol;
    }

    public void setFreeDaysPol(BigDecimal freeDaysPol) {
        this.freeDaysPol = freeDaysPol;
    }

    public BigDecimal getFreeDaysPod() {
        return freeDaysPod;
    }

    public void setFreeDaysPod(BigDecimal freeDaysPod) {
        this.freeDaysPod = freeDaysPod;
    }

    public String getTransitDays() {
        return transitDays;
    }

    public void setTransitDays(String transitDays) {
        this.transitDays = transitDays;
    }

    public String getImportExport() {
        return importExport;
    }

    public void setImportExport(String importExport) {
        this.importExport = importExport;
    }

    public String getShippingTerms() {
        return shippingTerms;
    }

    public void setShippingTerms(String shippingTerms) {
        this.shippingTerms = shippingTerms;
    }

    public String getTermsOfFreight() {
        return termsOfFreight;
    }

    public void setTermsOfFreight(String termsOfFreight) {
        this.termsOfFreight = termsOfFreight;
    }

    public String getLinePrintYn() {
        return linePrintYn;
    }

    public void setLinePrintYn(String linePrintYn) {
        this.linePrintYn = linePrintYn;
    }

    public String getRouting() {
        return routing;
    }

    public void setRouting(String routing) {
        this.routing = routing;
    }

    public BigDecimal getQuotationCompany() {
        return quotationCompany;
    }

    public void setQuotationCompany(BigDecimal quotationCompany) {
        this.quotationCompany = quotationCompany;
    }

    public BigDecimal getQuotationDivision() {
        return quotationDivision;
    }

    public void setQuotationDivision(BigDecimal quotationDivision) {
        this.quotationDivision = quotationDivision;
    }

    public String getPrintRemarks() {
        return printRemarks;
    }

    public void setPrintRemarks(String printRemarks) {
        this.printRemarks = printRemarks;
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

    public BigDecimal getVesselPoid() {
        return vesselPoid;
    }

    public void setVesselPoid(BigDecimal vesselPoid) {
        this.vesselPoid = vesselPoid;
    }

    public List<SalesQuotationShipChargeRequest> getCharges() {
        return charges;
    }

    public void setCharges(List<SalesQuotationShipChargeRequest> charges) {
        this.charges = charges;
    }

    public List<SalesQuotationShipEquipmentRequest> getEquipment() {
        return equipment;
    }

    public void setEquipment(List<SalesQuotationShipEquipmentRequest> equipment) {
        this.equipment = equipment;
    }
}

