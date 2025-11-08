package com.alsharif.shipchandling.salesinvoice.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;



@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoadQuotationRequest {
    private Long qtnPoid;
    private Long incentiveAmt;
    private Long incentiveAmt2;
    private Long incentiveAmt3;
}
