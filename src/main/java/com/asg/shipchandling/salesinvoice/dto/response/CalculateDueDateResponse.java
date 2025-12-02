package com.asg.shipchandling.salesinvoice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CalculateDueDateResponse {
    private Timestamp dueDate;
    private Long dueDays;
    private Boolean success;
    private String message;
}

