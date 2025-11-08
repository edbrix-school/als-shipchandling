package com.alsharif.shipchandling.salesinvoice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoadCreditDetailsResponse {
    private Boolean success;
    private String message;
    private Long creditDays;
    private Timestamp dueDate;
    private String paymentMode;
    private Long creditLimit;
    private Long outstandingAmount;
}
