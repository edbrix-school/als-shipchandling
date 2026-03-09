package com.asg.shipchandling.salesinvoice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoadCreditDetailsResponse {
    private Boolean success;
    private String message;
    private Long creditDays;
    private LocalDateTime dueDate;
    private String paymentMode;
    private Long creditLimit;
    private Long outstandingAmount;
}
