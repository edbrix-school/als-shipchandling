package com.alsharif.shipchandling.salesinvoice.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoadCostBookingsResponse {
    private Boolean success;
    private String message;
    private List<ArSchSalesInvCostbkdDtlDto> costBookings;
    private Integer count;
}