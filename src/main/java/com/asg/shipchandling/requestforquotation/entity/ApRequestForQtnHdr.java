package com.asg.shipchandling.requestforquotation.entity;

import com.asg.common.lib.annotation.AuditIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Entity
@Table(name = "AP_REQUEST_FOR_QTN_HDR")
@Data
@NoArgsConstructor
@AllArgsConstructor
//@SequenceGenerator(name = "rfq_trans_seq", sequenceName = "TRANSACTION_POID_SEQ", allocationSize = 1)
public class ApRequestForQtnHdr {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TRANSACTION_POID", nullable = false)
    @AuditIgnore
    private Long transactionPoid;

    @Column(name = "DOC_REF", length = 25, unique = true)
    @AuditIgnore
    private String docRef;

    @Column(name = "TRANSACTION_DATE", nullable = false)
    @AuditIgnore
    private Timestamp transactionDate;

    @Column(name = "GROUP_POID", nullable = false)
    @AuditIgnore
    private Long groupPoid;

    @Column(name = "COMPANY_POID", nullable = false)
    @AuditIgnore
    private Long companyPoid;

    @Column(name = "DESCRIPTION", length = 500)
    private String description;

    @Column(name = "STATUS", length = 100)
    private String status = "IN PROGRESS";

    @Column(name = "TYPE", length = 20)
    @AuditIgnore
    private String type;

    @Column(name = "DIVISION_POID")
    private Long divisionPoid;

    @Column(name = "EXPECTED_DATE")
    private Timestamp expectedDate;

    @Column(name = "REMARKS", length = 500)
    private String remarks;

    @Column(name = "DESCRIPTION_PRINT_YN", length = 1)
    private String descriptionPrintYn;

    @Column(name = "CURRENCY_CODE", length = 20)
    @AuditIgnore
    private String currencyCode;

    @Column(name = "CURRENCY_RATE")
    @AuditIgnore
    private BigDecimal currencyRate;

    @Column(name = "SALES_QTN_POID")
    private Long salesQtnPoid;

    @Column(name = "SALES_INV_DOC_REF", length = 50)
    private String salesInvDocRef;

    @Column(name = "DELETED", length = 1)
    @AuditIgnore
    private String deleted = "N";

    @Column(name = "CREATED_BY", length = 20)
    @AuditIgnore
    private String createdBy;

    @CreationTimestamp
    @Column(name = "CREATED_DATE")
    @AuditIgnore
    private Timestamp createdDate;

    @Column(name = "LASTMODIFIED_BY", length = 20)
    @AuditIgnore
    private String lastmodifiedBy;

    @UpdateTimestamp
    @Column(name = "LASTMODIFIED_DATE")
    @AuditIgnore
    private Timestamp lastmodifiedDate;
}