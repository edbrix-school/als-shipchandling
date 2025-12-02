package com.asg.shipchandling.salesinvoice.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesDnDtlId implements java.io.Serializable {
    private Long transactionPoid;
    private Long detRowId;
}
