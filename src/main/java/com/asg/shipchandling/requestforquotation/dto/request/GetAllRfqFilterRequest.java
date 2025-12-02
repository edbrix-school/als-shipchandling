package com.asg.shipchandling.requestforquotation.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetAllRfqFilterRequest {
    private String from;
    private String to;
    private String operator; // "AND" or "OR"
    private String isDeleted; // "Y" or "N"
    private List<FilterItem> filters;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FilterItem {
        private String searchField; // e.g., "DOC_REF", "TASK_DESCRIPTION", "SALES_QTN_REF"
        private String searchValue; // text search value
    }
}

