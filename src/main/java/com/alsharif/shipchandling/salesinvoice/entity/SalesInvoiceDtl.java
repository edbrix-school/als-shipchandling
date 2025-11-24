package com.alsharif.shipchandling.salesinvoice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.alsharif.shipchandling.salesinvoice.converter.BigDecimalConverter;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Entity
@Table(name = "AR_SCH_SALES_INVOICE_DTL")
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(SalesInvoiceDtlId.class)
public class SalesInvoiceDtl {

    @Id
    @Column(name = "TRANSACTION_POID", nullable = false)
    private Long transactionPoid;

    @Id
    @Column(name = "DET_ROW_ID", nullable = false)
    private Long detRowId;

    @Column(name = "DN_POID_LINK_FK")
    private Long dnPoidLinkFk;

    @Column(name = "DET_ROW_ID_CHRG_FK")
    private Long detRowIdChrgFk;

    @Column(name = "STOCK_POID")
    private Long stockPoid;

    @Column(name = "QUANTITY")
    private Long quantity;

    @Column(name = "PRICE")
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @jakarta.persistence.Convert(converter = BigDecimalConverter.class)
    private BigDecimal price;

    @Column(name = "DISCOUNT")
    private Long discount;

    @Column(name = "AMOUNT")
    private Long amount;

    @Column(name = "REMARKS", length = 500)
    private String remarks;

    @Column(name = "STOCK_UNIT_POID")
    private Long stockUnitPoid;

    @Column(name = "CREATED_BY", length = 20)
    private String createdBy;

    @CreationTimestamp
    @Column(name = "CREATED_DATE")
    private Timestamp createdDate;

    @Column(name = "LASTMODIFIED_BY", length = 20)
    private String lastmodifiedBy;

    @UpdateTimestamp
    @Column(name = "LASTMODIFIED_DATE")
    private Timestamp lastmodifiedDate;

    @Column(name = "QUOTATION_POID")
    private Long quotationPoid;

    @Column(name = "COST_AMT")
    private Long costAmt;

    @Column(name = "QTN_DET_ROW_ID")
    private Long quotationDetRowId;

    @Column(name = "PURCHASE_PRICE")
    private Long purchasePrice;

    @Column(name = "PURCHASE_QTY")
    private Long purchaseQty;

    @Column(name = "NET_SALES")
    private Long netSales;

    @Column(name = "NET_DISCOUNT")
    private Long netDiscount;

    @Column(name = "ITEM_GP")
    private Long itemGp;

    @Column(name = "ITEM_GP_PER")
    private Long itemGpPer;

    @Column(name = "ITEM_TYPE", length = 50)
    private String itemType;

    @Column(name = "TAX_PERCENTAGE")
    private Long taxPercentage;

    @Column(name = "TAX_AMOUNT")
    private Long taxAmount;

    @Column(name = "TAX_POID")
    private Long taxPoid;

    @Column(name = "BASE_AMT")
    private Long baseAmt;

    @Column(name = "INCENTIVE")
    private Long incentive;

    @Column(name = "COST_POID")
    private String costPoid;
}

