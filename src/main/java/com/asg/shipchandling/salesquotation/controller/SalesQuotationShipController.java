package com.asg.shipchandling.salesquotation.controller;

import com.asg.common.lib.annotation.AllowedAction;
import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.enums.LogDetailsEnum;
import com.asg.common.lib.enums.UserRolesRightsEnum;
import com.asg.common.lib.service.LoggingService;
import com.asg.shipchandling.salesquotation.dto.*;
import com.asg.shipchandling.salesquotation.service.SalesQuotationShipService;
import com.asg.shipchandling.salesquotation.dto.*;
import com.asg.shipchandling.salesquotation.service.SalesQuotationShipService.ChargeTaxResponse;
import com.asg.shipchandling.salesquotation.service.SalesQuotationShipService.CustomerContactResponse;
import com.asg.common.lib.security.util.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static com.asg.common.lib.dto.response.ApiResponse.error;

@RestController
@RequestMapping(path = "/v0/sales-quotations", produces = MediaType.APPLICATION_JSON_VALUE)
public class SalesQuotationShipController {

    private static final Logger log = LoggerFactory.getLogger(SalesQuotationShipController.class);
    private final SalesQuotationShipService service;
    private final LoggingService loggingService;


    public SalesQuotationShipController(SalesQuotationShipService service,LoggingService loggingService) {
        this.service = service;
        this.loggingService = loggingService;
    }

    @Operation(
            summary = "Create sales quotation",
            description = "Creates a new Sales Quotation for Ship Chandling operations. " +
                    "Handles default values, auto-population of salesman, validation, and business logic. " +
                    "DocRef is auto-generated and retrieved after insert. " +
                    "Company and division are auto-assigned based on buying vs selling total comparison.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully created sales quotation",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = SalesQuotationShipDetailDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input parameters or validation error",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public SalesQuotationShipDetailDto create(
            @Parameter(description = "Sales quotation creation request. CompanyPoid and UserId/UserPoid are required.", required = true)
            @RequestBody SalesQuotationShipCommand command) {
        log.info("create sales quotation started for companyPoid={} userId={}", 
                command.getCompanyPoid(), command.getUserId());
        SalesQuotationShipDetailDto dto = service.createQuotation(command);
        log.info("create sales quotation completed for transactionPoid={} docRef={}", 
                dto != null ? dto.getTransactionPoid() : null, 
                dto != null ? dto.getDocRef() : null);
        return dto;
    }

    @Operation(
            summary = "Update sales quotation",
            description = "Updates an existing Sales Quotation document. Handles audit field updates, validation, and status-based editing restrictions. " +
                    "Quotations in 'PROCESSING' status can be edited. Quotations in 'CONFIRMED' or 'LOST' status may have restricted editing. " +
                    "DocRef is read-only and cannot be updated. CreatedBy and CreatedDate are preserved, while LastModifiedBy and LastModifiedDate are updated.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully updated sales quotation",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = SalesQuotationShipDetailDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input parameters or validation error",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Sales quotation not found or does not belong to the specified company",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden - Quotation status does not allow editing",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PutMapping(path = "/{transactionPoid}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public SalesQuotationShipDetailDto update(
            @Parameter(description = "Transaction POID of the sales quotation to update", required = true)
            @PathVariable("transactionPoid") BigDecimal transactionPoid,
            @Parameter(description = "Sales quotation update request. CompanyPoid and UserId/UserPoid are required.", required = true)
            @RequestBody SalesQuotationShipCommand command) {
        log.info("update sales quotation started for transactionPoid={} companyPoid={}", transactionPoid, command.getCompanyPoid());
        SalesQuotationShipDetailDto dto = service.updateQuotation(transactionPoid, command);
        log.info("update sales quotation completed for transactionPoid={} docRef={}", 
                transactionPoid, dto != null ? dto.getDocRef() : null);
        return dto;
    }

    @Operation(
            summary = "Delete sales quotation",
            description = "Deletes a Sales Quotation by performing a soft delete (sets Deleted='Y'). " +
                    "Checks for dependencies (e.g., Sales Invoices) before allowing deletion. " +
                    "Returns a response indicating whether deletion was successful or blocked due to dependencies. " +
                    "Quotations that have been converted to Sales Invoice cannot be deleted. " +
                    "The quotation must belong to the specified company for data isolation.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Delete operation completed. Response indicates success (canDelete=true) or blocked due to dependencies (canDelete=false).",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = SalesQuotationShipDeleteResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input parameters or validation error",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Sales quotation not found or does not belong to the specified company",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @DeleteMapping(path = "/{transactionPoid}")
    public ResponseEntity<SalesQuotationShipDeleteResponse> delete(
            @Parameter(description = "Transaction POID of the sales quotation to delete", required = true)
            @PathVariable("transactionPoid") BigDecimal transactionPoid,
            @Valid @RequestBody(required = false) DeleteReasonDto deleteReasonDto
    ) {
        BigDecimal companyId = BigDecimal.valueOf(UserContext.getCompanyPoid());
        log.info("delete sales quotation started for transactionPoid={} companyId={} userId={}", 
                transactionPoid, companyId, UserContext.getUserId());
        SalesQuotationShipDeleteResponse response = service.deleteQuotation(transactionPoid, companyId, UserContext.getUserId(),deleteReasonDto);
        log.info("delete sales quotation completed for transactionPoid={} canDelete={}", 
                transactionPoid, response.isCanDelete());
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "List sales quotations",
            description = "Returns a paginated list of Sales Quotations with filtering and search capabilities. " +
                    "Supports filtering by company, customer, salesman, line, status, quotation type, and date ranges. " +
                    "Supports search across DocRef, CustomerName, and Description. " +
                    "Implements line access control based on user's line access rights. " +
                    "Company filtering is mandatory for data isolation.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully retrieved sales quotations list",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = SalesQuotationShipListResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input parameters or validation error",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping
    public SalesQuotationShipListResponse search(
            @Parameter(description = "Customer POID (optional)")
            @RequestParam(value = "customerId", required = false) BigDecimal customerId,
            @Parameter(description = "Salesman POID (optional)")
            @RequestParam(value = "salesmanId", required = false) BigDecimal salesmanId,
            @Parameter(description = "Line POID (optional)")
            @RequestParam(value = "lineId", required = false) BigDecimal lineId,
            @Parameter(description = "Quotation status (optional, e.g., PROCESSING, CONFIRMED, LOST)")
            @RequestParam(value = "status", required = false) String status,
            @Parameter(description = "Quotation type (optional, e.g., SHIPPING, GENERAL)")
            @RequestParam(value = "qtnType", required = false) String qtnType,
            @Parameter(description = "Document reference (optional, partial match)")
            @RequestParam(value = "docRef", required = false) String docRef,
            @Parameter(description = "Search text (optional, searches across DocRef, CustomerName, Description)")
            @RequestParam(value = "search", required = false) String search,
            @Parameter(description = "Transaction date from (optional, format: YYYY-MM-DD)")
            @RequestParam(value = "fromDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @Parameter(description = "Transaction date to (optional, format: YYYY-MM-DD)")
            @RequestParam(value = "toDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @Parameter(description = "Validity from date (optional, format: YYYY-MM-DD)")
            @RequestParam(value = "validityFromDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate validityFromDate,
            @Parameter(description = "Validity to date (optional, format: YYYY-MM-DD)")
            @RequestParam(value = "validityToDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate validityToDate,
            @Parameter(description = "Page number (optional, default: 0)")
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
            @Parameter(description = "Page size (optional, default: 20)")
            @RequestParam(value = "size", required = false, defaultValue = "20") Integer size,
            @Parameter(description = "Sort field (optional, default: transactionDate)")
            @RequestParam(value = "sortBy", required = false, defaultValue = "transactionDate") String sortBy,
            @Parameter(description = "Sort order (optional, ASC or DESC, default: DESC)")
            @RequestParam(value = "sortOrder", required = false, defaultValue = "DESC") String sortOrder) {
        BigDecimal companyId = BigDecimal.valueOf(UserContext.getCompanyPoid());
        String userId = UserContext.getUserId();
        log.info("search sales quotations started for companyId={} userId={} page={} size={}", 
                companyId, userId, page, size);
        
        SalesQuotationShipFilter filter = new SalesQuotationShipFilter();
        filter.setCompanyPoid(companyId);
        filter.setCustomerPoid(customerId);
        filter.setSalesmanPoid(salesmanId);
        filter.setLinePoid(lineId);
        filter.setQuotationStatus(status);
        filter.setQuotationType(qtnType);
        filter.setDocRef(docRef);
        filter.setSearch(search);
        filter.setFromDate(fromDate);
        filter.setToDate(toDate);
        filter.setValidityFromDate(validityFromDate);
        filter.setValidityToDate(validityToDate);
        filter.setPage(page);
        filter.setSize(size);
        filter.setSortBy(sortBy);
        filter.setSortOrder(sortOrder);
        
        BigDecimal userIdBigDecimal = null;
        try {
            if (userId != null && !userId.isBlank()) {
                userIdBigDecimal = new BigDecimal(userId);
            }
        } catch (NumberFormatException e) {
            log.warn("Invalid userId format: {}, line access control will be skipped", userId);
        }
        
        SalesQuotationShipListResponse response = service.search(filter, userIdBigDecimal);
        log.info("search sales quotations completed for companyId={} totalElements={} totalPages={}", 
                companyId, response.getTotalElements(), response.getTotalPages());
        return response;
    }

    @Operation(
            summary = "Get sales quotation by ID",
            description = "Retrieves a single Sales Quotation document by its primary key. " +
                    "Includes header and optionally detail tables (Charge Details, Equipment Details). " +
                    "The quotation must belong to the specified company for data isolation. " +
                    "Totals are recalculated from charge details to ensure accuracy.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully retrieved sales quotation",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = SalesQuotationShipDetailDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input parameters or validation error",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Sales quotation not found or does not belong to the specified company",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping(path = "/{transactionPoid}")
    public SalesQuotationShipDetailDto getById(
            @Parameter(description = "Transaction POID of the sales quotation to retrieve", required = true)
            @PathVariable("transactionPoid") BigDecimal transactionPoid,
            @Parameter(description = "Include charge and equipment details in response (optional, default: true)")
            @RequestParam(value = "includeDetails", defaultValue = "true") boolean includeDetails) {
        BigDecimal companyId = BigDecimal.valueOf(UserContext.getCompanyPoid());
        log.info("get sales quotation started for transactionPoid={} companyId={} includeDetails={}", 
                transactionPoid, companyId, includeDetails);
        SalesQuotationShipDetailDto dto = service.getDetail(transactionPoid, companyId, includeDetails);
        log.info("get sales quotation completed for transactionPoid={} docRef={}", 
                transactionPoid, dto != null ? dto.getDocRef() : null);
        loggingService.createLogSummaryEntry(LogDetailsEnum.VIEWED, UserContext.getDocumentId(), transactionPoid.toString());
        return dto;
    }

    @Operation(
            summary = "Load customer data",
            description = "Loads customer data for a sales quotation based on customer address selection. " +
                    "Calls stored procedure to retrieve customer contact details and other related information.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully loaded customer data",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = SalesQuotationShipCustomerDataResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input parameters",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping(path = "/customer-data", consumes = MediaType.APPLICATION_JSON_VALUE)
    public SalesQuotationShipCustomerDataResponse getCustomerData(
            @Parameter(description = "Customer data request with companyId, customerAddressId, and transactionPoid", required = true)
            @RequestBody SalesQuotationShipCustomerDataRequest request) {
        return service.loadCustomerData(request);
    }

    @Operation(
            summary = "Refresh detail charges",
            description = "Refreshes charge details for a sales quotation based on quoted rate. " +
                    "Recalculates charges and updates the quotation details.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully refreshed detail charges",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input parameters",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping(path = "/refresh-detail", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> refreshDetail(
            @Parameter(description = "Refresh detail request with transactionPoid and quotedRate", required = true)
            @RequestBody SalesQuotationShipRefreshDetailRequest request) {
        return ResponseEntity.ok(service.refreshDetailCharges(request));
    }

    @Operation(
            summary = "Create RFQ",
            description = "Creates a Request for Quotation (RFQ) based on the sales quotation. " +
                    "Converts the quotation to an RFQ document for vendor processing.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully created RFQ",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input parameters",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping(path = "/rfq", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> createRfq(
            @Parameter(description = "RFQ creation request with transactionPoid and loginUser", required = true)
            @RequestBody SalesQuotationShipRfQRequest request) {
        return ResponseEntity.ok(service.createRfq(request));
    }

    @Operation(
            summary = "Update linked quantities",
            description = "Updates quantities for linked items in the sales quotation. " +
                    "Synchronizes quantities across related items and recalculates totals.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully updated quantities",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input parameters",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping(path = "/update-quantities", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> updateQuantities(
            @Parameter(description = "Quantity update request with transactionPoid and quantity details", required = true)
            @RequestBody SalesQuotationShipQuantityUpdateRequest request) {
        return ResponseEntity.ok(service.updateLinkedQuantities(request));
    }

    @Operation(
            summary = "Import items",
            description = "Imports items into the sales quotation from external sources. " +
                    "Adds items to charge details or equipment details based on the import type.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully imported items",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input parameters",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping(path = "/import-items", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> importItems(
            @Parameter(description = "Import items request with transactionPoid and items to import", required = true)
            @RequestBody SalesQuotationShipImportRequest request) {
        return ResponseEntity.ok(service.importItems(request));
    }

    @Operation(
            summary = "Clear items",
            description = "Clears all items (charge details and equipment details) from the sales quotation. " +
                    "Removes all detail records while preserving the header.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully cleared items",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input parameters",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping(path = "/clear-items", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> clearItems(
            @Parameter(description = "Clear items request with transactionPoid", required = true)
            @RequestBody SalesQuotationShipClearItemsRequest request) {
        return ResponseEntity.ok(service.clearItems(request));
    }

    @Operation(
            summary = "Create delivery note",
            description = "Creates a delivery note from the sales quotation. " +
                    "Converts the quotation to a delivery note document for logistics processing.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully created delivery note",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input parameters",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping(path = "/delivery-note", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> createDeliveryNote(
            @Parameter(description = "Delivery note creation request with transactionPoid", required = true)
            @RequestBody SalesQuotationShipDeliveryNoteRequest request) {
        return ResponseEntity.ok(service.createDeliveryNote(request));
    }

    @Operation(
            summary = "Select all details",
            description = "Selects all charge and equipment details in the sales quotation. " +
                    "Used for bulk operations on detail records.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully selected all details",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input parameters",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping(path = "/select-all", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> selectAll(
            @Parameter(description = "Select all request with transactionPoid", required = true)
            @RequestBody SalesQuotationShipSelectAllRequest request) {
        return ResponseEntity.ok(service.selectAllDetails(request));
    }

    @Operation(
            summary = "Validate customer",
            description = "Validates customer data for the sales quotation. " +
                    "Checks customer status, credit limits, and other validation rules.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Customer validation completed",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = SalesQuotationShipCustomerValidationResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input parameters",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping(path = "/validate-customer", consumes = MediaType.APPLICATION_JSON_VALUE)
    public SalesQuotationShipCustomerValidationResponse validateCustomer(
            @Parameter(description = "Customer validation request with customerPoid and transactionPoid", required = true)
            @RequestBody SalesQuotationShipValidationRequest request) {
        return service.validateCustomer(request);
    }

    @Operation(
            summary = "Set default detail values",
            description = "Sets default values for charge or equipment detail records. " +
                    "Auto-populates fields based on customer, stock, and other context information.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully set default detail values",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = SalesQuotationShipDefaultDetailResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input parameters",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping(path = "/default-detail", consumes = MediaType.APPLICATION_JSON_VALUE)
    public SalesQuotationShipDefaultDetailResponse defaultDetail(
            @Parameter(description = "Default detail request with customerAddressId, stockPoid, and transactionPoid", required = true)
            @RequestBody SalesQuotationShipDefaultDetailRequest request) {
        return service.setDefaultDetailValues(request);
    }

    @Operation(
            summary = "Validate delivery option",
            description = "Validates delivery option for the sales quotation. " +
                    "Checks if the selected delivery option is valid for the quotation type and customer.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Delivery option validation completed",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input parameters",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping(path = "/delivery-option/validate", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> validateDeliveryOption(
            @Parameter(description = "Delivery option validation request with transactionPoid and deliveryOption", required = true)
            @RequestBody SalesQuotationShipDeliveryOptionValidateRequest request) {
        return ResponseEntity.ok(service.validateDeliveryOption(request));
    }

    @Operation(
            summary = "Calculate after save",
            description = "Triggers calculation after saving the sales quotation. " +
                    "Recalculates totals, taxes, and other calculated fields from charge and equipment details.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully calculated totals",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input parameters",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping(path = "/calculate", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> calculate(
            @Parameter(description = "Calculate request with transactionPoid", required = true)
            @RequestBody SalesQuotationShipCalculateRequest request) {
        return ResponseEntity.ok(service.calculateAfterSave(request));
    }

    @Operation(
            summary = "Mark document as deleted",
            description = "Marks a sales quotation document as deleted (soft delete). " +
                    "Sets the Deleted flag to 'Y' and updates audit fields.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully marked document as deleted",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input parameters",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping(path = "/document/delete", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> deleteDocument(
            @Parameter(description = "Delete document request with transactionPoid", required = true)
            @RequestBody SalesQuotationShipGlobalDeleteRequest request) {
        return ResponseEntity.ok(service.markDocumentAsDeleted(request));
    }

    @Operation(
            summary = "Acquire record lock",
            description = "Acquires a record lock on the sales quotation document. " +
                    "Prevents other users from editing the document simultaneously.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully acquired record lock",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input parameters or lock already acquired",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping(path = "/document/lock", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> lockDocument(
            @Parameter(description = "Record lock request with transactionPoid and userId", required = true)
            @RequestBody SalesQuotationShipRecordLockRequest request) {
        return ResponseEntity.ok(service.acquireRecordLock(request));
    }

    @Operation(
            summary = "Release record lock",
            description = "Releases a record lock on the sales quotation document. " +
                    "Allows other users to edit the document.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully released record lock",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input parameters",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping(path = "/document/release-lock", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> releaseLock(
            @Parameter(description = "Release lock request with transactionPoid and userId", required = true)
            @RequestBody SalesQuotationShipReleaseLockRequest request) {
        return ResponseEntity.ok(service.releaseRecordLock(request));
    }

    @Operation(
            summary = "Get deleted documents",
            description = "Retrieves a list of soft-deleted sales quotation documents. " +
                    "Returns documents that have been marked as deleted (Deleted='Y').",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully retrieved deleted documents",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = SalesQuotationShipTreeResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input parameters",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping(path = "/document/deleted", consumes = MediaType.APPLICATION_JSON_VALUE)
    public SalesQuotationShipTreeResponse getDeletedDocuments(
            @Parameter(description = "Deleted documents request with companyId and filters", required = true)
            @RequestBody SalesQuotationShipDeletedDocsRequest request) {
        return service.loadDeletedDocuments(request);
    }

    @Operation(
            summary = "Reset document sequence",
            description = "Resets the sequence number for the sales quotation document. " +
                    "Resets DocRef generation sequence for the document type.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully reset document sequence",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input parameters",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping(path = "/document/reset-sequence", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> resetSequence(
            @Parameter(description = "Reset sequence request with document type and sequence details", required = true)
            @RequestBody SalesQuotationShipResetSequenceRequest request) {
        return ResponseEntity.ok(service.resetSequence(request));
    }

    @Operation(
            summary = "Update document sequence",
            description = "Updates the sequence number for the sales quotation document. " +
                    "Updates DocRef generation sequence for the document type.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully updated document sequence",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input parameters",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping(path = "/document/update-sequence", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> updateSequence(
            @Parameter(description = "Update sequence request with document type and new sequence", required = true)
            @RequestBody SalesQuotationShipUpdateSequenceRequest request) {
        return ResponseEntity.ok(service.updateSequence(request));
    }

    @Operation(
            summary = "Update user profile",
            description = "Updates user profile settings for the sales quotation module. " +
                    "Saves user preferences and settings.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully updated user profile",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input parameters",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping(path = "/user-profile", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> updateUserProfile(
            @Parameter(description = "User profile update request with user preferences", required = true)
            @RequestBody SalesQuotationShipUserProfileRequest request) {
        service.updateUserProfile(request);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Grant edit permission",
            description = "Grants edit permission to a user for the sales quotation document. " +
                    "Allows users with appropriate permissions to edit locked documents.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully granted edit permission",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input parameters",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping(path = "/document/grant-edit", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> grantEdit(
            @Parameter(description = "Grant edit request with transactionPoid and userId", required = true)
            @RequestBody SalesQuotationShipGrantEditRequest request) {
        return ResponseEntity.ok(service.grantEditPermission(request));
    }

    @Operation(
            summary = "Approval action",
            description = "Performs an approval action on the sales quotation document. " +
                    "Supports approve, reject, and other approval workflow actions.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully processed approval action",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = SalesQuotationShipApprovalResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input parameters",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping(path = "/document/approval", consumes = MediaType.APPLICATION_JSON_VALUE)
    public SalesQuotationShipApprovalResponse approvalAction(
            @Parameter(description = "Approval action request with transactionPoid, action, and comments", required = true)
            @RequestBody SalesQuotationShipApprovalActionRequest request) {
        return service.approvalAction(request);
    }

    @Operation(
            summary = "Update document confidentiality",
            description = "Updates the confidentiality flag for the sales quotation document. " +
                    "Marks the document as confidential or removes confidential status.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully updated document confidentiality",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input parameters",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping(path = "/document/confidential", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> updateConfidential(
            @Parameter(description = "Confidentiality update request with transactionPoid and confidential flag", required = true)
            @RequestBody SalesQuotationShipConfidentialRequest request) {
        return ResponseEntity.ok(service.updateDocumentConfidentiality(request));
    }

    @Operation(
            summary = "View GL posting",
            description = "Retrieves General Ledger (GL) posting details for the sales quotation. " +
                    "Shows GL entries created from the quotation for accounting purposes.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully retrieved GL posting details",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = SalesQuotationShipGlPostingResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input parameters",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping(path = "/gl/view", consumes = MediaType.APPLICATION_JSON_VALUE)
    public SalesQuotationShipGlPostingResponse viewGlPosting(
            @Parameter(description = "GL view request with transactionPoid", required = true)
            @RequestBody SalesQuotationShipGlViewRequest request) {
        return service.loadGlPosting(request);
    }

    @Operation(
            summary = "Repost to GL",
            description = "Reposts the sales quotation to General Ledger (GL). " +
                    "Regenerates GL entries for accounting purposes.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully reposted to GL",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input parameters",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping(path = "/gl/repost", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> repostGl(
            @Parameter(description = "GL repost request with transactionPoid", required = true)
            @RequestBody SalesQuotationShipGlRepostRequest request) {
        return ResponseEntity.ok(service.repostToGl(request));
    }

    @Operation(
            summary = "Add default local charges",
            description = "Automatically adds default local charges to the quotation. " +
                    "Calls stored procedure PROC_SALES_SHQTN_LOCAL_CHARGES. " +
                    "Requires user confirmation as this operation cannot be undone. " +
                    "Only allowed when quotation status is 'PROCESSING'.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully added local charges",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = SalesQuotationShipService.AddLocalChargesResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input parameters, confirmation not provided, or quotation status is not PROCESSING",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Sales quotation not found or does not belong to the specified company",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping(path = "/{transactionPoid}/add-local-charges", consumes = MediaType.APPLICATION_JSON_VALUE)
    public SalesQuotationShipService.AddLocalChargesResponse addLocalCharges(
            @Parameter(description = "Transaction POID of the sales quotation", required = true)
            @PathVariable("transactionPoid") BigDecimal transactionPoid,
            @Parameter(description = "Request body with confirmation flag. Must set confirm=true to proceed.", required = true)
            @RequestBody AddLocalChargesRequest request) {
        BigDecimal companyId = BigDecimal.valueOf(UserContext.getCompanyPoid());
        log.info("addLocalCharges started for transactionPoid={} companyId={} userId={} confirm={}",
                transactionPoid, companyId, UserContext.getUserId(), request != null ? request.confirm() : false);
        SalesQuotationShipService.AddLocalChargesResponse response = service.addLocalCharges(
                transactionPoid, companyId, UserContext.getUserId(), request != null && request.confirm());
        log.info("addLocalCharges completed for transactionPoid={} success={} chargesAdded={}",
                transactionPoid, response.success(), response.chargesAdded());
        return response;
    }

    @Operation(
            summary = "Get default salesman",
            description = "Retrieves the default salesman for the current user. " +
                    "Calls stored procedure PROC_SALES_QTN_SALESMAN to get the user's assigned salesman. " +
                    "Used for auto-populating the salesman field when creating new quotations. " +
                    "The stored procedure returns salesman POID as VARCHAR, which is returned as String in the response.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully retrieved default salesman",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = SalesQuotationShipService.SalesmanDefaultResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input parameters or invalid userId format",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping(path = "/default-salesman")
    public SalesQuotationShipService.SalesmanDefaultResponse getDefaultSalesman() {
        String userId = UserContext.getUserId();
        log.info("getDefaultSalesman started for userId={}", userId);
        SalesQuotationShipService.SalesmanDefaultResponse response = service.getDefaultSalesman(userId);
        log.info("getDefaultSalesman completed for userId={} salesmanPoid={}",
                userId, response != null ? response.salesmanPoid() : null);
        return response;
    }

    @Operation(
            summary = "Get user line access list",
            description = "Retrieves list of line POIDs that the user can access for quotations. " +
                    "Calls stored procedure PROC_GLOB_USER_LINE_LIST_SHQN to get the user's line access rights. " +
                    "Used for filtering quotation list based on user's line access. " +
                    "Returns comma-separated list of line POIDs, or 'ALL_LINE_USER' if user has access to all lines. " +
                    "If result is 'ALL_LINE_USER', no line filtering should be applied in list queries.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully retrieved user line access list",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = SalesQuotationShipService.LineAccessResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input parameters or invalid userId format",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping(path = "/user-lines")
    public SalesQuotationShipService.LineAccessResponse getUserLines() {
        String userId = UserContext.getUserId();
        log.info("getUserLines started for userId={}", userId);
        SalesQuotationShipService.LineAccessResponse response = service.getAccessibleLines(userId);
        log.info("getUserLines completed for userId={} lineList={}",
                userId, response != null ? response.lineList() : null);
        return response;
    }

    @Operation(
            summary = "Get quotation totals",
            description = "Calculates and returns buying total, selling total, and tax total for a quotation. " +
                    "Totals are calculated from charge details: " +
                    "BuyingTotal = Sum of BuyingChargeLocal, " +
                    "SellingTotal = Sum of TotalSellingChargeLocal (selling charge + tax), " +
                    "TotalTax = Sum of TaxAmountLocal. " +
                    "Validates that the quotation belongs to the specified company for data isolation.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully retrieved quotation totals",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = SalesQuotationShipService.QuotationTotalsResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input parameters",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Sales quotation not found, or does not belong to the specified company",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping(path = "/{transactionPoid}/totals")
    public SalesQuotationShipService.QuotationTotalsResponse getQuotationTotals(
            @Parameter(description = "Transaction POID of the sales quotation", required = true)
            @PathVariable("transactionPoid") BigDecimal transactionPoid) {
        BigDecimal companyId = BigDecimal.valueOf(UserContext.getCompanyPoid());
        log.info("getQuotationTotals started for transactionPoid={} companyId={}", transactionPoid, companyId);
        SalesQuotationShipService.QuotationTotalsResponse response = service.getQuotationTotals(transactionPoid, companyId);
        log.info("getQuotationTotals completed for transactionPoid={} buyingTotal={} sellingTotal={} totalTax={}",
                transactionPoid,
                response != null ? response.buyingTotal() : null,
                response != null ? response.sellingTotal() : null,
                response != null ? response.totalTax() : null);
        return response;
    }

    @Operation(
            summary = "Check quotation dependencies",
            description = "Checks if a quotation can be deleted by checking for dependencies. " +
                    "Validates that the quotation belongs to the specified company for data isolation. " +
                    "Checks for dependencies like Sales Invoices that reference this quotation. " +
                    "Returns detailed information about dependencies and whether deletion is allowed. " +
                    "Used before allowing delete to provide user-friendly error messages.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully checked quotation dependencies",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = SalesQuotationShipService.QuotationDependenciesResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input parameters",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Sales quotation not found, or does not belong to the specified company",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping(path = "/{transactionPoid}/dependencies")
    public SalesQuotationShipService.QuotationDependenciesResponse checkQuotationDependencies(
            @Parameter(description = "Transaction POID of the sales quotation", required = true)
            @PathVariable("transactionPoid") BigDecimal transactionPoid) {
        BigDecimal companyId = BigDecimal.valueOf(UserContext.getCompanyPoid());
        log.info("checkQuotationDependencies started for transactionPoid={} companyId={}", transactionPoid, companyId);
        SalesQuotationShipService.QuotationDependenciesResponse response = service.checkQuotationDependencies(transactionPoid, companyId);
        log.info("checkQuotationDependencies completed for transactionPoid={} canDelete={} salesInvoiceCount={}",
                transactionPoid,
                response != null ? response.canDelete() : null,
                response != null ? response.salesInvoiceCount() : null);
        return response;
    }

    @Operation(
            summary = "Get customer contact details",
            description = "Retrieves customer contact details (contact person, email) when customer address is selected. " +
                    "Calls stored procedure PROC_GET_QTN_CUST_ADDRESS to get contact information. " +
                    "Used for auto-populating customer contact and email fields in the quotation.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully retrieved customer contact details",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = CustomerContactResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input parameters",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Customer not found",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping(path = "/customers/{customerId}/contact")
    public CustomerContactResponse getCustomerContact(
            @Parameter(description = "Customer POID (required)", required = true)
            @PathVariable("customerId") BigDecimal customerId) {
        BigDecimal userId = new BigDecimal(UserContext.getUserId());
        return service.getCustomerContactDetails(userId, customerId);
    }

    @Operation(
            summary = "Get charge tax details",
            description = "Retrieves tax percentage and tax POID for a charge based on company, customer, and charge. " +
                    "Calls stored procedure PROC_GET_CHARGE_TAX_PER_V2 to get tax information. " +
                    "Used for auto-populating tax fields when a charge is selected in charge details.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully retrieved charge tax details",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ChargeTaxResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input parameters",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Charge or tax information not found",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping(path = "/charges/{chargeId}/tax")
    public ChargeTaxResponse getChargeTax(
            @Parameter(description = "Customer POID (required)", required = true)
            @RequestParam("customerId") BigDecimal customerId,
            @Parameter(description = "Charge POID (required)", required = true)
            @PathVariable("chargeId") BigDecimal chargeId) {
        BigDecimal companyPoid = BigDecimal.valueOf(UserContext.getCompanyPoid());
        return service.getChargeTaxDetails(companyPoid, customerId, chargeId);
    }

    @Operation(
            summary = "Add charge detail",
            description = "Adds a charge detail record to a Sales Quotation. " +
                    "Implements business logic including default values, tax auto-population, and charge calculations. " +
                    "DetRowId is auto-generated as the next sequence number for the quotation. " +
                    "Calculates all read-only fields (buyingCharge, sellingCharge, taxAmount, local amounts). " +
                    "Recalculates header totals after adding the charge detail.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully added charge detail",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = SalesQuotationShipItemDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input parameters or validation error",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Sales quotation not found or does not belong to the specified company",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping(path = "/{transactionPoid}/charge-details", consumes = MediaType.APPLICATION_JSON_VALUE)
    public SalesQuotationShipItemDto addChargeDetail(
            @Parameter(description = "Transaction POID of the sales quotation", required = true)
            @PathVariable("transactionPoid") BigDecimal transactionPoid,
            @Parameter(description = "Charge detail request. Calculated fields (buyingCharge, sellingCharge, taxAmount, local amounts) are read-only and will be calculated automatically.", required = true)
            @RequestBody SalesQuotationShipChargeRequest request) {
        BigDecimal companyId = BigDecimal.valueOf(UserContext.getCompanyPoid());
        log.info("addChargeDetail started for transactionPoid={} companyId={} userId={}", 
                transactionPoid, companyId, UserContext.getUserId());
        SalesQuotationShipItemDto dto = service.addChargeDetail(transactionPoid, companyId, UserContext.getUserId(), request);
        log.info("addChargeDetail completed for transactionPoid={} detRowId={}", 
                transactionPoid, dto != null ? dto.getDetailRowId() : null);
        return dto;
    }

    @Operation(
            summary = "Update charge detail",
            description = "Updates an existing charge detail record in a Sales Quotation. " +
                    "Implements business logic including default values, tax auto-population, and charge calculations. " +
                    "DetRowId cannot be changed (part of primary key). " +
                    "Recalculates all read-only fields (buyingCharge, sellingCharge, taxAmount, local amounts) when any input field changes. " +
                    "Recalculates header totals after updating the charge detail. " +
                    "Preserves createdBy and createdDate audit fields.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully updated charge detail",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = SalesQuotationShipItemDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input parameters or validation error",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Sales quotation or charge detail not found, or does not belong to the specified company",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PutMapping(path = "/{transactionPoid}/charge-details/{detRowId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public SalesQuotationShipItemDto updateChargeDetail(
            @Parameter(description = "Transaction POID of the sales quotation", required = true)
            @PathVariable("transactionPoid") BigDecimal transactionPoid,
            @Parameter(description = "Detail Row ID of the charge detail to update (cannot be changed)", required = true)
            @PathVariable("detRowId") BigDecimal detRowId,
            @Parameter(description = "Charge detail update request. Calculated fields (buyingCharge, sellingCharge, taxAmount, local amounts) are read-only and will be recalculated automatically. DetRowId in request body is ignored.", required = true)
            @RequestBody SalesQuotationShipChargeRequest request) {
        BigDecimal companyId = BigDecimal.valueOf(UserContext.getCompanyPoid());
        log.info("updateChargeDetail started for transactionPoid={} detRowId={} companyId={} userId={}",
                transactionPoid, detRowId, companyId, UserContext.getUserId());
        SalesQuotationShipItemDto dto = service.updateChargeDetail(transactionPoid, detRowId, companyId, UserContext.getUserId(), request);
        log.info("updateChargeDetail completed for transactionPoid={} detRowId={}",
                transactionPoid, detRowId);
        return dto;
    }

    @Operation(
            summary = "Delete charge detail",
            description = "Deletes a charge detail record from a Sales Quotation. " +
                    "Validates that the quotation and charge detail belong to the specified company for data isolation. " +
                    "Recalculates header totals (buying total, selling total, tax total) after deletion. " +
                    "DetRowId cannot be changed (part of primary key).",
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "Successfully deleted charge detail",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input parameters or validation error",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Sales quotation or charge detail not found, or does not belong to the specified company",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @DeleteMapping(path = "/{transactionPoid}/charge-details/{detRowId}")
    public ResponseEntity<Void> deleteChargeDetail(
            @Parameter(description = "Transaction POID of the sales quotation", required = true)
            @PathVariable("transactionPoid") BigDecimal transactionPoid,
            @Parameter(description = "Detail Row ID of the charge detail to delete", required = true)
            @PathVariable("detRowId") BigDecimal detRowId) {
        BigDecimal companyId = BigDecimal.valueOf(UserContext.getCompanyPoid());
        log.info("deleteChargeDetail started for transactionPoid={} detRowId={} companyId={}",
                transactionPoid, detRowId, companyId);
        service.deleteChargeDetail(transactionPoid, detRowId, companyId);
        log.info("deleteChargeDetail completed for transactionPoid={} detRowId={}",
                transactionPoid, detRowId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Get charge details",
            description = "Retrieves all charge details for a Sales Quotation. " +
                    "Validates that the quotation belongs to the specified company for data isolation. " +
                    "Returns charge details ordered by DetRowId. " +
                    "Returns an empty array if no charge details exist.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully retrieved charge details",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = SalesQuotationShipItemDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input parameters",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Sales quotation not found, or does not belong to the specified company",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping(path = "/{transactionPoid}/charge-details")
    public List<SalesQuotationShipItemDto> getChargeDetails(
            @Parameter(description = "Transaction POID of the sales quotation", required = true)
            @PathVariable("transactionPoid") BigDecimal transactionPoid) {
        BigDecimal companyId = BigDecimal.valueOf(UserContext.getCompanyPoid());
        log.info("getChargeDetails started for transactionPoid={} companyId={}",
                transactionPoid, companyId);
        List<SalesQuotationShipItemDto> dtos = service.getChargeDetails(transactionPoid, companyId);
        log.info("getChargeDetails completed for transactionPoid={} found {} charge details",
                transactionPoid, dtos != null ? dtos.size() : 0);
        return dtos;
    }

    @Operation(
            summary = "Add equipment detail",
            description = "Adds an equipment detail record to a Sales Quotation. " +
                    "Equipment details are only shown for FCL cargo type. " +
                    "Implements business logic including default values (quantity defaults to 1). " +
                    "DetRowId is auto-generated as the next sequence number for the quotation.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully added equipment detail",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = SalesQuotationShipEquipmentDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input parameters or validation error",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Sales quotation not found or does not belong to the specified company",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping(path = "/{transactionPoid}/equipment-details", consumes = MediaType.APPLICATION_JSON_VALUE)
    public SalesQuotationShipEquipmentDto addEquipmentDetail(
            @Parameter(description = "Transaction POID of the sales quotation", required = true)
            @PathVariable("transactionPoid") BigDecimal transactionPoid,
            @Parameter(description = "Equipment detail request. Quantity defaults to 1 if not provided.", required = true)
            @RequestBody SalesQuotationShipEquipmentRequest request) {
        BigDecimal companyId = BigDecimal.valueOf(UserContext.getCompanyPoid());
        log.info("addEquipmentDetail started for transactionPoid={} companyId={} userId={}",
                transactionPoid, companyId, UserContext.getUserId());
        SalesQuotationShipEquipmentDto dto = service.addEquipmentDetail(transactionPoid, companyId, UserContext.getUserId(), request);
        log.info("addEquipmentDetail completed for transactionPoid={} detRowId={}",
                transactionPoid, dto != null ? dto.getDetailRowId() : null);
        return dto;
    }

    @Operation(
            summary = "Update equipment detail",
            description = "Updates an existing equipment detail record in a Sales Quotation. " +
                    "Implements business logic including default values. " +
                    "DetRowId cannot be changed (part of primary key). " +
                    "Preserves createdBy and createdDate audit fields.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully updated equipment detail",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = SalesQuotationShipEquipmentDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input parameters or validation error",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Sales quotation or equipment detail not found, or does not belong to the specified company",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PutMapping(path = "/{transactionPoid}/equipment-details/{detRowId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public SalesQuotationShipEquipmentDto updateEquipmentDetail(
            @Parameter(description = "Transaction POID of the sales quotation", required = true)
            @PathVariable("transactionPoid") BigDecimal transactionPoid,
            @Parameter(description = "Detail Row ID of the equipment detail to update (cannot be changed)", required = true)
            @PathVariable("detRowId") BigDecimal detRowId,
            @Parameter(description = "Equipment detail update request. DetRowId in request body is ignored.", required = true)
            @RequestBody SalesQuotationShipEquipmentRequest request) {
        BigDecimal companyId = BigDecimal.valueOf(UserContext.getCompanyPoid());
        log.info("updateEquipmentDetail started for transactionPoid={} detRowId={} companyId={} userId={}",
                transactionPoid, detRowId, companyId, UserContext.getUserId());
        SalesQuotationShipEquipmentDto dto = service.updateEquipmentDetail(transactionPoid, detRowId, companyId, UserContext.getUserId(), request);
        log.info("updateEquipmentDetail completed for transactionPoid={} detRowId={}",
                transactionPoid, detRowId);
        return dto;
    }

    @Operation(
            summary = "Get equipment details",
            description = "Retrieves all equipment details for a Sales Quotation. " +
                    "Validates that the quotation belongs to the specified company for data isolation. " +
                    "Returns equipment details ordered by DetRowId. " +
                    "Returns an empty array if no equipment details exist.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully retrieved equipment details",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = SalesQuotationShipEquipmentDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input parameters",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Sales quotation not found, or does not belong to the specified company",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping(path = "/{transactionPoid}/equipment-details")
    public List<SalesQuotationShipEquipmentDto> getEquipmentDetails(
            @Parameter(description = "Transaction POID of the sales quotation", required = true)
            @PathVariable("transactionPoid") BigDecimal transactionPoid) {
        BigDecimal companyId = BigDecimal.valueOf(UserContext.getCompanyPoid());
        log.info("getEquipmentDetails started for transactionPoid={} companyId={}",
                transactionPoid, companyId);
        List<SalesQuotationShipEquipmentDto> dtos = service.getEquipmentDetails(transactionPoid, companyId);
        log.info("getEquipmentDetails completed for transactionPoid={} found {} equipment details",
                transactionPoid, dtos != null ? dtos.size() : 0);
        return dtos;
    }

    @Operation(
            summary = "Get customer address details",
            description = "Retrieves customer contact details (contact person, email) when customer address is selected. " +
                    "Calls stored procedure PROC_GET_QTN_CUST_ADDRESS to get contact information. " +
                    "Used for auto-populating customer contact and email fields in the quotation. " +
                    "Validates that the quotation belongs to the specified company for data isolation.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully retrieved customer contact details",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = SalesQuotationShipService.CustomerContactResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input parameters",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Sales quotation not found, or does not belong to the specified company, or customer not found",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping(path = "/{transactionPoid}/customer-address")
    public SalesQuotationShipService.CustomerContactResponse getCustomerAddress(
            @Parameter(description = "Transaction POID of the sales quotation", required = true)
            @PathVariable("transactionPoid") BigDecimal transactionPoid,
            @Parameter(description = "Customer POID (from address selection, required)", required = true)
            @RequestParam("customerPoid") BigDecimal customerPoid) {
        BigDecimal companyId = BigDecimal.valueOf(UserContext.getCompanyPoid());
        log.info("getCustomerAddress started for transactionPoid={} companyId={} userId={} customerPoid={}",
                transactionPoid, companyId, UserContext.getUserId(), customerPoid);
        SalesQuotationShipService.CustomerContactResponse response = service.getCustomerAddressDetails(
                transactionPoid, companyId, UserContext.getUserId(), customerPoid);
        log.info("getCustomerAddress completed for transactionPoid={} customerPoid={}",
                transactionPoid, customerPoid);
        return response;
    }

    @Operation(
            summary = "Get charge tax percentage",
            description = "Retrieves tax percentage and tax POID for a charge based on company, customer, and charge. " +
                    "Calls stored procedure PROC_GET_CHARGE_TAX_PER_V2 to get tax information. " +
                    "Used for auto-populating tax fields when a charge is selected in charge details. " +
                    "Validates that the quotation belongs to the specified company for data isolation.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully retrieved charge tax details",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = SalesQuotationShipService.ChargeTaxResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input parameters",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Sales quotation not found, or does not belong to the specified company",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping(path = "/{transactionPoid}/charge-tax")
    public SalesQuotationShipService.ChargeTaxResponse getChargeTax(
            @Parameter(description = "Transaction POID of the sales quotation", required = true)
            @PathVariable("transactionPoid") BigDecimal transactionPoid,
            @Parameter(description = "Customer POID (required)", required = true)
            @RequestParam("customerPoid") BigDecimal customerPoid,
            @Parameter(description = "Charge POID (required)", required = true)
            @RequestParam("chargePoid") BigDecimal chargePoid) {
        BigDecimal companyPoid = BigDecimal.valueOf(UserContext.getCompanyPoid());
        log.info("getChargeTax started for transactionPoid={} companyPoid={} customerPoid={} chargePoid={}",
                transactionPoid, companyPoid, customerPoid, chargePoid);
        SalesQuotationShipService.ChargeTaxResponse response = service.getChargeTaxForQuotation(
                transactionPoid, companyPoid, customerPoid, chargePoid);
        log.info("getChargeTax completed for transactionPoid={} chargePoid={} taxPercentage={} taxPoid={}",
                transactionPoid, chargePoid,
                response != null ? response.taxPercentage() : null,
                response != null ? response.taxPoid() : null);
        return response;
    }

    public record ApplyLocalChargesRequest(String loginUser) { }
    
    public record AddLocalChargesRequest(boolean confirm) { }
}
