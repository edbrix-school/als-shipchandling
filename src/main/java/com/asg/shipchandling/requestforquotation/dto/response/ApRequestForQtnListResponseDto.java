package com.asg.shipchandling.requestforquotation.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApRequestForQtnListResponseDto {
    @JsonProperty("TRANSACTION_POID")
    private Long transactionPoid;

    @JsonProperty("DOC_REF")
    private String docRef;

    @JsonProperty("TRANSACTION_DATE")
    private Timestamp transactionDate;

    @JsonProperty("DESCRIPTION")
    private String description;
    
    @JsonProperty("STATUS")
    private String status;

    @JsonProperty("TYPE")
    private String type;

    @JsonProperty("EXPECTED_DATE")
    private LocalDate expectedDate;

    @JsonProperty("REMARKS")
    private String remarks;

    @JsonProperty("DESCRIPTION_PRINT_YN")
    private String descriptionPrintYn;

    @JsonProperty("SALES_QTN_REF")
    private String salesQtnRef;

    @JsonProperty("SALES_INV_DOC_REF")
    private String salesInvDocRef;

    @JsonProperty("DELETED")
    private String deleted;

    @JsonProperty("CREATED_DATE")
    private Timestamp createdDate;

    @JsonProperty("LASTMODIFIED_DATE")
    private Timestamp lastmodifiedDate;
}

