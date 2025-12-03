package com.asg.shipchandling.stockunitmaster.dto;

import java.util.List;

public record FilterRequestDto(
        String operator,       // "AND" or "OR", default OR
        String isDeleted,   // true = only deleted, false = only active
        List<FilterDto> filters
) {}
