package com.asg.shipchandling.salesquotationsch.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

import com.asg.shipchandling.salesquotationsch.dto.response.AddressDetailsResponse;

import com.asg.shipchandling.salesquotationsch.dto.request.CreateSalesQuotationSchItemDtlRequest;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSalesQuotationSchRequest {

    private Timestamp transactionDate;

    private BigDecimal customerPoid;

    private Long addressPoid;

    private boolean newAddressYN;

    @Size(max = 20, message = "Currency code must not exceed 20 characters")
    private String currencyCode;

    private BigDecimal currencyRate;

    @Size(max = 20, message = "Quotation status must not exceed 20 characters")
    private String quotationStatus;

    private Long salesmanPoid;

    private Timestamp validityFromDate;

    private Timestamp validityToDate;

    @Size(max = 20, message = "Payment mode must not exceed 20 characters")
    private String paymentMode;

    @Size(max = 30, message = "Delivery terms must not exceed 30 characters")
    private String deliveryTerms;

    private Long linePoid;

    @Size(max = 50, message = "Vessel POID must not exceed 50 characters")
    private String vesselPoid;

    @Size(max = 20, message = "Voyage reference must not exceed 20 characters")
    private String voyageRef;

    @Size(max = 500, message = "Remarks must not exceed 500 characters")
    private String remarks;

    private Long totalDiscount;

    @Size(max = 20, message = "Action status must not exceed 20 characters")
    private String actionStatus;

    private Timestamp actionDueDate;

    private Long enquiryRefNumber;

    @Size(max = 20, message = "Lost reason must not exceed 20 characters")
    private String lostReason;

    private Long businessPromotionValue;

    private Long percentage;

    @Size(max = 100, message = "Details must not exceed 100 characters")
    private String details;

    private Timestamp expectedDeliveryDate;

    @Size(max = 50, message = "Vessel agent must not exceed 50 characters")
    private String vesselAgent;

    private Long portPoid;

    @Size(max = 20, message = "Quoted rate must not exceed 20 characters")
    private String quotedRate;

    @Size(max = 25, message = "RFQ reference number must not exceed 25 characters")
    private String rfqRefNo;

    private Long percentageDisc;

    @Size(max = 50, message = "Vessel name must not exceed 50 characters")
    private String vesselName;

    @Size(max = 50, message = "Port description must not exceed 50 characters")
    private String portDescription;

    @Size(max = 20, message = "Customer reference must not exceed 20 characters")
    private String customerRef;

    @Size(max = 500, message = "Delivery to address must not exceed 500 characters")
    private String deliveryToAddress;

    @Size(max = 1, message = "Select all detail must not exceed 1 character")
    private String selectAllDtl;

    @Size(max = 1, message = "Total amount print YN must not exceed 1 character")
    private String totAmtPrintYn;

    @Size(max = 1, message = "Description print YN must not exceed 1 character")
    private String descriptionPrintYn;

    @Size(max = 1, message = "Advance detail must not exceed 1 character")
    private String advanceDetail;

    private Long mtaInvPoid;

    private Long salesInvPoid;

    @Size(max = 50, message = "Sales invoice document reference must not exceed 50 characters")
    private String salesInvDocRef;

    @Size(max = 1000, message = "Party address details must not exceed 1000 characters")
    private String partyAddressDetails;

    private AddressDetailsResponse addressDetails;
    // Detail tables
    private List<CreateSalesQuotationSchItemDtlRequest> itemDetails;
}
