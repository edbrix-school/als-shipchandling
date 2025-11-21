package com.alsharif.shipchandling.salesinvoice.dto.request;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;



@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoadQuotationItemsRequest {
    private String qtnPoid;
    private Long incentiveAmt;
    private Long incentiveAmt2;
    private Long incentiveAmt3;
}
