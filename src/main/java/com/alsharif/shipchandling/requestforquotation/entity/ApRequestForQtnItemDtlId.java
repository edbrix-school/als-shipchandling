package com.alsharif.shipchandling.requestforquotation.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Composite Key Class
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApRequestForQtnItemDtlId implements java.io.Serializable {
    private Long transactionPoid;
    private Long detRowId;
}