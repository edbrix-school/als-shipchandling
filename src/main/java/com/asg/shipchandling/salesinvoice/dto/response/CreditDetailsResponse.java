package com.asg.shipchandling.salesinvoice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreditDetailsResponse {
    private LocalDate dueDate;
    private Long creditDays;
    private Boolean success;
    private String message;
}