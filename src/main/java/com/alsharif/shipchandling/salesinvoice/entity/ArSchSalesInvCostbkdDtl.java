package com.alsharif.shipchandling.salesinvoice.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;



@Entity
@Table(name = "AR_SCH_SALES_INV_COSTBKD_DTL")
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(ArSchSalesInvCostbkdDtlId.class)
public class ArSchSalesInvCostbkdDtl {

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

    @Column(name = "COST_AMT")
    private Long costAmt;

    @Column(name = "REMARKS", length = 200)
    private String remarks;
}


