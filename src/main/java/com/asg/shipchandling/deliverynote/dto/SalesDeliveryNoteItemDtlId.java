package com.asg.shipchandling.deliverynote.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Composite Key Class
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesDeliveryNoteItemDtlId implements java.io.Serializable {
    private Long transactionPoid;
    private Long detRowId;
}
