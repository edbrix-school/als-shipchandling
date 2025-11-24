package com.alsharif.shipchandling.salesinvoice.dto;


import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreditDetailsDto {
    private Long creditPeriod;
    private Timestamp dueDate;
}
