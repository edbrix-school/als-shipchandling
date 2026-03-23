package com.asg.shipchandling.salesinvoice.dto;




import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreditDetailsDto {
    private Long creditPeriod;
    private LocalDateTime dueDate;
}
