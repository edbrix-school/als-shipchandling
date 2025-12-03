package com.asg.shipchandling.salesinvoice.dto.request;

import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreditDetailsRequest {
    private String docId;
    private Long docKeyPoid;
    private Timestamp docDate;
    private String partyType;
    private Long partyPoid;
}
