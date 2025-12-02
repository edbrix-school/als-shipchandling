package com.asg.shipchandling.salesinvoice.dto.response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoadDeliveryNoteResponse {
    private Boolean success;
    private String message;
    private Integer itemsLoaded;
}
