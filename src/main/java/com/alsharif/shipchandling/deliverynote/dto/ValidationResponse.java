package com.alsharif.shipchandling.deliverynote.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public
class ValidationResponse {
    private Boolean isUnique;
    private String message;
}
