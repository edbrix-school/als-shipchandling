package com.alsharif.shipchandling.deliverynote.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Timestamp;

@Entity
@Table(name = "SALES_DELIVERY_NOTE_HDR")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SequenceGenerator(name = "dn_trans_seq", sequenceName = "TRANSACTION_POID_SEQ", allocationSize = 1)
public class SalesDeliveryNoteHdr {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "dn_trans_seq")
    @Column(name = "TRANSACTION_POID", nullable = false)
    private Long transactionPoid;

    @Column(name = "DOC_REF", length = 25, unique = true)
    private String docRef;

    @Column(name = "TRANSACTION_DATE")
    private Timestamp transactionDate;

    @Column(name = "COMPANY_POID")
    private Long companyPoid;

    @Column(name = "CUSTOMER_POID", nullable = false) // done
    private Long customerPoid;

    @Column(name = "CURRENCY_CODE", length = 20) // done
    private String currencyCode;

    @Column(name = "CURRENCY_RATE") // done
    private Long currencyRate;

    @Column(name = "DELIVERY_STATUS", length = 20) // done
    private String deliveryStatus;

    @Column(name = "SALESMAN_POID") // done
    private Long salesmanPoid;

    @Column(name = "PAYMENT_MODE", length = 20) // done
    private String paymentMode;

    @Column(name = "DELIVERY_TERMS", length = 30) // done
    private String deliveryTerms;

    @Column(name = "LINE_POID") // done
    private Long linePoid;

    @Column(name = "VESSEL_POID", length = 50) // done
    private String vesselPoid;

    @Column(name = "VESSEL_NAME", length = 50) // done
    private String vesselName;

    @Column(name = "VOYAGE_REF", length = 20) // done
    private String voyageRef;

    @Column(name = "PORT_POID") // done
    private Long portPoid;

    @Column(name = "PORT_DESCRIPTION", length = 50) // done
    private String portDescription;

    @Column(name = "QTN_REF_NO", length = 25) // done
    private String qtnRefNo;

    @Column(name = "VESSEL_AGENT", length = 50) // done
    private String vesselAgent;

    @Column(name = "DELIVERY_TO_ADDRESS", length = 500) // done
    private String deliveryToAddress;

    @Column(name = "DESCRIPTION_PRINT_YN", length = 1)
    private String descriptionPrintYn = "Y";

    @Column(name = "PARTY_ADDRESS_DETAILS", length = 500)
    private String partyAddressDetails;

    @Column(name = "PRINT_DIVISION_POID")
    private Long printDivisionPoid;

    @Column(name = "PARTY_TYPE", length = 500)
    private String partyType;

    @Column(name = "PRINCIPAL_POID")
    private Long principalPoid;

    @Column(name = "TOTAL_DISCOUNT")
    private Long totalDiscount;

    @Column(name = "TOTAL_AMOUNT")
    private Long totalAmount;

    @Column(name = "REMARKS", length = 500) // done
    private String remarks;

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
