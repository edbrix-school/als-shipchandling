package com.asg.shipchandling.salesquotationsch.entity;

import com.asg.common.lib.annotation.AuditIgnore;
import com.asg.common.lib.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;


@Entity
@Table(name = "SALES_QUOTATION_HDR")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesQuotationSchHdr extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TRANSACTION_POID", nullable = false)
    @AuditIgnore
    private Long transactionPoid;

    @Column(name = "TRANSACTION_DATE")
    @AuditIgnore
    private LocalDateTime transactionDate;

    @Column(name = "COMPANY_POID")
    @AuditIgnore
    private Long companyPoid;

    @Column(name = "DOC_REF", length = 25, unique = true, insertable = false, updatable = false)
    @Generated(GenerationTime.INSERT)
    @AuditIgnore
    private String docRef;

    @Column(name = "CUSTOMER_POID")
    private BigDecimal customerPoid;

    @Column(name = "ADDRESS_POID")
    private Long addressPoid;

    @Column(name = "CURRENCY_CODE", length = 20)
    private String currencyCode;

    @Column(name = "CURRENCY_RATE")
    private BigDecimal currencyRate;

    @Column(name = "QUOTATION_STATUS", length = 20)
    private String quotationStatus;

    @Column(name = "SALESMAN_POID", nullable = false)
    private Long salesmanPoid;

    @Column(name = "VALIDITY_FROM_DATE")
    private LocalDate validityFromDate;

    @Column(name = "VALIDITY_TO_DATE")
    private LocalDate validityToDate;


    @Column(name = "PAYMENT_MODE", length = 20)
    private String paymentMode;

    @Column(name = "DELIVERY_TERMS", length = 30)
    @AuditIgnore
    private String deliveryTerms;

    @Column(name = "LINE_POID")
    @AuditIgnore
    private Long linePoid;

    @Column(name = "VESSEL_POID", length = 50)
    private String vesselPoid;

    @Column(name = "VOYAGE_REF", length = 20)
    @AuditIgnore
    private String voyageRef;

    @Column(name = "REMARKS", length = 500)
    private String remarks;

    @Column(name = "TOTAL_DISCOUNT")
    private BigDecimal totalDiscount;

    @Column(name = "TOTAL_AMOUNT")
    private Long totalAmount;

    @Column(name = "ACTION_STATUS", length = 20)
    @AuditIgnore
    private String actionStatus;

    @Column(name = "ACTION_DUE_DATE")
    @AuditIgnore
    private LocalDate actionDueDate;


    @Column(name = "ENQUIRY_REF_NUMBER")
    @AuditIgnore
    private Long enquiryRefNumber;

    @Column(name = "DELETED", length = 1)
    @AuditIgnore
    private String deleted;

    @Column(name = "LOST_REASON", length = 20)
    @AuditIgnore
    private String lostReason;

    @Column(name = "BUSINESS_PROMOTION_VALUE")
    @AuditIgnore
    private Long businessPromotionValue;

    @Column(name = "PERCENTAGE")
    @AuditIgnore
    private Long percentage;

    @Column(name = "DETAILS", length = 100)
    private String details;

    @Column(name = "EXPEACTED_DELIVERY_DATE")
    @AuditIgnore
    private LocalDate expectedDeliveryDate;


    @Column(name = "VESSEL_AGENT", length = 50)
    private String vesselAgent;

    @Column(name = "PORT_POID")
    private Long portPoid;

    @Column(name = "QUOTED_RATE", length = 20)
    @AuditIgnore
    private String quotedRate;

    @Column(name = "RFQ_REF_NO", length = 25)
    @AuditIgnore
    private String rfqRefNo;

    @Column(name = "PERCENTAGE_DISC")
    private BigDecimal percentageDisc;

    @Column(name = "TOTAL_GP_AMT")
    private Long totalGpAmt;

    @Column(name = "TOTAL_GP_PERCENTAGE")
    private Long totalGpPercentage;

    @Column(name = "VESSEL_NAME", length = 50)
    private String vesselName;

    @Column(name = "PORT_DESCRIPTION", length = 50)
    private String portDescription;

    @Column(name = "CUSTOMER_REF", length = 20)
    private String customerRef;

    @Column(name = "DELIVERY_TO_ADDRESS", length = 500)
    private String deliveryToAddress;

    @Column(name = "SELECT_ALL_DTL", length = 1)
    @AuditIgnore
    private String selectAllDtl = "N";

    @Column(name = "TOT_AMT_PRINT_YN", length = 1)
    private String totAmtPrintYn = "Y";

    @Column(name = "DESCRIPTION_PRINT_YN", length = 1)
    private String descriptionPrintYn = "Y";

    @Column(name = "ADVANCE_DETAIL", length = 1)
    @AuditIgnore
    private String advanceDetail = "Y";

    @Column(name = "MTA_INV_POID")
    @AuditIgnore
    private Long mtaInvPoid;

    @Column(name = "SALES_INV_POID")
    private Long salesInvPoid;

    @Column(name = "SALES_INV_DOC_REF", length = 50)
    @AuditIgnore
    private String salesInvDocRef;

    @Column(name = "TOTAL_TAX")
    @AuditIgnore
    private Long totalTax;

    @Column(name = "PARTY_ADDRESS_DETAILS", length = 1000)
    @AuditIgnore
    private String partyAddressDetails;
}
