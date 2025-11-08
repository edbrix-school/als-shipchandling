package com.alsharif.shipchandling.salesinvoice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Timestamp;

@Entity
@Table(name = "AR_SCH_SALES_DN_DTL")
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(ArSchSalesDnDtlId.class)
public class ArSchSalesDnDtl {

    @Id
    @Column(name = "TRANSACTION_POID", nullable = false)
    private Long transactionPoid;

    @Id
    @Column(name = "DET_ROW_ID", nullable = false)
    private Long detRowId;

    @Column(name = "DN_POID_FK")
    private Long dnPoidFk;

    @Column(name = "QUOTATION_POID_FK")
    private Long quotationPoidFk;

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
}


