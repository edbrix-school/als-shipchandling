package com.asg.shipchandling.salesinvoice.dto.response;

import java.util.List;

import com.asg.shipchandling.salesinvoice.dto.CreditDetailsDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreditDetailsResponse {
    private Boolean success;
    private String message;
    private List<CreditDetailsDto> creditDetails;
}