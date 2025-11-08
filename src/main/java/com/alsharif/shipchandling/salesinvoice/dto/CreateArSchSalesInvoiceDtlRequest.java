package com.alsharif.shipchandling.salesinvoice.dto;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;



@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateArSchSalesInvoiceDtlRequest {
    private Long stockPoid;
    private Long stockUnitPoid;
    private Long quantity;
    private Long price;
    private Long discount;
    private Long baseAmt;
    private Long taxPoid;
    private Long costCenterPoid;
    private String remarks;
}
