package com.asg.shipchandling.deliverynote.entity;

import com.asg.common.lib.annotation.AuditIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.sql.Timestamp;

@Entity
@Table(name = "SALES_DELIVERY_NOTE_HDR")
@Data
@NoArgsConstructor
@AllArgsConstructor
// @SequenceGenerator(name = "dn_trans_seq", sequenceName =
// "TRANSACTION_POID_SEQ", allocationSize = 1)

public class SalesDeliveryNoteHdr {

    @Id
    // @GeneratedValue(strategy = GenerationType.SEQUENCE, generator =
    // "dn_trans_seq")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TRANSACTION_POID", nullable = false)
    @AuditIgnore
    private Long transactionPoid;

    @Column(name = "DOC_REF", length = 25, unique = true, insertable = false, updatable = false)
    @Generated(GenerationTime.INSERT)
    @AuditIgnore
    private String docRef;

    @Column(name = "TRANSACTION_DATE")
    @AuditIgnore
    private Timestamp transactionDate;

    @Column(name = "COMPANY_POID")
    @AuditIgnore
    private Long companyPoid;

    @Column(name = "CUSTOMER_POID")
    private Long customerPoid;

    @Column(name = "CURRENCY_CODE", length = 20)
    private String currencyCode;

    @Column(name = "CURRENCY_RATE")
    private BigDecimal currencyRate;

    @Column(name = "DELIVERY_STATUS", length = 20)
    private String deliveryStatus;

    @Column(name = "SALESMAN_POID")
    private Long salesmanPoid;

    @Column(name = "PAYMENT_MODE", length = 20)
    private String paymentMode;

    @Column(name = "DELIVERY_TERMS", length = 30)
    private String deliveryTerms;

    @Column(name = "LINE_POID")
    @AuditIgnore
    private Long linePoid;

    @Column(name = "VESSEL_POID", length = 50)
    private String vesselPoid;

    @Column(name = "VESSEL_NAME", length = 50)
    private String vesselName;

    @Column(name = "VOYAGE_REF", length = 20)
    private String voyageRef;

    @Column(name = "PORT_POID")
    private Long portPoid;

    @Column(name = "PORT_DESCRIPTION", length = 50)
    private String portDescription;

    @Column(name = "QTN_REF_NO", length = 25)
    private String qtnRefNo;

    @Column(name = "VESSEL_AGENT", length = 50)
    private String vesselAgent;

    @Column(name = "DELIVERY_TO_ADDRESS", length = 500)
    private String deliveryToAddress;

    @Column(name = "DESCRIPTION_PRINT_YN", length = 1)
    private String descriptionPrintYn = "Y";

    @Column(name = "PARTY_ADDRESS_DETAILS", length = 1000)
    @AuditIgnore
    private String partyAddressDetails;

    @Column(name = "PRINT_DIVISION_POID")
    private Long printDivisionPoid;

    @Column(name = "PARTY_TYPE", length = 100)
    private String partyType;

    @Column(name = "PRINCIPAL_POID")
    @AuditIgnore
    private Long principalPoid;

    @Column(name = "TOTAL_DISCOUNT")
    @AuditIgnore
    private Long totalDiscount;

    @Column(name = "TOTAL_AMOUNT")
    @AuditIgnore
    private Long totalAmount;

    @Column(name = "REMARKS", length = 500)
    private String remarks;

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
