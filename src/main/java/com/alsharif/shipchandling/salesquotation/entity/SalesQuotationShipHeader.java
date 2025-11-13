package com.alsharif.shipchandling.salesquotation.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "SALES_QUOTATION_SHIP_HDR")
public class SalesQuotationShipHeader {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TRANSACTION_POID")
    private BigDecimal transactionPoid;

    @Column(name = "TRANSACTION_DATE")
    private LocalDate transactionDate;

//    @Column(name = "GROUP_POID")
//    private BigDecimal groupPoid;

    @Column(name = "COMPANY_POID")
    private BigDecimal companyPoid;

    @Column(name = "DOC_REF", length = 25)
    private String docRef;

    @Column(name = "CUSTOMER_POID")
    private BigDecimal customerPoid;

    @Column(name = "ADDRESS_POID")
    private BigDecimal addressPoid;

    @Column(name = "LINE_POID")
    private BigDecimal linePoid;

    @Column(name = "QUOTATION_STATUS", length = 20)
    private String quotationStatus;

    @Column(name = "SALESMAN_POID")
    private BigDecimal salesmanPoid;

    @Column(name = "VALIDITY_FROM_DATE")
    private LocalDate validityFromDate;

    @Column(name = "VALIDITY_TO_DATE")
    private LocalDate validityToDate;

    @Column(name = "LOADING_PORT_POID")
    private BigDecimal loadingPortPoid;

    @Column(name = "DISCHARGE_PORT_POID")
    private BigDecimal dischargePortPoid;

    @Column(name = "PLACE_OF_RECEIPT", length = 100)
    private String placeOfReceipt;

    @Column(name = "PLACE_OF_DELIVERY", length = 100)
    private String placeOfDelivery;

    @Column(name = "COMMODITY_TYPE", length = 50)
    private String commodityType;

    @Column(name = "DESCRIPTION", length = 500)
    private String description;
    @Column(name = "SHIPPING_MODE", length = 20)
    private String shippingMode;

    @Column(name = "TERMS_OF_SHIPMENT", length = 20)
    private String termsOfShipment;

    @Column(name = "ELSEWHERE_POID")
    private BigDecimal elsewherePoid;

    @Column(name = "VESSEL_FREQUENCY", length = 20)
    private String vesselFrequency;

    @Column(name = "SURCHARGES", length = 50)
    private String surcharges;

    @Column(name = "REMARKS", length = 500)
    private String remarks;

    @Column(name = "ACTION_STATUS", length = 20)
    private String actionStatus;

    @Column(name = "ACTION_DUE_DATE")
    private LocalDate actionDueDate;

    @Column(name = "DELETED", length = 1)
    private String deleted;

    @Column(name = "CREATED_BY", length = 20)
    private String createdBy;

    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;

    @Column(name = "LASTMODIFIED_BY", length = 20)
    private String lastModifiedBy;

    @Column(name = "LASTMODIFIED_DATE")
    private LocalDateTime lastModifiedDate;

    @Column(name = "TERMS_CONDITION_POID")
    private BigDecimal termsConditionPoid;

    @Column(name = "ENQUIRY_REF_POID")
    private BigDecimal enquiryRefPoid;

    @Column(name = "LOST_REASON", length = 20)
    private String lostReason;

    @Column(name = "INCO_TERMS", length = 20)
    private String incoTerms;

    @Column(name = "CARGO_TYPE", length = 20)
    private String cargoType;

    @Column(name = "TOTAL_SELLING_AMOUNT_LOCAL")
    private BigDecimal totalSellingAmountLocal;

    @Column(name = "TOTAL_BUYING_AMOUNT_LOCAL")
    private BigDecimal totalBuyingAmountLocal;

    @Column(name = "TOTAL_TAX")
    private BigDecimal totalTax;

    @Column(name = "FREE_DAYS_POL")
    private BigDecimal freeDaysPol;

    @Column(name = "FREE_DAYS_POD")
    private BigDecimal freeDaysPod;

    @Column(name = "TRANSIT_DAYS", length = 20)
    private String transitDays;

    @Column(name = "IMPORT_EXPORT", length = 20)
    private String importExport;

    @Column(name = "SHIPPING_TERMS", length = 20)
    private String shippingTerms;

    @Column(name = "TERMS_OF_FREIGHT", length = 20)
    private String termsOfFreight;

    @Column(name = "LINE_PRINT_YN", length = 1)
    private String linePrintYn;

    @Column(name = "ROUTING", length = 100)
    private String routing;

    @Column(name = "QTN_COMPANY")
    private BigDecimal quotationCompany;

    @Column(name = "QTN_DIVISION")
    private BigDecimal quotationDivision;

    @Column(name = "PRINT_REMARKS", length = 500)
    private String printRemarks;

    @Column(name = "MULTI_PORT", length = 1)
    private String multiPort;

    @Column(name = "NEW_CUSTOMER", length = 1)
    private String newCustomer;

    @Column(name = "CUSTOMER_NAME", length = 200)
    private String customerName;

    @Column(name = "CUSTOMER_CONTACT", length = 200)
    private String customerContact;

    @Column(name = "CUSTOMER_EMAIL", length = 200)
    private String customerEmail;

    @Column(name = "QTN_SUBJECT", length = 200)
    private String quotationSubject;

    @Column(name = "QTN_TYPE", length = 200)
    private String quotationType;

    @Column(name = "VESSEL_POID")
    private BigDecimal vesselPoid;

    @OneToMany(mappedBy = "header", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id.detailRowId ASC")
    @Fetch(FetchMode.SUBSELECT)
    private List<SalesQuotationShipChargeDetail> charges = new ArrayList<>();

    @OneToMany(mappedBy = "header", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id.detailRowId ASC")
    @Fetch(FetchMode.SUBSELECT)
    private List<SalesQuotationShipEquipmentDetail> equipment = new ArrayList<>();


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

   // public BigDecimal getGroupPoid() {
//        return groupPoid;
//    }

//    public void setGroupPoid(BigDecimal groupPoid) {
//        this.groupPoid = groupPoid;
//    }

    public BigDecimal getCompanyPoid() {
        return companyPoid;
    }

    public void setCompanyPoid(BigDecimal companyPoid) {
        this.companyPoid = companyPoid;
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

    public List<SalesQuotationShipChargeDetail> getCharges() {
        return charges;
    }

    public void setCharges(List<SalesQuotationShipChargeDetail> charges) {
        this.charges = charges;
    }

    public List<SalesQuotationShipEquipmentDetail> getEquipment() {
        return equipment;
    }

    public void setEquipment(List<SalesQuotationShipEquipmentDetail> equipment) {
        this.equipment = equipment;
    }

    public void addCharge(SalesQuotationShipChargeDetail charge) {
        if (charges == null) {
            charges = new ArrayList<>();
        }
        charges.add(charge);
        charge.setHeader(this);
    }

    public void clearCharges() {
        if (charges != null) {
            charges.clear();
        }
    }

    public void addEquipment(SalesQuotationShipEquipmentDetail detail) {
        if (equipment == null) {
            equipment = new ArrayList<>();
        }
        equipment.add(detail);
        detail.setHeader(this);
    }

    public void clearEquipment() {
        if (equipment != null) {
            equipment.clear();
        }
    }
}
