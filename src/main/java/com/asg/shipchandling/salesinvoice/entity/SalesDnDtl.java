package com.asg.shipchandling.salesinvoice.entity;

import com.asg.common.lib.annotation.AuditIgnore;
import com.asg.common.lib.entity.BaseEntity;
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
@IdClass(SalesDnDtlId.class)
public class SalesDnDtl extends BaseEntity {

    @Id
    @Column(name = "TRANSACTION_POID", nullable = false)
    @AuditIgnore
    private Long transactionPoid;

    @Id
    @Column(name = "DET_ROW_ID", nullable = false)
    @AuditIgnore
    private Long detRowId;

    @Column(name = "DN_POID_FK")
    private Long dnPoidFk;

    @Column(name = "QUOTATION_POID_FK")
    @AuditIgnore
    private Long quotationPoidFk;

    @Column(name = "REMARKS", length = 200)
    private String remarks;

}


