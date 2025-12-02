package com.alsharif.shipchandling.salesquotationsch.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesQuotationSchHdrDto {
    private Long transactionPoid;
    private Timestamp transactionDate;
    private Long companyPoid;
    private String docRef;
    private Long customerPoid;
    private Long addressPoid;
    private String currencyCode;
    private Long currencyRate;
    private String quotationStatus;
    private Long salesmanPoid;
    private Timestamp validityFromDate;
    private Timestamp validityToDate;
    private String paymentMode;
    private String deliveryTerms;
    private Long linePoid;
    private String vesselPoid;
    private String voyageRef;
    private String remarks;
    private Long totalDiscount;
    private Long totalAmount;
    private String actionStatus;
    private Timestamp actionDueDate;
    private Long enquiryRefNumber;
    private String deleted;
    private String createdBy;
    private Timestamp createdDate;
    private String lastmodifiedBy;
    private Timestamp lastmodifiedDate;
    private String lostReason;
    private Long businessPromotionValue;
    private Long percentage;
    private String details;
    private Timestamp expectedDeliveryDate;
    private String vesselAgent;
    private Long portPoid;
    private String quotedRate;
    private String rfqRefNo;
    private Long percentageDisc;
    private Long totalGpAmt;
    private Long totalGpPercentage;
    private String vesselName;
    private String portDescription;
    private String customerRef;
    private String deliveryToAddress;
    private String selectAllDtl;
    private String totAmtPrintYn;
    private String descriptionPrintYn;
    private String advanceDetail;
    private Long mtaInvPoid;
    private Long salesInvPoid;
    private String salesInvDocRef;
    private Long totalTax;
    private String partyAddressDetails;
    
    private List<SalesQuotationSchItemDtlDto> itemDetails;
    
    // LOV Details (similar to Delivery Note)
    private LovDetailDto customerDetails;
    private LovDetailDto salesmanDetails;
    private LovDetailDto lineDetails;
    private LovDetailDto portDetails;
    private LovDetailDto vesselDetails;
    private LovDetailDto printDivisionDetails;
    private LovDetailDto principalDetails;
    
    // Inner class for LOV details
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LovDetailDto {
        private Long poid;
        private String code;
        private String description;
    }
}
