package com.asg.shipchandling.deliverynote.dto;

import com.asg.shipchandling.deliverynote.entity.SalesDeliveryNoteHdr;

/**
 * Projection interface for native query results that include joined table columns
 */
public interface SalesDeliveryNoteWithCustomerProjection {
    SalesDeliveryNoteHdr getDeliveryNote();
    String getCustomerName();
}


