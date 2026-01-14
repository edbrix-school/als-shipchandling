package com.asg.shipchandling.deliverynote.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SalesDeliveryNoteHdrDto {
    private Long transactionPoid;
    private String docRef;
    private Timestamp transactionDate;
    private Long companyPoid;
    private Long customerPoid;
    private String customerName; // From joined SALES_CUSTOMER_MASTER table
    private String currencyCode;
    private BigDecimal currencyRate;
    private String deliveryStatus;
    private Long salesmanPoid;
    private String paymentMode;
    private String deliveryTerms;
    private Long linePoid;
    private String vesselPoid;
    private String vesselName;
    private String voyageRef;
    private Long portPoid;
    private String portDescription;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String qtnRefNo;
    private String vesselAgent;
    private String deliveryToAddress;
    private String descriptionPrintYn;
    private String partyAddressDetails;
    private Long printDivisionPoid;
    private String partyType;
    private Long principalPoid;
    private Long totalDiscount;
    private Long totalAmount;
    private String remarks;
    private String deleted;
    private String createdBy;
    private Timestamp createdDate;
    private String lastmodifiedBy;
    private Timestamp lastmodifiedDate;

    // Detail tables (optional, included when includeDetails = true)
    private List<SalesDeliveryNoteItemDtlDto> itemDetails;
    
    // LOV Details (similar to StockMaster)
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
