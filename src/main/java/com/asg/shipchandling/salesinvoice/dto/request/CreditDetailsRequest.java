package com.asg.shipchandling.salesinvoice.dto.request;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreditDetailsRequest {
    private LocalDate docDate;
    private String partyType; //customerPoid/principalPoid
    private Long partyPoid;
}
