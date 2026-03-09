package com.asg.shipchandling.salesquotation.entity;

import com.asg.common.lib.entity.BaseEntity;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "SALES_QUOTATION_SHIP_CHARG_DTL")
public class SalesQuotationShipChargeDetail extends BaseEntity {

    @EmbeddedId
    private SalesQuotationShipChargeDetailId id;

    @MapsId("transactionPoid")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TRANSACTION_POID", nullable = false, insertable = false, updatable = false)
    private SalesQuotationShipHeader header;

    @Column(name = "CHARGE_POID")
    private BigDecimal chargePoid;

    @Column(name = "CURRENCY_CODE", length = 20)
    private String currencyCode;

    @Column(name = "CURRENCY_RATE")
    private BigDecimal currencyRate;

    @Column(name = "BUYING_CHARGE")
    private BigDecimal buyingCharge;

    @Column(name = "BUYING_CHARGE_LOCAL")
    private BigDecimal buyingChargeLocal;

    @Column(name = "SELLING_CHARGE")
    private BigDecimal sellingCharge;

    @Column(name = "SELLING_CHARGE_LOCAL")
    private BigDecimal sellingChargeLocal;

    @Column(name = "TOTAL_SELLING_CHARGE_LOCAL")
    private BigDecimal totalSellingChargeLocal;

    @Column(name = "REMARKS", length = 500)
    private String remarks;

    @Column(name = "EQUIPMENT_POID")
    private BigDecimal equipmentPoid;

    @Column(name = "DISCHARGE_PORT_POID")
    private BigDecimal dischargePortPoid;

    @Column(name = "LOADING_PORT_POID")
    private BigDecimal loadingPortPoid;

    @Column(name = "QTY")
    private BigDecimal quantity;

    @Column(name = "PER_BUY")
    private BigDecimal perBuy;

    @Column(name = "PER_QTY")
    private BigDecimal perQty;

    @Column(name = "QTN_DAYS")
    private BigDecimal quotationDays;

    @Column(name = "UNIT", length = 50)
    private String unit;

    @Column(name = "TAX_POID")
    private BigDecimal taxPoid;

    @Column(name = "TAX_PERCENTAGE")
    private BigDecimal taxPercentage;

    @Column(name = "TAX_AMOUNT")
    private BigDecimal taxAmount;

    @Column(name = "TAX_AMOUNT_LOCAL")
    private BigDecimal taxAmountLocal;

    public SalesQuotationShipChargeDetailId getId() {
        return id;
    }

    public void setId(SalesQuotationShipChargeDetailId id) {
        this.id = id;
    }

    public SalesQuotationShipHeader getHeader() {
        return header;
    }

    public void setHeader(SalesQuotationShipHeader header) {
        this.header = header;
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

}
