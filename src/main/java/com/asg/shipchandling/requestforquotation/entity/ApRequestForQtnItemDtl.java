package com.asg.shipchandling.requestforquotation.entity;

import com.asg.common.lib.annotation.AuditIgnore;
import com.asg.common.lib.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;


@Entity
@Table(name = "AP_REQUEST_FOR_QTN_ITEM_DTL")
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(ApRequestForQtnItemDtlId.class)
public class ApRequestForQtnItemDtl extends BaseEntity {

    @Id
    @Column(name = "TRANSACTION_POID", nullable = false)
    @AuditIgnore
    private Long transactionPoid;

    @Id
    @Column(name = "DET_ROW_ID", nullable = false)
    @AuditIgnore
    private Long detRowId;

    @Column(name = "STOCK_POID", nullable = false)
    private Long stockPoid;

    @Column(name = "STOCK_UNIT_POID", nullable = false)
    private Long stockUnitPoid;

    @Column(name = "QTY", nullable = false)
    private BigDecimal qty;

    @Column(name = "SUPPLIER_POID")
    private Long supplierPoid;

    @Column(name = "PRICE")
    private BigDecimal price;

    @Column(name = "TAX_POID")
    private Long taxPoid;

    @Column(name = "TAX_PERCENTAGE")
    private BigDecimal taxPercentage;

    @Column(name = "TAX_AMOUNT")
    private BigDecimal taxAmount;

    @Column(name = "LAST_RATE")
    private BigDecimal lastRate;

    @Column(name = "PURCHASE_QTY")
    private BigDecimal purchaseQty;

    @Column(name = "PURCHASE_PRICE")
    private BigDecimal purchasePrice;

    @Column(name = "REF_DOC_ID", length = 30)
    @AuditIgnore
    private String refDocId;

    @Column(name = "REF_POID", length = 50)
    @AuditIgnore
    private String refPoid;

    @Column(name = "REMARKS", length = 500)
    private String remarks;

}