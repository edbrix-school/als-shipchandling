package com.alsharif.shipchandling.salesquotationsch.dto;

import java.util.List;

public record FilterRequestDto(
        String operator,       // "AND" or "OR", default OR
        String isDeleted,   // "Y" = only deleted, "N" = only active
        List<FilterDto> filters
) {}

