package com.asg.shipchandling.salesquotationsch.entity;

import com.asg.common.lib.annotation.AuditIgnore;
import com.asg.common.lib.entity.BaseEntity;
import com.asg.shipchandling.salesquotationsch.dto.SalesQuotationSchItemDtlId;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;

@Entity
@Table(name = "SALES_QUOTATION_ITEM_DTL")
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(SalesQuotationSchItemDtlId.class)
public class SalesQuotationSchItemDtl extends BaseEntity {

    @Id
    @Column(name = "TRANSACTION_POID", nullable = false)
    @AuditIgnore
    private Long transactionPoid;

    @Id
    @Column(name = "DET_ROW_ID", nullable = false)
    @AuditIgnore
    private Long detRowId;

    @Column(name = "STOCK_POID")
    private Long stockPoid;

    @Column(name = "QUANTITY")
    private BigDecimal quantity;

    @Column(name = "PRICE", precision = 18, scale = 6)
    private BigDecimal price;

    @Column(name = "DISCOUNT")
    @AuditIgnore
    private BigDecimal discount;

    @Column(name = "AMOUNT")
    private BigDecimal amount;

    @Column(name = "REMARKS", length = 500)
    private String remarks;

    @Column(name = "STOCK_UNIT_POID")
    private Long stockUnitPoid;

    @Column(name = "ADJ_QUANTITY")
    @AuditIgnore
    private BigDecimal adjQuantity;

    @Column(name = "COST", precision = 18, scale = 6)
    private BigDecimal cost;

    @Column(name = "LAST_RATE1")
    private BigDecimal lastRate1;

    @Column(name = "LAST_RATE2")
    @AuditIgnore
    private BigDecimal lastRate2;

    @Column(name = "DELIVERY_SELECT", length = 1)
    @AuditIgnore
    private String deliverySelect;

    @Column(name = "DN_REF_NO", length = 50)
    @AuditIgnore
    private String dnRefNo;

    @Column(name = "GP_AMOUNT")
    private BigDecimal gpAmount;

    @Column(name = "GP_PERCENTAGE")
    private BigDecimal gpPercentage;

    @Column(name = "TOT_COST")
    @AuditIgnore
    private BigDecimal totCost;

    @Column(name = "PURCHASE_PRICE")
    private BigDecimal purchasePrice;

    @Column(name = "PURCHASE_QTY")
    private BigDecimal purchaseQty;

    @Column(name = "ITEM_TYPE", length = 50)
    private String itemType;

    @Column(name = "REF_DOC_ID", length = 50)
    @AuditIgnore
    private String refDocId;

    @Column(name = "REF_POID")
    @AuditIgnore
    private Long refPoid;

    @Column(name = "TAX_POID")
    @AuditIgnore
    private Long taxPoid;

    @Column(name = "TAX_AMOUNT")
    private BigDecimal taxAmount;

    @Column(name = "TAX_PERCENTAGE")
    private BigDecimal taxPercentage;

    @Column(name = "VAT_MODIFIED", length = 1)
    @AuditIgnore
    private String vatModified;
}
