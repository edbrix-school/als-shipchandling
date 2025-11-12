package com.alsharif.shipchandling.requestforquotation.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateApRequestForQtnSupDtlRequest {
    private Long supplierPoid;
    private String remarks;
}