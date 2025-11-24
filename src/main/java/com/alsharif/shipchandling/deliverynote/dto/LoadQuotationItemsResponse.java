package com.alsharif.shipchandling.deliverynote.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoadQuotationItemsResponse {
    private Boolean success;
    private String message;
    private Integer itemsLoaded;
    private List<QuotationItemDto> items;
}
