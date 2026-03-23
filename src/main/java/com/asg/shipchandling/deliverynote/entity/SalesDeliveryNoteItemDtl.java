package com.asg.shipchandling.deliverynote.entity;

import com.asg.common.lib.annotation.AuditIgnore;
import com.asg.common.lib.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.asg.shipchandling.deliverynote.dto.SalesDeliveryNoteItemDtlId;

import java.math.BigDecimal;

@Entity
@Table(name = "SALES_DELIVERY_NOTE_ITEM_DTL")
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(SalesDeliveryNoteItemDtlId.class)
public class SalesDeliveryNoteItemDtl extends BaseEntity {

    @Id
    @Column(name = "TRANSACTION_POID", nullable = false)
    @AuditIgnore
    private Long transactionPoid;

    @Id
    @Column(name = "DET_ROW_ID", nullable = false)
    @AuditIgnore
    private Long detRowId;

    @Column(name = "STOCK_POID")
    @AuditIgnore
    private Long stockPoid;

    @Column(name = "QUANTITY")
    private Long quantity;

    @Column(name = "PRICE")
    private BigDecimal price;

    @Column(name = "DISCOUNT")
    @AuditIgnore
    private Long discount;

    @Column(name = "AMOUNT")
    @AuditIgnore
    private Long amount;

    @Column(name = "REMARKS", length = 4000)
    private String remarks;

    @Column(name = "STOCK_UNIT_POID")
    private Long stockUnitPoid;

    @Column(name = "QTN_DET_ROW_ID")
    @AuditIgnore
    private Long qtnDetRowId; // Links to quotation detail if loaded from quotation

    @Column(name = "TOT_COST")
    @AuditIgnore
    private Long totCost;

    @Column(name = "ITEM_TYPE", length = 20)
    private String itemType;

    @Column(name = "CHECK_ALL", length = 1)
    private String checkAll = "Y"; // Y = included, N = excluded (removed before save)

}
