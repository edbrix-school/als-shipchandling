package com.asg.shipchandling.salesquotation.entity;

import com.asg.common.lib.entity.BaseEntity;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "SALES_QUOTATION_SHIP_EQUIP_DTL")
public class SalesQuotationShipEquipmentDetail extends BaseEntity {

    @EmbeddedId
    private SalesQuotationShipEquipmentDetailId id;

    @MapsId("transactionPoid")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TRANSACTION_POID", nullable = false, insertable = false, updatable = false)
    private SalesQuotationShipHeader header;

    @Column(name = "EQUIPMENT_POID")
    private BigDecimal equipmentPoid;

    @Column(name = "QUANTITY")
    private BigDecimal quantity;

    @Column(name = "OOG", length = 1)
    private String oog;

    @Column(name = "OOG_DETAILS", length = 50)
    private String oogDetails;

    @Column(name = "DG", length = 1)
    private String dangerousGoods;

    @Column(name = "DG_CLASS", length = 50)
    private String dangerousGoodsClass;

    @Column(name = "UNO", length = 50)
    private String unoNumber;

    @Column(name = "TEMPERATURE", length = 20)
    private String temperature;

    @Column(name = "REMARKS", length = 100)
    private String remarks;

    public SalesQuotationShipEquipmentDetailId getId() {
        return id;
    }

    public void setId(SalesQuotationShipEquipmentDetailId id) {
        this.id = id;
    }

    public SalesQuotationShipHeader getHeader() {
        return header;
    }

    public void setHeader(SalesQuotationShipHeader header) {
        this.header = header;
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

}

