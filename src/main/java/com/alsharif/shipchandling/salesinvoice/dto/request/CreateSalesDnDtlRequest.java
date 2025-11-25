package com.alsharif.shipchandling.salesinvoice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateSalesDnDtlRequest {
    private Long dnPoidFk;
    private Long quotationPoidFk;
    private String remarks;
}
