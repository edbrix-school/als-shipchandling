package com.asg.shipchandling.salesquotation.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class SalesQuotationShipEquipmentDto {

    private BigDecimal transactionPoid;
    private BigDecimal detailRowId;
    private BigDecimal equipmentPoid;
    private BigDecimal quantity;
    private String oog;
    private String oogDetails;
    private String dangerousGoods;
    private String dangerousGoodsClass;
    private String unoNumber;
    private String temperature;
    private String remarks;
    private String createdBy;
    private LocalDateTime createdDate;

    public BigDecimal getTransactionPoid() {
        return transactionPoid;
    }

    public void setTransactionPoid(BigDecimal transactionPoid) {
        this.transactionPoid = transactionPoid;
    }

    public BigDecimal getDetailRowId() {
        return detailRowId;
    }

    public void setDetailRowId(BigDecimal detailRowId) {
        this.detailRowId = detailRowId;
    }

    public BigDecimal getEquipmentPoid() {
        return equipmentPoid;
    }

    public void setEquipmentPoid(BigDecimal equipmentPoid) {
        this.equipmentPoid = equipmentPoid;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public String getOog() {
        return oog;
    }

    public void setOog(String oog) {
        this.oog = oog;
    }

    public String getOogDetails() {
        return oogDetails;
    }

    public void setOogDetails(String oogDetails) {
        this.oogDetails = oogDetails;
    }

    public String getDangerousGoods() {
        return dangerousGoods;
    }

    public void setDangerousGoods(String dangerousGoods) {
        this.dangerousGoods = dangerousGoods;
    }

    public String getDangerousGoodsClass() {
        return dangerousGoodsClass;
    }

    public void setDangerousGoodsClass(String dangerousGoodsClass) {
        this.dangerousGoodsClass = dangerousGoodsClass;
    }

    public String getUnoNumber() {
        return unoNumber;
    }

    public void setUnoNumber(String unoNumber) {
        this.unoNumber = unoNumber;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
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

