package com.asg.shipchandling.deliverynote.dto;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateSalesDeliveryNoteRequest {
    
    private Long customerPoid;

    @Size(max = 20, message = "Currency code must not exceed 20 characters")
    private String currencyCode;

    @Size(max = 20, message = "Payment mode must not exceed 20 characters")
    private String paymentMode;

    @Size(max = 30, message = "Delivery terms must not exceed 30 characters")
    private String deliveryTerms;

    @Size(max = 20, message = "Delivery status must not exceed 20 characters")
    private String deliveryStatus;

    @Size(max = 500, message = "Delivery to address must not exceed 500 characters")
    private String deliveryToAddress;

    @Size(max = 1, message = "Description print flag must be Y or N")
    private String descriptionPrintYn = "Y";

    @NotNull(message = "Salesman is required")
    private Long salesmanPoid;

    @Size(max = 20, message = "Voyage reference must not exceed 20 characters")
    private String voyageRef;

    @Size(max = 50, message = "Vessel POID must not exceed 50 characters")
    private String vesselPoid;

    @Size(max = 50, message = "Vessel agent must not exceed 50 characters")
    private String vesselAgent;

    private Long portPoid;

    @Size(max = 50, message = "Port description must not exceed 50 characters")
    private String portDescription;

    @Size(max = 25, message = "Quotation reference must not exceed 25 characters")
    private String qtnRefNo; // Optional - can be set to link to a quotation

    private Long printDivisionPoid;

    @Size(max = 100, message = "Party type must not exceed 100 characters")
    private String partyType;

    private Long principalPoid;

    @NotNull(message = "Currency rate is required")
    private Long currencyRate;

    @Size(max = 500, message = "Remarks must not exceed 500 characters")
    private String remarks;

    @Size(max = 1000, message = "Party address details must not exceed 1000 characters")
    private String partyAddressDetails;
}
