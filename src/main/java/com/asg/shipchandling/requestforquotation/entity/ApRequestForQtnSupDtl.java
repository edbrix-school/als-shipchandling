package com.asg.shipchandling.requestforquotation.entity;

import com.asg.common.lib.annotation.AuditIgnore;
import com.asg.common.lib.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "AP_REQUEST_FOR_QTN_SUP_DTL")
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(ApRequestForQtnSupDtlId.class)
public class ApRequestForQtnSupDtl extends BaseEntity {

    @Id
    @Column(name = "TRANSACTION_POID", nullable = false)
    @AuditIgnore
    private Long transactionPoid;

    @Id
    @Column(name = "DET_ROW_ID", nullable = false)
    @AuditIgnore
    private Long detRowId;

    @Column(name = "SUPPLIER_POID")
    private Long supplierPoid;

    @Column(name = "REMARKS", length = 100)
    private String remarks;

}