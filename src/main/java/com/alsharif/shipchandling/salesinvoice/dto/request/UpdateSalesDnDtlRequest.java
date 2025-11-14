package com.alsharif.shipchandling.salesinvoice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSalesDnDtlRequest {
    private Long detRowId;
    private Long dnPoidFk;
    private Long quotationPoidFk;
    private String remarks;
}
