package com.asg.shipchandling.salesinvoice.entity;

import com.asg.common.lib.annotation.AuditIgnore;
import com.asg.common.lib.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "AR_SCH_SALES_INVOICE_HDR")
@Data
@NoArgsConstructor
@AllArgsConstructor
// @SequenceGenerator(name = "sch_inv_trans_seq", sequenceName =
// "TRANSACTION_POID_SEQ", allocationSize = 1)
public class SalesInvoiceHdr extends BaseEntity {

    @Id
    // @GeneratedValue(strategy = GenerationType.SEQUENCE, generator =
    // "sch_inv_trans_seq")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TRANSACTION_POID", nullable = false)
    @AuditIgnore
    private Long transactionPoid;

    @Column(name = "DOC_REF", length = 30, unique = true)
    @AuditIgnore
    private String docRef;

    @Column(name = "TRANSACTION_DATE")
    @AuditIgnore
    private LocalDateTime transactionDate;

    @Column(name = "GROUP_POID", nullable = false)
    @AuditIgnore
    private Long groupPoid;

    @Column(name = "COMPANY_POID", nullable = false)
    @AuditIgnore
    private Long companyPoid;

    @Column(name = "PARTY_TYPE", length = 100)
    private String partyType; // CUSTOMER or PRINCIPAL

    @Column(name = "CUSTOMER_POID")
    private Long customerPoid;

    @Column(name = "PRINCIPAL_POID")
    private Long principalPoid;

    @Column(name = "CUSTOMER_ADDR_POID")
    private Long customerAddrPoid;

    @Column(name = "CURRENCY_CODE", length = 10)
    private String currencyCode;

    @Column(name = "CURRENCY_RATE")
    private BigDecimal currencyRate;

    @Column(name = "INV_AMOUNT")
    private BigDecimal invAmount;

    @Column(name = "CREDIT_DAYS")
    private Long creditDays;

    @Column(name = "DUE_DATE")
    private LocalDateTime dueDate;

    @Column(name = "QTN_POID")
    private Long qtnPoid;

    @Column(name = "STATUS", length = 20)
    private String status;

    @Column(name = "INV_STATUS", length = 20)
    private String invStatus = "IN_PROGRESS";

    @Column(name = "DISCOUNT_PERCENT")
    private BigDecimal discountPercent;

    @Column(name = "DISCOUNT_AMT")
    private BigDecimal discountAmt;

    @Column(name = "INV_DISCOUNT")
    private BigDecimal invDiscount;

    @Column(name = "INCENTIVE_PERCENT")
    private BigDecimal incentivePercent;

    @Column(name = "INCENTIVE_AMT")
    private BigDecimal incentiveAmt;

    @Column(name = "INCENTIVE_TO", length = 30)
    private String incentiveTo;

    @Column(name = "INCENTIVE_PERCENT2")
    private BigDecimal incentivePercent2;

    @Column(name = "INCENTIVE_AMT2")
    private BigDecimal incentiveAmt2;

    @Column(name = "INCENTIVE_TO2", length = 30)
    private String incentiveTo2;

    @Column(name = "INCENTIVE_PERCENT3")
    private BigDecimal incentivePercent3;

    @Column(name = "INCENTIVE_AMT3")
    private BigDecimal incentiveAmt3;

    @Column(name = "INCENTIVE_TO3", length = 30)
    private String incentiveTo3;

    @Column(name = "TOTAL_GP_AMT")
    private BigDecimal totalGpAmt;

    @Column(name = "TOTAL_GP_PERCENT")
    private BigDecimal totalGpPercent;

    @Column(name = "TOTAL_COST")
    private BigDecimal totalCost;

    @Column(name = "PAYMENT_MODE", length = 20)
    private String paymentMode;

    @Column(name = "DATA_LOAD_TYPE", length = 10)
    @AuditIgnore
    private String dataLoadType;

    @Column(name = "VESSEL_NAME", length = 100)
    private String vesselName;

    @Column(name = "PORT_NAME", length = 100)
    private String portName;

    @Column(name = "DESCRIPTION_PRINT_YN", length = 1)
    @AuditIgnore
    private String descriptionPrintYn;

    @Column(name = "DELIVERY_TO_ADDRESS", length = 200)
    private String deliveryToAddress;

    @Column(name = "DETAILS", length = 200)
    private String details;

    @Column(name = "REMARKS", length = 500)
    private String remarks;

    @Column(name = "LPO_DETAILS", length = 100)
    private String lpoDetails;

    @Column(name = "LPO_NUMBER", length = 300)
    private String lpoNumber;

    @Column(name = "CONTRACT_REF_NUMBER", length = 500)
    private String contractRefNumber;

    @Column(name = "COST_REF_NUMBER", length = 500)
    private String costRefNumber;

    @Column(name = "FDA_REF", length = 100)
    private String fdaRef;

    @Column(name = "PRINT_DIVISION_POID")
    private Long printDivisionPoid;

    @Column(name = "DN_POID", length = 50)
    private String dnPoid;

    @Column(name = "VERIFIED", length = 1)
    private String verified = "N";

    @Column(name = "PJ_LOAD_STATUS", length = 1)
    @AuditIgnore
    private String pjLoadStatus;

    @Column(name = "POST_WITH_SPL_RIGHTS", length = 1)
    @AuditIgnore
    private String postWithSplRights;

    @Column(name = "AUTHORIZED_ID", length = 100)
    private String authorizedId;

    @Column(name = "DELETED", length = 1)
    @AuditIgnore
    private String deleted = "N";

}