package com.asg.shipchandling.salesquotationsch.entity;

import com.asg.shipchandling.salesquotationsch.dto.SalesQuotationSchItemDtlId;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Timestamp;

@Entity
@Table(name = "SALES_QUOTATION_ITEM_DTL")
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(SalesQuotationSchItemDtlId.class)
public class SalesQuotationSchItemDtl {

    @Id
    @Column(name = "TRANSACTION_POID", nullable = false)
    private Long transactionPoid;

    @Id
    @Column(name = "DET_ROW_ID", nullable = false)
    private Long detRowId;

    @Column(name = "STOCK_POID")
    private Long stockPoid;

    @Column(name = "QUANTITY")
    private Long quantity;

    @Column(name = "PRICE")
    private Long price;

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

    @Column(name = "ADJ_QUANTITY")
    private Long adjQuantity;

    @Column(name = "COST")
    private Long cost;

    @Column(name = "LAST_RATE1")
    private Long lastRate1;

    @Column(name = "LAST_RATE2")
    private Long lastRate2;

    @Column(name = "DELIVERY_SELECT", length = 1)
    private String deliverySelect;

    @Column(name = "DN_REF_NO", length = 50)
    private String dnRefNo;

    @Column(name = "GP_AMOUNT")
    private Long gpAmount;

    @Column(name = "GP_PERCENTAGE")
    private Long gpPercentage;

    @Column(name = "TOT_COST")
    private Long totCost;

    @Column(name = "PURCHASE_PRICE")
    private Long purchasePrice;

    @Column(name = "PURCHASE_QTY")
    private Long purchaseQty;

    @Column(name = "ITEM_TYPE", length = 50)
    private String itemType;

    @Column(name = "REF_DOC_ID", length = 50)
    private String refDocId;

    @Column(name = "REF_POID")
    private Long refPoid;

    @Column(name = "TAX_POID")
    private Long taxPoid;

    @Column(name = "TAX_AMOUNT")
    private Long taxAmount;

    @Column(name = "TAX_PERCENTAGE")
    private Long taxPercentage;

    @Column(name = "VAT_MODIFIED", length = 1)
    private String vatModified;
}
