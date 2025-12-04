package com.asg.shipchandling.salesquotationsch.controller;

import com.asg.shipchandling.salesquotationsch.dto.*;
import com.asg.shipchandling.salesquotationsch.dto.request.*;
import com.asg.shipchandling.salesquotationsch.dto.request.UpdateSalesQuotationSchRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.asg.shipchandling.salesquotationsch.dto.response.SalesQuotationSchListResponse;
import com.asg.shipchandling.salesquotationsch.dto.response.StoredProcedureResponse;
import com.asg.shipchandling.salesquotationsch.dto.response.ValidationResponse;
import com.asg.shipchandling.salesquotationsch.dto.response.AddressDetailsResponse;
import com.asg.shipchandling.salesquotationsch.dto.response.ExcelImportResponse;
import com.asg.shipchandling.salesquotationsch.service.SalesQuotationSchService;
import com.asg.common.lib.security.util.UserContext;

import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import static com.asg.shipchandling.common.ApiResponse.success;
import static com.asg.shipchandling.common.ApiResponse.badRequest;

@RestController
@RequestMapping("/v1/sales-quotation-sch")
@RequiredArgsConstructor
@Slf4j
public class SalesQuotationSchController {

        private final SalesQuotationSchService quotationSchService;

        // ==================== BASIC CRUD OPERATIONS ====================

        @Operation(summary = "Create Sales Quotation SCH", description = "Creates a new Sales Quotation SCH. DocRef is auto-generated.", responses = {
                        @ApiResponse(responseCode = "200", description = "Successfully created sales quotation sch"),
                        @ApiResponse(responseCode = "400", description = "Invalid input or validation error"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized")
        }, security = @SecurityRequirement(name = "bearerAuth"))
        @PostMapping
        public ResponseEntity<?> createSalesQuotationSch(
                        @Valid @RequestBody CreateSalesQuotationSchRequest request) {
                log.info("createSalesQuotationSch started for companyPoid={} groupPoid={}", UserContext.getCompanyPoid(), UserContext.getGroupPoid());
                SalesQuotationSchHdrDto dto = quotationSchService.createSalesQuotationSch(
                                request, UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserId());
                log.info("createSalesQuotationSch completed for companyPoid={} groupPoid={}", UserContext.getCompanyPoid(), UserContext.getGroupPoid());
                return success("Sales quotation sch created successfully", dto);
        }

        @Operation(summary = "Get Sales Quotation SCH by ID", description = "Retrieves a sales quotation sch by transaction POID. Optionally includes item details.", responses = {
                        @ApiResponse(responseCode = "200", description = "Successfully retrieved sales quotation sch"),
                        @ApiResponse(responseCode = "404", description = "Sales quotation sch not found"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized")
        }, security = @SecurityRequirement(name = "bearerAuth"))
        @GetMapping("/{transactionPoid}")
        public ResponseEntity<?> getSalesQuotationSchByPoid(
                        @PathVariable Long transactionPoid,
                        @RequestParam(required = false, defaultValue = "false") Boolean includeDetails) {

                log.info("getSalesQuotationSchByPoid started for companyPoid={} groupPoid={}", UserContext.getCompanyPoid(), UserContext.getGroupPoid());
                SalesQuotationSchHdrDto dto = quotationSchService.getSalesQuotationSchByPoid(
                                transactionPoid, UserContext.getGroupPoid(), UserContext.getCompanyPoid(), includeDetails);
                log.info("getSalesQuotationSchByPoid completed for companyPoid={} groupPoid={}", UserContext.getCompanyPoid(),
                                UserContext.getGroupPoid());
                return success("Sales quotation sch fetched successfully", dto);
        }

        @Operation(summary = "Update Sales Quotation SCH", description = "Updates an existing sales quotation sch.", responses = {
                        @ApiResponse(responseCode = "200", description = "Successfully updated sales quotation sch"),
                        @ApiResponse(responseCode = "400", description = "Invalid input or validation error"),
                        @ApiResponse(responseCode = "404", description = "Sales quotation sch not found"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized")
        }, security = @SecurityRequirement(name = "bearerAuth"))
        @PutMapping("/{transactionPoid}")
        public ResponseEntity<?> updateSalesQuotationSch(
                        @PathVariable Long transactionPoid,
                        @Valid @RequestBody UpdateSalesQuotationSchRequest request) {

                log.info("updateSalesQuotationSch started for companyPoid={} groupPoid={}", UserContext.getCompanyPoid(), UserContext.getGroupPoid());
                SalesQuotationSchHdrDto dto = quotationSchService.updateSalesQuotationSch(
                                UserContext.getGroupPoid(), transactionPoid, request, UserContext.getCompanyPoid(), UserContext.getUserId());
                log.info("updateSalesQuotationSch completed for companyPoid={} groupPoid={}", UserContext.getCompanyPoid(), UserContext.getGroupPoid());
                return success("Sales quotation sch updated successfully", dto);
        }

        @Operation(summary = "Delete Sales Quotation SCH", description = "Deletes a sales quotation sch (soft delete).", responses = {
                        @ApiResponse(responseCode = "200", description = "Successfully deleted sales quotation sch"),
                        @ApiResponse(responseCode = "404", description = "Sales quotation sch not found"),
                        @ApiResponse(responseCode = "400", description = "Cannot delete"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized")
        }, security = @SecurityRequirement(name = "bearerAuth"))
        @DeleteMapping("/{transactionPoid}")
        public ResponseEntity<?> deleteSalesQuotationSch(
                        @PathVariable Long transactionPoid) {
                log.info("deleteSalesQuotationSch started for companyPoid={} groupPoid={}", UserContext.getCompanyPoid(), UserContext.getGroupPoid());
                quotationSchService.deleteSalesQuotationSch(UserContext.getGroupPoid(), transactionPoid, UserContext.getCompanyPoid());
                log.info("deleteSalesQuotationSch completed for companyPoid={} groupPoid={}", UserContext.getCompanyPoid(), UserContext.getGroupPoid());
                return success("Sales quotation sch deleted successfully", null);
        }

        @Operation(summary = "Get All Sales Quotation SCH", description = "Retrieves all sales quotation sch with optional filtering. Supports pagination.", responses = {
                        @ApiResponse(responseCode = "200", description = "Successfully retrieved sales quotation sch list"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized")
        }, security = @SecurityRequirement(name = "bearerAuth"))
        @GetMapping
        public ResponseEntity<?> getAllSalesQuotationSch(
                        @RequestParam(required = false) String quotationStatus,
                        @RequestParam(required = false) Long customerPoid,
                        @RequestParam(required = false) Long salesmanPoid,
                        @RequestParam(required = false) Long linePoid,
                        @RequestParam(required = false) String docRef,
                        @RequestParam(required = false, defaultValue = "0") Integer page,
                        @RequestParam(required = false, defaultValue = "20") Integer size,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime fromDate,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime toDate,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime validityFromDate,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime validityToDate,
                        @RequestParam(required = false) String search,
                        @RequestParam(required = false, defaultValue = "transactionDate") String sortBy,
                        @RequestParam(required = false, defaultValue = "DESC") String sortOrder) {

                Timestamp fromTs = (fromDate == null) ? null : Timestamp.from(fromDate.toInstant());
                Timestamp toTs = (toDate == null) ? null : Timestamp.from(toDate.toInstant());
                Timestamp validityFromTs = (validityFromDate == null) ? null
                                : Timestamp.from(validityFromDate.toInstant());
                Timestamp validityToTs = (validityToDate == null) ? null : Timestamp.from(validityToDate.toInstant());

                log.info("getAllSalesQuotationSch started for companyPoid={} groupPoid={}", UserContext.getCompanyPoid(), UserContext.getGroupPoid());

                // Build filter
                SalesQuotationSchFilter filter = new SalesQuotationSchFilter();
                filter.setCompanyPoid(UserContext.getCompanyPoid());
                filter.setCustomerPoid(customerPoid);
                filter.setSalesmanPoid(salesmanPoid);
                filter.setLinePoid(linePoid);
                filter.setQuotationStatus(quotationStatus);
                filter.setDocRef(docRef);
                filter.setSearch(search);
                filter.setFromDate(fromTs);
                filter.setToDate(toTs);
                filter.setValidityFromDate(validityFromTs);
                filter.setValidityToDate(validityToTs);
                filter.setPage(page);
                filter.setSize(size);
                filter.setSortBy(sortBy);
                filter.setSortOrder(sortOrder);

                SalesQuotationSchListResponse response = quotationSchService.search(filter, UserContext.getUserId());
                log.info("getAllSalesQuotationSch completed for companyPoid={} groupPoid={} totalElements={} totalPages={}",
                                UserContext.getCompanyPoid(), UserContext.getGroupPoid(), response.getTotalElements(), response.getTotalPages());
                return success("Sales quotation sch fetched successfully", response);
        }

        @Operation(summary = "Get Sales Quotation SCH List with Filters", description = "Retrieves sales quotation sch list with dynamic filters (docRef, status, customer name, transactionDate). Supports AND/OR operators.", responses = {
                        @ApiResponse(responseCode = "200", description = "Successfully retrieved sales quotation sch list"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized")
        }, security = @SecurityRequirement(name = "bearerAuth"))
        @PostMapping("/list")
        public ResponseEntity<?> getSalesQuotationSchListWithFilters(
                        @Valid @RequestBody FilterRequestDto filterRequest,
                        @ParameterObject Pageable pageable) {

                log.info("getSalesQuotationSchListWithFilters started for companyPoid={}", UserContext.getCompanyPoid());
                SalesQuotationSchListResponse response = quotationSchService.listSalesQuotationSchWithFilters(
                                filterRequest, UserContext.getCompanyPoid(), pageable);
                log.info("getSalesQuotationSchListWithFilters completed for companyPoid={} totalElements={} totalPages={}",
                                UserContext.getCompanyPoid(), response.getTotalElements(), response.getTotalPages());
                return success("Sales quotation sch list fetched successfully", response);
        }

        // ==================== VALIDATION APIs ====================

        @Operation(summary = "Validate Document Reference", description = "Checks if document reference is unique within the company")
        @GetMapping("/validate-doc-ref")
        public ResponseEntity<?> validateDocRef(
                        @RequestParam String docRef,
                        @RequestParam(required = false) Long transactionPoid) {

                log.info("validateDocRef started for companyPoid={} groupPoid={}", UserContext.getCompanyPoid(), UserContext.getGroupPoid());
                ValidationResponse response = quotationSchService.validateDocRef(docRef, transactionPoid);
                log.info("validateDocRef completed for companyPoid={} groupPoid={}", UserContext.getCompanyPoid(), UserContext.getGroupPoid());
                return success(response.getMessage(), response);
        }

        // ==================== ITEM DETAILS APIs ====================

        @Operation(summary = "Add Item Detail", description = "Adds a new item detail to the sales quotation sch.")
        @PostMapping("/{transactionPoid}/item-details")
        public ResponseEntity<?> addItemDetail(
                        @PathVariable Long transactionPoid,
                        @Valid @RequestBody CreateSalesQuotationSchItemDtlRequest request) {
                log.info("addItemDetail started for companyPoid={} groupPoid={}", UserContext.getCompanyPoid(), UserContext.getGroupPoid());
                SalesQuotationSchItemDtlDto dto = quotationSchService.addItemDetail(
                                transactionPoid, request, UserContext.getCompanyPoid(), UserContext.getUserId());
                log.info("addItemDetail completed for companyPoid={} groupPoid={}", UserContext.getCompanyPoid(), UserContext.getGroupPoid());
                return success("Item detail added successfully", dto);
        }

        @Operation(summary = "Update Item Detail", description = "Updates an existing item detail.")
        @PutMapping("/{transactionPoid}/item-details/{detRowId}")
        public ResponseEntity<?> updateItemDetail(
                        @PathVariable Long transactionPoid,
                        @PathVariable Long detRowId,
                        @Valid @RequestBody CreateSalesQuotationSchItemDtlRequest request) {
                log.info("updateItemDetail started for companyPoid={} groupPoid={}", UserContext.getCompanyPoid(), UserContext.getGroupPoid());
                SalesQuotationSchItemDtlDto dto = quotationSchService.updateItemDetail(
                                transactionPoid, detRowId, request, UserContext.getCompanyPoid(), UserContext.getUserId());
                log.info("updateItemDetail completed for companyPoid={} groupPoid={}", UserContext.getCompanyPoid(), UserContext.getGroupPoid());
                return success("Item detail updated successfully", dto);
        }

        @Operation(summary = "Delete Item Detail", description = "Deletes an item detail.")
        @DeleteMapping("/{transactionPoid}/item-details/{detRowId}")
        public ResponseEntity<?> deleteItemDetail(
                        @PathVariable Long transactionPoid,
                        @PathVariable Long detRowId) {
                log.info("deleteItemDetail started for companyPoid={} groupPoid={}", UserContext.getCompanyPoid(), UserContext.getGroupPoid());
                quotationSchService.deleteItemDetail(transactionPoid, detRowId, UserContext.getCompanyPoid());
                log.info("deleteItemDetail completed for companyPoid={} groupPoid={}", UserContext.getCompanyPoid(), UserContext.getGroupPoid());
                return success("Item detail deleted successfully", null);
        }

        @Operation(summary = "Get Item Details", description = "Retrieves all item details for a sales quotation sch.")
        @GetMapping("/{transactionPoid}/item-details")
        public ResponseEntity<?> getItemDetails(
                        @PathVariable Long transactionPoid) {
                log.info("getItemDetails started for companyPoid={} groupPoid={}", UserContext.getCompanyPoid(), UserContext.getGroupPoid());
                List<SalesQuotationSchItemDtlDto> itemDetails = quotationSchService.getItemDetails(
                                transactionPoid, UserContext.getCompanyPoid());
                log.info("getItemDetails completed for companyPoid={} groupPoid={}", UserContext.getCompanyPoid(), UserContext.getGroupPoid());
                return success("Item details fetched successfully", itemDetails);
        }

        @Operation(summary = "Refresh Previous Quotation Data", description = "Retrieves the customer details such as Customer type, credit period, and currency details for a sales quotation sch.")
        @GetMapping("/{transactionPoid}/refresh-previous-quotation-data")
        public ResponseEntity<?> refreshPreviousQuotationData(
                        @PathVariable Long transactionPoid,
                        @RequestParam(required = true) Long customerPoid) {
                log.info("getCustomerDetails by refreshing th previous quotation data started for companyPoid={} groupPoid={}",
                                UserContext.getCompanyPoid(), UserContext.getGroupPoid());
                List<SalesQuotationSchCustomerDetailsDto> customerDetails = quotationSchService
                                .refreshPreviousQuotationData(
                                                transactionPoid, UserContext.getGroupPoid(), UserContext.getCompanyPoid(), customerPoid);
                log.info("getCustomerDetails by refreshing th previous quotation data completed for companyPoid={} groupPoid={}",
                                UserContext.getCompanyPoid(), UserContext.getGroupPoid());
                return success("Customer details fetched successfully", customerDetails);
        }

        @Operation(summary = "Get Customer Address Details", description = "Retrieves customer address details including contact person, email, and mobile for a given customer. Calls PROC_GET_QTN_CUST_ADDRESS_V2.", responses = {
                        @ApiResponse(responseCode = "200", description = "Successfully retrieved customer address details"),
                        @ApiResponse(responseCode = "400", description = "Invalid input parameters"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized")
        }, security = @SecurityRequirement(name = "bearerAuth"))
        @GetMapping("/customer/{customerPoid}/address-details")
        public ResponseEntity<?> getCustomerAddressDetails(
                        @PathVariable Long customerPoid,
                        @RequestHeader("X-Company-Poid") Long companyPoid,
                        @RequestParam(required = false, defaultValue = "SALES") String addressType) {
                log.info("getCustomerAddressDetails started for customerPoid={} addressType={} companyPoid={}", 
                                customerPoid, addressType, companyPoid);
                
                // Convert userId string to Long (assuming userId is numeric)
                Long userPoid;
                try {
                        userPoid = Long.parseLong(UserContext.getUserId());
                } catch (NumberFormatException e) {
                        log.error("Invalid userId format: {}", UserContext.getUserId());
                        return badRequest("Invalid userId format. Expected numeric value.");
                }
                
                List<AddressDetailsResponse> addressDetails = quotationSchService.getCustomerAddress(
                                userPoid, customerPoid, addressType);
                
                log.info("getCustomerAddressDetails completed for customerPoid={} found {} address details", 
                                customerPoid, addressDetails != null ? addressDetails.size() : 0);
                return success("Customer address details fetched successfully", addressDetails);
        }

        // ==================== Stored Procedure Endpoints ====================

        @Operation(summary = "Import Items from Excel", description = "Import stock details to SALES_QUOTATION_ITEM_DTL through Excel file. Calls PROC_SALES_SCQTN_IMPORT_ITEMS.")
        @PostMapping("/{transactionPoid}/import-items")
        public ResponseEntity<?> importItems(
                        @PathVariable Long transactionPoid,
                        @RequestHeader("X-Login-User") String loginUser) {
                log.info("importItems started for transactionPoid={} companyPoid={}", transactionPoid, UserContext.getCompanyPoid());
                ImportItemsRequest request = new ImportItemsRequest(UserContext.getGroupPoid(), UserContext.getCompanyPoid(), transactionPoid, loginUser);
                StoredProcedureResponse response = quotationSchService.importItems(request);
                log.info("importItems completed for transactionPoid={}", transactionPoid);
                if (response.isSuccess()) {
                        return success(response.getMessage(), response);
                } else {
                        return badRequest(response.getErrorMessage());
                }
        }

        @Operation(summary = "Import Items from Excel File", description = "Upload and process Excel file to import stock details into SALES_QUOTATION_ITEM_DTL. Excel format: Stock POID, Quantity, Price, Discount, Stock Unit POID, Remarks, Item Type, Cost, Delivery Select.", responses = {
                        @ApiResponse(responseCode = "200", description = "Successfully imported items from Excel"),
                        @ApiResponse(responseCode = "400", description = "Invalid file or processing error"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized")
        }, security = @SecurityRequirement(name = "bearerAuth"))
        @PostMapping(value = "/{transactionPoid}/import-items-excel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public ResponseEntity<?> importItemsFromExcel(
                        @PathVariable Long transactionPoid,
                        @RequestHeader("X-Company-Poid") Long companyPoid,
                        @RequestParam("file") MultipartFile file) {
                log.info("importItemsFromExcel started for transactionPoid={} companyPoid={} fileName={}", 
                        transactionPoid, companyPoid, file != null ? file.getOriginalFilename() : "null");
                try {
                        ExcelImportResponse response = quotationSchService.importItemsFromExcel(
                                transactionPoid, companyPoid, UserContext.getUserId(), file);
                        log.info("importItemsFromExcel completed for transactionPoid={} successfulRows={} failedRows={}", 
                                transactionPoid, response.getSuccessfulRows(), response.getFailedRows());
                        if (response.isSuccess()) {
                                return success(response.getMessage(), response);
                        } else {
                                // Return response data even for failed imports so client can see error details
                                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                                        "statusCode", HttpStatus.BAD_REQUEST.value(),
                                        "success", false,
                                        "message", response.getMessage(),
                                        "result", Map.of("data", response)
                                ));
                        }
                } catch (Exception e) {
                        log.error("Unexpected error in importItemsFromExcel for transactionPoid={}", transactionPoid, e);
                        ExcelImportResponse errorResponse = new ExcelImportResponse();
                        errorResponse.setSuccess(false);
                        errorResponse.setMessage("Error processing import: " + (e.getMessage() != null ? e.getMessage() : "Unknown error"));
                        errorResponse.setTotalRows(0);
                        errorResponse.setSuccessfulRows(0);
                        errorResponse.setFailedRows(0);
                        errorResponse.setErrors(List.of("Error processing import: " + (e.getMessage() != null ? e.getMessage() : "Unknown error")));
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                                "statusCode", HttpStatus.BAD_REQUEST.value(),
                                "success", false,
                                "message", errorResponse.getMessage(),
                                "result", Map.of("data", errorResponse)
                        ));
                }
        }

        @Operation(summary = "Clear Items", description = "Clear stock detail table if quotation status is not in 'processing'. Calls PROC_SALES_SCQTN_ITEMS_CLEAR.")
        @PostMapping("/{transactionPoid}/clear-items")
        public ResponseEntity<?> clearItems(
                        @PathVariable Long transactionPoid) {
                log.info("clearItems started for transactionPoid={} companyPoid={}", transactionPoid, UserContext.getCompanyPoid());
                ClearItemsRequest request = new ClearItemsRequest(UserContext.getGroupPoid(), UserContext.getCompanyPoid(), transactionPoid);
                StoredProcedureResponse response = quotationSchService.clearItems(request);
                log.info("clearItems completed for transactionPoid={}", transactionPoid);
                if (response.isSuccess()) {
                        return success(response.getMessage(), response);
                } else {
                        return badRequest(response.getErrorMessage());
                }
        }

        @Operation(summary = "Refresh Cost & Rate", description = "Update cost and price from stock master to Sales quotation detail table and RFQ table. Calls PROC_SALES_SCQTN_REFRESH_DTL.")
        @PostMapping("/{transactionPoid}/refresh-detail")
        public ResponseEntity<?> refreshDetail(
                        @PathVariable Long transactionPoid,
                        @RequestParam(required = false) String quotedRate) {
                log.info("refreshDetail started for transactionPoid={} companyPoid={} quotedRate={}",
                                transactionPoid, UserContext.getCompanyPoid(), quotedRate);
                RefreshDetailRequest request = new RefreshDetailRequest(UserContext.getGroupPoid(), UserContext.getCompanyPoid(), transactionPoid,
                                quotedRate);
                StoredProcedureResponse response = quotationSchService.refreshDetail(request);
                log.info("refreshDetail completed for transactionPoid={}", transactionPoid);
                if (response.isSuccess()) {
                        return success(response.getMessage(), response);
                } else {
                        return badRequest(response.getErrorMessage());
                }
        }

        @Operation(summary = "Create RFQ", description = "Create Request For Quotation (RFQ) from the sales quotation. Calls PROC_SALES_SCQTN_RFQ_CREATE.")
        @PostMapping("/{transactionPoid}/create-rfq")
        public ResponseEntity<?> createRfq(
                        @PathVariable Long transactionPoid) {
                log.info("createRfq started for transactionPoid={} companyPoid={} user={}",
                                transactionPoid, UserContext.getCompanyPoid(), UserContext.getUserId());
                CreateRfqRequest request = new CreateRfqRequest(UserContext.getGroupPoid(), UserContext.getCompanyPoid(), transactionPoid, UserContext.getUserId());
                StoredProcedureResponse response = quotationSchService.createRfq(request);
                log.info("createRfq completed for transactionPoid={}", transactionPoid);
                if (response.isSuccess()) {
                        return success(response.getMessage(), response);
                } else {
                        return badRequest(response.getErrorMessage());
                }
        }

        @Operation(summary = "Create Delivery Note", description = "Create Delivery Note (DN) from quotation. Calls PROC_SALES_SCQTN_DN_CREATE.")
        @PostMapping("/{transactionPoid}/create-delivery-note")
        public ResponseEntity<?> createDeliveryNote(
                        @PathVariable Long transactionPoid) {
                log.info("createDeliveryNote started for transactionPoid={} companyPoid={} user={}",
                                transactionPoid, UserContext.getCompanyPoid(), UserContext.getUserId());
                CreateDeliveryNoteRequest request = new CreateDeliveryNoteRequest(UserContext.getGroupPoid(), UserContext.getCompanyPoid(),
                                transactionPoid, UserContext.getUserId());
                StoredProcedureResponse response = quotationSchService.createDeliveryNote(request);
                log.info("createDeliveryNote completed for transactionPoid={}", transactionPoid);
                if (response.isSuccess()) {
                        return success(response.getMessage(), response);
                } else {
                        return badRequest(response.getErrorMessage());
                }
        }

        @Operation(summary = "Select All Items", description = "Mark all items in the detail table to create delivery notes. Calls PROC_SALES_SCQTN_SELECT_ALL.")
        @PostMapping("/{transactionPoid}/select-all")
        public ResponseEntity<?> selectAll(
                        @PathVariable Long transactionPoid,
                        @RequestParam(required = true) String selectStatus) {
                log.info("selectAll started for transactionPoid={} companyPoid={}", transactionPoid, UserContext.getCompanyPoid());
                SelectAllRequest request = new SelectAllRequest(UserContext.getGroupPoid(), UserContext.getCompanyPoid(), transactionPoid, selectStatus);
                StoredProcedureResponse response = quotationSchService.selectAll(request);
                log.info("selectAll completed for transactionPoid={}", transactionPoid);
                if (response.isSuccess()) {
                        return success(response.getMessage(), response);
                } else {
                        return badRequest(response.getErrorMessage());
                }
        }

        @Operation(summary = "Validate Customer", description = "Validate customer or principal before save. Calls PROC_SCH_QTN_VALIDATE_CUSTOMER.")
        @PostMapping("/validate-customer")
        public ResponseEntity<?> validateCustomer(
                        @RequestParam(required = true) Long addressPoid) {
                log.info("validateCustomer started for addressPoid={} companyPoid={}", addressPoid, UserContext.getCompanyPoid());
                ValidateCustomerRequest request = new ValidateCustomerRequest(UserContext.getGroupPoid(), UserContext.getCompanyPoid(), null, null, addressPoid);
                ValidationResponse response = quotationSchService.validateCustomer(request);
                log.info("validateCustomer completed");
                return success(response.getMessage(), response);
        }

        @Operation(summary = "Update Quantity", description = "Update new details in Delivery Note and RFQ based on user input after DN and RFQ were created. Calls PROC_SALES_SCQTN_QTY_UPDATE.")
        @PostMapping("/{transactionPoid}/update-quantity")
        public ResponseEntity<?> updateQuantity(
                        @PathVariable Long transactionPoid,
                        @RequestHeader("X-Login-User") String loginUser) {
                log.info("updateQuantity started for transactionPoid={} companyPoid={} user={}",
                                transactionPoid, UserContext.getCompanyPoid(), UserContext.getUserId());
                Long userId = Long.parseLong(UserContext.getUserId());
                UpdateQuantityRequest request = new UpdateQuantityRequest(UserContext.getGroupPoid(), UserContext.getCompanyPoid(), transactionPoid,
                                userId, loginUser);
                StoredProcedureResponse response = quotationSchService.updateQuantity(request);
                log.info("updateQuantity completed for transactionPoid={}", transactionPoid);
                if (response.isSuccess()) {
                        return success(response.getMessage(), response);
                } else {
                        return badRequest(response.getErrorMessage());
                }
        }

        @Operation(summary = "Validate Checkbox", description = "Validate if delivery note has already been created when user unticks a checkbox. Calls PROC_SALES_SCQTN_CBOX_VALIDATE.")
        @PostMapping("/{transactionPoid}/validate-checkbox")
        public ResponseEntity<?> validateCheckbox(
                        @PathVariable Long transactionPoid,
                        @RequestParam(required = true) Long detRowId,
                        @RequestParam(required = true) String deliverySelect,
                        @RequestParam(required = true) Long stockPoid) {
                log.info("validateCheckbox started for transactionPoid={} detRowId={} companyPoid={}",
                                transactionPoid, detRowId, UserContext.getCompanyPoid());
                ValidateCheckboxRequest request = new ValidateCheckboxRequest(UserContext.getGroupPoid(), UserContext.getCompanyPoid(), transactionPoid,
                                detRowId, stockPoid, deliverySelect);
                ValidationResponse response = quotationSchService.validateCheckbox(request);
                log.info("validateCheckbox completed for transactionPoid={} detRowId={}", transactionPoid, detRowId);
                if (Boolean.TRUE.equals(response.getIsValid())) {
                        return success(response.getMessage(), response);
                } else {
                        return badRequest(response.getMessage());
                }
        }

        @Operation(summary = "Calculate", description = "Calculate item details price, qty, and other calculations if user selected 'Suppress Calculation' button. Calls PROC_SALES_SCQTN_DO_CALC.")
        @PostMapping("/{transactionPoid}/calculate")
        public ResponseEntity<?> calculate(
                        @PathVariable Long transactionPoid) {
                log.info("calculate started for transactionPoid={} companyPoid={} user={}",
                                transactionPoid, UserContext.getCompanyPoid(), UserContext.getUserId());
                CalculateRequest request = new CalculateRequest(UserContext.getGroupPoid(), UserContext.getCompanyPoid(), transactionPoid, UserContext.getUserId());
                StoredProcedureResponse response = quotationSchService.calculate(request);
                log.info("calculate completed for transactionPoid={}", transactionPoid);
                if (response.isSuccess()) {
                        return success(response.getMessage(), response);
                } else {
                        return badRequest(response.getErrorMessage());
                }
        }

}
