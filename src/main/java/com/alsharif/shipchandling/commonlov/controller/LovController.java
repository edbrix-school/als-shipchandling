package com.alsharif.shipchandling.commonlov.controller;

import com.alsharif.shipchandling.commonlov.dto.LovResponse;
import com.alsharif.shipchandling.commonlov.service.LovServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.alsharif.shipchandling.common.ApiResponse.success;

@RestController
@RequestMapping("/api/shipchandling/lov")
@Tag(name = "Common LOV", description = "Common List of Values API")
public class LovController {

    @Autowired
    LovServiceImpl lovService;

    @Operation(summary = "Get LOV List", description = "Single unified endpoint that handles all LOV requests.\n\nlovName:\nRFQ_STATUS - RFQ Status dropdown (read-only)\nDIVISION - Division dropdown\nSALES_QTN_REF - Sales Quotation Reference LOV (read-only)\nRFQ_CONFIRMED_SUPPLIER - Confirmed Suppliers LOV (RFQ-specific)\nSTOCK_MASTER - Stock Master LOV (required for item details)\nSTOCK_UNIT - Stock Unit LOV (required for item details)\nSUPPLIER_MASTER - Supplier Master LOV (for item details and supplier details)\nINPUT_TAX_MASTER - Input Tax Code LOV (for item details)")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getLovList(
            @Parameter(description = "One of the LOV names below", required = true) @RequestParam("lovName") String lovName,

            @Parameter(description = "Document context POID (may be TransactionPoid for RFQ)") @RequestParam(value = "docKeyPoid", required = false) Long docKeyPoid,

            @Parameter(description = "Additional filter value") @RequestParam(value = "filterValue", required = false) String filterValue) {
        LovResponse lovResponse = lovService.getLovList(lovName, docKeyPoid, filterValue);
        return success("Task fetched successfully", lovResponse);

    }

}
