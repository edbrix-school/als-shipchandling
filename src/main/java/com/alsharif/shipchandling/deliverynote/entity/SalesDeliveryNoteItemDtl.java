package com.alsharif.shipchandling.deliverynote.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.alsharif.shipchandling.deliverynote.dto.SalesDeliveryNoteItemDtlId;

import java.sql.Timestamp;

@Entity
@Table(name = "SALES_DELIVERY_NOTE_ITEM_DTL")
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(SalesDeliveryNoteItemDtlId.class)
public class SalesDeliveryNoteItemDtl {

    @Id
    @Column(name = "TRANSACTION_POID", nullable = false)
    private Long transactionPoid;

    @Id
    @Column(name = "DET_ROW_ID", nullable = false)
    private Long detRowId;

    @Column(name = "STOCK_POID")
    private Long stockPoid;

    @Column(name = "STOCK_UNIT_POID")
    private Long stockUnitPoid;

    @Column(name = "QUANTITY")
    private Long quantity;

    @Column(name = "PRICE")
    private Long price;

    @Column(name = "DISCOUNT")
    private Long discount;

    @Column(name = "AMOUNT")
    private Long amount;

    @Column(name = "REMARKS", length = 100)
    private String remarks;

    @Column(name = "CHECK_ALL", length = 1)
    private String checkAll = "Y"; // Y = included, N = excluded (removed before save)

    @Column(name = "QTN_DET_ROW_ID")
    private Long qtnDetRowId; // Links to quotation detail if loaded from quotation

    @Column(name = "TOT_COST")
    private Long totCost;

    @Column(name = "ITEM_TYPE", length = 20)
    private String itemType;

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
}
