package com.alsharif.shipchandling.deliverynote.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidateCustomerChangeResponse {
    private Boolean success;
    private Boolean canChange;
    private String message;
}