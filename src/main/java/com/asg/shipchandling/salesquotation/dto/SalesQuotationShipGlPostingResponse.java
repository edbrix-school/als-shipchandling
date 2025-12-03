package com.asg.shipchandling.salesquotation.dto;

import java.util.List;
import java.util.Map;

public record SalesQuotationShipGlPostingResponse(
        List<Map<String, Object>> glHeaders,
        List<Map<String, Object>> glLines,
        List<Map<String, Object>> taxBreakup,
        List<Map<String, Object>> additionalInfo
) {
}

