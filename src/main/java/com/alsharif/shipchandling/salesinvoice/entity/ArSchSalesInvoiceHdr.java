package com.alsharif.shipchandling.salesinvoice.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;


import java.sql.Timestamp;

@Entity
@Table(name = "AR_SCH_SALES_INVOICE_HDR")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SequenceGenerator(name = "sch_inv_trans_seq", sequenceName = "TRANSACTION_POID_SEQ", allocationSize = 1)
public class ArSchSalesInvoiceHdr {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sch_inv_trans_seq")
    @Column(name = "TRANSACTION_POID", nullable = false)
    private Long transactionPoid;

    @Column(name = "DOC_REF", length = 30, unique = true)
    private String docRef;

    @Column(name = "TRANSACTION_DATE")
    private Timestamp transactionDate;

    @Column(name = "GROUP_POID", nullable = false)
    private Long groupPoid;

    @Column(name = "COMPANY_POID", nullable = false)
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
    private Long currencyRate;

    @Column(name = "INV_AMOUNT")
    private Long invAmount;

    @Column(name = "CREDIT_DAYS")
    private Long creditDays;

    @Column(name = "DUE_DATE")
    private Timestamp dueDate;

    @Column(name = "QTN_POID")
    private Long qtnPoid;

    @Column(name = "STATUS", length = 20)
    private String status;

    @Column(name = "INV_STATUS", length = 20)
    private String invStatus = "IN_PROGRESS";

    @Column(name = "DISCOUNT_PERCENT")
    private Long discountPercent;

    @Column(name = "DISCOUNT_AMT")
    private Long discountAmt;

    @Column(name = "INV_DISCOUNT")
    private Long invDiscount;

    @Column(name = "INCENTIVE_PERCENT")
    private Long incentivePercent;

    @Column(name = "INCENTIVE_AMT")
    private Long incentiveAmt;

    @Column(name = "INCENTIVE_TO", length = 30)
    private String incentiveTo;

    @Column(name = "INCENTIVE_PERCENT2")
    private Long incentivePercent2;

    @Column(name = "INCENTIVE_AMT2")
    private Long incentiveAmt2;

    @Column(name = "INCENTIVE_TO2", length = 30)
    private String incentiveTo2;

    @Column(name = "INCENTIVE_PERCENT3")
    private Long incentivePercent3;

    @Column(name = "INCENTIVE_AMT3")
    private Long incentiveAmt3;

    @Column(name = "INCENTIVE_TO3", length = 30)
    private String incentiveTo3;

    @Column(name = "TOTAL_GP_AMT")
    private Long totalGpAmt;

    @Column(name = "TOTAL_GP_PERCENT")
    private Long totalGpPercent;

    @Column(name = "TOTAL_COST")
    private Long totalCost;

    @Column(name = "PAYMENT_MODE", length = 20)
    private String paymentMode;

    @Column(name = "DATA_LOAD_TYPE", length = 10)
    private String dataLoadType;

    @Column(name = "VESSEL_NAME", length = 100)
    private String vesselName;

    @Column(name = "PORT_NAME", length = 100)
    private String portName;

    @Column(name = "DESCRIPTION_PRINT_YN", length = 1)
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
    private String pjLoadStatus;

    @Column(name = "POST_WITH_SPL_RIGHTS", length = 1)
    private String postWithSplRights;

    @Column(name = "AUTHORIZED_ID", length = 100)
    private String authorizedId;

    @Column(name = "DELETED", length = 1)
    private String deleted = "N";

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