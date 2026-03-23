package com.asg.shipchandling.salesinvoice.entity;

import com.asg.common.lib.annotation.AuditIgnore;
import com.asg.common.lib.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;


@Entity
@Table(name = "AR_SCH_SALES_INV_COSTBKD_DTL")
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(SalesInvCostbkdDtlId.class)
public class SalesInvCostbkdDtl extends BaseEntity {

    @Id
    @Column(name = "TRANSACTION_POID", nullable = false)
    @AuditIgnore
    private Long transactionPoid;

    @Id
    @Column(name = "DET_ROW_ID", nullable = false)
    @AuditIgnore
    private Long detRowId;

    @Column(name = "BOOKING_POID_FK")
    @AuditIgnore
    private Long bookingPoidFk;

    @Column(name = "DOC_REF_FK", length = 25)
    @AuditIgnore
    private String docRefFk;

    @Column(name = "SUPPLIER_POID")
    @AuditIgnore
    private Long supplierPoid;

    @Column(name = "COST_AMOUNT")
    private Long costAmount;

    @Column(name = "REMARKS", length = 200)
    private String remarks;

    @Column(name = "SUPPLIER_NAME", length = 200)
    private String supplierName;

    @Column(name = "BOOKED_DATE")
    private LocalDateTime bookedDate;

    @Column(name = "BOOK_TYPE", length = 50)
    private String bookType;

    @Column(name = "SALES_QTN_POID")
    @AuditIgnore
    private Long salesQtnPoid;
}


