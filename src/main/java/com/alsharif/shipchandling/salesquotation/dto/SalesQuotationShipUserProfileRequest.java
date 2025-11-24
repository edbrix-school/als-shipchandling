package com.alsharif.shipchandling.salesquotation.dto;

import java.math.BigDecimal;

public record SalesQuotationShipUserProfileRequest(
        BigDecimal userPoid,
        String settingName,
        String settingValue
) {
}

