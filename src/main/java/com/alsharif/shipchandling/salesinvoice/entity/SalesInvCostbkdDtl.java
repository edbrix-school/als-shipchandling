package com.alsharif.shipchandling.salesinvoice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Timestamp;



@Entity
@Table(name = "AR_SCH_SALES_INV_COSTBKD_DTL")
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(SalesInvCostbkdDtlId.class)
public class SalesInvCostbkdDtl {

    @Id
    @Column(name = "TRANSACTION_POID", nullable = false)
    private Long transactionPoid;

    @Id
    @Column(name = "DET_ROW_ID", nullable = false)
    private Long detRowId;

    @Column(name = "BOOKING_POID_FK")
    private Long bookingPoidFk;

    @Column(name = "DOC_REF_FK", length = 25)
    private String docRefFk;

    @Column(name = "SUPPLIER_POID")
    private Long supplierPoid;

    @Column(name = "COST_AMOUNT")
    private Long costAmount;

    @Column(name = "REMARKS", length = 200)
    private String remarks;

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

    @Column(name = "SUPPLIER_NAME", length = 200)
    private String supplierName;

    @Column(name = "BOOKED_DATE")
    private Timestamp bookedDate;

    @Column(name = "BOOK_TYPE", length = 50)
    private String bookType;

    @Column(name = "SALES_QTN_POID")
    private Long salesQtnPoid;
}


