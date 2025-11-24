package com.alsharif.shipchandling.salesinvoice.dto.response;

import java.util.List;

import com.alsharif.shipchandling.salesinvoice.dto.SalesInvCostbkdDtlDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoadCostBookingsResponse {
    private Boolean success;
    private String message;
    private List<SalesInvCostbkdDtlDto> costBookings;
    private Integer count;
}