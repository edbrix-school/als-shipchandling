package com.alsharif.shipchandling.deliverynote.dto;

import com.alsharif.shipchandling.deliverynote.entity.SalesDeliveryNoteHdr;

/**
 * Projection interface for native query results that include joined table columns
 */
public interface SalesDeliveryNoteWithCustomerProjection {
    SalesDeliveryNoteHdr getDeliveryNote();
    String getCustomerName();
}


