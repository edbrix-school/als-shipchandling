package com.asg.shipchandling.salesinvoice.entity;

import com.asg.common.lib.annotation.AuditIgnore;
import com.asg.common.lib.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.asg.shipchandling.salesinvoice.converter.BigDecimalConverter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "AR_SCH_SALES_INVOICE_DTL")
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(SalesInvoiceDtlId.class)
public class SalesInvoiceDtl extends BaseEntity {

    @Id
    @Column(name = "TRANSACTION_POID", nullable = false)
    @AuditIgnore
    private Long transactionPoid;

    @Id
    @Column(name = "DET_ROW_ID", nullable = false)
    @AuditIgnore
    private Long detRowId;

    @Column(name = "DN_POID_LINK_FK")
    @AuditIgnore
    private Long dnPoidLinkFk;

    @Column(name = "DET_ROW_ID_CHRG_FK")
    @AuditIgnore
    private Long detRowIdChrgFk;

    @Column(name = "STOCK_POID")
    private Long stockPoid;

    @Column(name = "QUANTITY")
    private BigDecimal quantity;

    @Column(name = "PRICE")
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @jakarta.persistence.Convert(converter = BigDecimalConverter.class)
    private BigDecimal price;

    @Column(name = "DISCOUNT")
    private BigDecimal discount;

    @Column(name = "AMOUNT")
    private BigDecimal amount;

    @Column(name = "REMARKS", length = 500)
    private String remarks;

    @Column(name = "STOCK_UNIT_POID")
    private Long stockUnitPoid;

    @Column(name = "QUOTATION_POID")
    private Long quotationPoid;

    @Column(name = "COST_AMT")
    private BigDecimal costAmt;

    @Column(name = "QTN_DET_ROW_ID")
    @AuditIgnore
    private Long quotationDetRowId;

    @Column(name = "PURCHASE_PRICE")
    private BigDecimal purchasePrice;

    @Column(name = "PURCHASE_QTY")
    private BigDecimal purchaseQty;

    @Column(name = "NET_SALES")
    private BigDecimal netSales;

    @Column(name = "NET_DISCOUNT")
    @AuditIgnore
    private BigDecimal netDiscount;

    @Column(name = "ITEM_GP")
    private BigDecimal itemGp;

    @Column(name = "ITEM_GP_PER")
    private BigDecimal itemGpPer;

    @Column(name = "ITEM_TYPE", length = 50)
    private String itemType;

    @Column(name = "TAX_PERCENTAGE")
    private BigDecimal taxPercentage;

    @Column(name = "TAX_AMOUNT")
    private BigDecimal taxAmount;

    @Column(name = "TAX_POID")
    private Long taxPoid;

    @Column(name = "BASE_AMT")
    @AuditIgnore
    private BigDecimal baseAmt;

    @Column(name = "INCENTIVE")
    private BigDecimal incentive;

    @Column(name = "COST_POID")
    private String costPoid;
}

