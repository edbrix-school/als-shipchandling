package com.alsharif.shipchandling.salesquotation.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class SalesQuotationShipItemDto {

    private BigDecimal transactionPoid;
    private BigDecimal detailRowId;
    private BigDecimal chargePoid;
    private String currencyCode;
    private BigDecimal currencyRate;
    private BigDecimal buyingCharge;
    private BigDecimal buyingChargeLocal;
    private BigDecimal sellingCharge;
    private BigDecimal sellingChargeLocal;
    private BigDecimal totalSellingChargeLocal;
    private String remarks;
    private BigDecimal equipmentPoid;
    private BigDecimal dischargePortPoid;
    private BigDecimal loadingPortPoid;
    private BigDecimal quantity;
    private BigDecimal perBuy;
    private BigDecimal perQty;
    private BigDecimal quotationDays;
    private String unit;
    private BigDecimal taxPoid;
    private BigDecimal taxPercentage;
    private BigDecimal taxAmount;
    private BigDecimal taxAmountLocal;
    private String createdBy;
    private LocalDateTime createdDate;

    public BigDecimal getDetailRowId() {
        return detailRowId;
    }

    public void setDetailRowId(BigDecimal detailRowId) {
        this.detailRowId = detailRowId;
    }

    public BigDecimal getChargePoid() {
        return chargePoid;
    }

    public void setChargePoid(BigDecimal chargePoid) {
        this.chargePoid = chargePoid;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public BigDecimal getCurrencyRate() {
        return currencyRate;
    }

    public void setCurrencyRate(BigDecimal currencyRate) {
        this.currencyRate = currencyRate;
    }

    public BigDecimal getBuyingCharge() {
        return buyingCharge;
    }

    public void setBuyingCharge(BigDecimal buyingCharge) {
        this.buyingCharge = buyingCharge;
    }

    public BigDecimal getBuyingChargeLocal() {
        return buyingChargeLocal;
    }

    public void setBuyingChargeLocal(BigDecimal buyingChargeLocal) {
        this.buyingChargeLocal = buyingChargeLocal;
    }

    public BigDecimal getSellingCharge() {
        return sellingCharge;
    }

    public void setSellingCharge(BigDecimal sellingCharge) {
        this.sellingCharge = sellingCharge;
    }

    public BigDecimal getSellingChargeLocal() {
        return sellingChargeLocal;
    }

    public void setSellingChargeLocal(BigDecimal sellingChargeLocal) {
        this.sellingChargeLocal = sellingChargeLocal;
    }

    public BigDecimal getTotalSellingChargeLocal() {
        return totalSellingChargeLocal;
    }

    public void setTotalSellingChargeLocal(BigDecimal totalSellingChargeLocal) {
        this.totalSellingChargeLocal = totalSellingChargeLocal;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public BigDecimal getEquipmentPoid() {
        return equipmentPoid;
    }

    public void setEquipmentPoid(BigDecimal equipmentPoid) {
        this.equipmentPoid = equipmentPoid;
    }

    public BigDecimal getDischargePortPoid() {
        return dischargePortPoid;
    }

    public void setDischargePortPoid(BigDecimal dischargePortPoid) {
        this.dischargePortPoid = dischargePortPoid;
    }

    public BigDecimal getLoadingPortPoid() {
        return loadingPortPoid;
    }

    public void setLoadingPortPoid(BigDecimal loadingPortPoid) {
        this.loadingPortPoid = loadingPortPoid;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPerBuy() {
        return perBuy;
    }

    public void setPerBuy(BigDecimal perBuy) {
        this.perBuy = perBuy;
    }

    public BigDecimal getPerQty() {
        return perQty;
    }

    public void setPerQty(BigDecimal perQty) {
        this.perQty = perQty;
    }

    public BigDecimal getQuotationDays() {
        return quotationDays;
    }

    public void setQuotationDays(BigDecimal quotationDays) {
        this.quotationDays = quotationDays;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public BigDecimal getTaxPoid() {
        return taxPoid;
    }

    public void setTaxPoid(BigDecimal taxPoid) {
        this.taxPoid = taxPoid;
    }

    public BigDecimal getTaxPercentage() {
        return taxPercentage;
    }

    public void setTaxPercentage(BigDecimal taxPercentage) {
        this.taxPercentage = taxPercentage;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }

    public BigDecimal getTaxAmountLocal() {
        return taxAmountLocal;
    }

    public void setTaxAmountLocal(BigDecimal taxAmountLocal) {
        this.taxAmountLocal = taxAmountLocal;
    }

    public BigDecimal getTransactionPoid() {
        return transactionPoid;
    }

    public void setTransactionPoid(BigDecimal transactionPoid) {
        this.transactionPoid = transactionPoid;
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
}
