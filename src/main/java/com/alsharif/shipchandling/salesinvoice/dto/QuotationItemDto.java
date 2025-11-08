package com.alsharif.shipchandling.salesinvoice.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;




@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuotationItemDto {
    private Long stockPoid;
    private Long stockUnitPoid;
    private Long quantity;
    private Long price;
    private Long discount;
    private Long baseAmt;
    private Long taxPoid;
    private Long incentiveAmt;
    private Long incentiveAmt2;
    private Long incentiveAmt3;
    private Long discountAmt;
    private Long gpAmt;
}
