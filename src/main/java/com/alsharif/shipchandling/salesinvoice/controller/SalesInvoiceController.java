        package com.alsharif.shipchandling.salesinvoice.controller;

        import com.alsharif.shipchandling.salesinvoice.dto.*;
        import com.alsharif.shipchandling.salesinvoice.dto.request.CalculateDiscountCommissionRequest;
        import com.alsharif.shipchandling.salesinvoice.dto.request.CreateSalesDnDtlRequest;
        import com.alsharif.shipchandling.salesinvoice.dto.request.CreateSalesInvoiceDtlRequest;
        import com.alsharif.shipchandling.salesinvoice.dto.request.CreateSalesInvoiceRequest;
        import com.alsharif.shipchandling.salesinvoice.dto.request.CreditDetailsRequest;
        import com.alsharif.shipchandling.salesinvoice.dto.request.LoadQuotationItemsRequest;
        import com.alsharif.shipchandling.salesinvoice.dto.request.UpdateSalesDnDtlRequest;
        import com.alsharif.shipchandling.salesinvoice.dto.request.UpdateSalesInvoiceDtlRequest;
        import com.alsharif.shipchandling.salesinvoice.dto.request.UpdateSalesInvoiceRequest;
        import com.alsharif.shipchandling.salesinvoice.dto.response.CalculateDiscountCommissionResponse;
        import com.alsharif.shipchandling.salesinvoice.dto.response.CalculateDueDateResponse;
        import com.alsharif.shipchandling.salesinvoice.dto.response.CalculateGpResponse;
        import com.alsharif.shipchandling.salesinvoice.dto.response.CreditDetailsResponse;
        import com.alsharif.shipchandling.salesinvoice.dto.response.LoadCostBookingsResponse;
        import com.alsharif.shipchandling.salesinvoice.dto.response.LoadDeliveryNoteResponse;
        import com.alsharif.shipchandling.salesinvoice.dto.response.LoadQuotationCurrencyResponse;
        import com.alsharif.shipchandling.salesinvoice.dto.response.LoadQuotationItemsResponse;
        import com.alsharif.shipchandling.salesinvoice.dto.response.UnloadQuotationResponse;
        import com.alsharif.shipchandling.salesinvoice.dto.response.ValidationResponse;
        import com.alsharif.shipchandling.salesinvoice.dto.response.VerifyInvoiceResponse;
        import com.alsharif.shipchandling.salesinvoice.service.SalesInvoiceService;
        import io.swagger.v3.oas.annotations.Operation;
        import io.swagger.v3.oas.annotations.responses.ApiResponse;
        import io.swagger.v3.oas.annotations.security.SecurityRequirement;
        import jakarta.validation.Valid;
        import lombok.RequiredArgsConstructor;
        import lombok.extern.slf4j.Slf4j;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
        import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.util.List;

import com.alsharif.shipchandling.salesinvoice.dto.PaginatedResponse;

import static com.alsharif.shipchandling.common.ApiResponse.success;

        @RestController
        @RequestMapping("/sales-invoice-sch")
        @RequiredArgsConstructor
        @Slf4j
        public class SalesInvoiceController {

                private final SalesInvoiceService invoiceService;

                // ==================== BASIC CRUD OPERATIONS ====================

                @Operation(summary = "Create Sales Invoice", description = "Creates a new Sales Invoice (Ship Chandling). DocRef is auto-generated. Calls stored procedures for validation and authorization.", responses = {
                                @ApiResponse(responseCode = "200", description = "Successfully created sales invoice"),
                                @ApiResponse(responseCode = "400", description = "Invalid input or validation error"),
                                @ApiResponse(responseCode = "401", description = "Unauthorized")
                }, security = @SecurityRequirement(name = "bearerAuth"))
                @PostMapping
                public ResponseEntity<?> createSalesInvoice(
                                @Valid @RequestBody CreateSalesInvoiceRequest request,
                                @RequestHeader("X-Group-Poid") Long groupPoid,
                                @RequestHeader("X-Company-Poid") Long companyPoid,
                                @RequestHeader("X-User-Id") String userId) {
                        log.info("Creating sales invoice with groupId: {} companyId: {} userId: {}", groupPoid, companyPoid, userId);
                        SalesInvoiceHdrDto dto = invoiceService.createSalesInvoice(
                                        request, groupPoid, companyPoid, userId);
                        log.info("Sales invoice created with transactionPoid: {}", dto.getTransactionPoid());
                        return success("Sales invoice created successfully", dto);
                }

                @Operation(summary = "Get Sales Invoice by ID", description = "Retrieves a sales invoice by transaction POID. Optionally includes detail tables.", responses = {
                                @ApiResponse(responseCode = "200", description = "Successfully retrieved sales invoice"),
                                @ApiResponse(responseCode = "404", description = "Sales invoice not found"),
                                @ApiResponse(responseCode = "401", description = "Unauthorized")
                }, security = @SecurityRequirement(name = "bearerAuth"))
                @GetMapping("/{transactionPoid}")
                public ResponseEntity<?> getSalesInvoiceByPoid(
                                @PathVariable Long transactionPoid,
                                @RequestHeader("X-Group-Poid") Long groupPoid,
                                @RequestHeader("X-Company-Poid") Long companyPoid,
                                @RequestParam(required = false, defaultValue = "false") Boolean includeDetails,
                                @RequestParam String documentId,
                                @RequestParam String actionRequested) {
                        log.info("Fetching sales invoice with transactionPoid: {} groupId: {} companyId: {}", transactionPoid, groupPoid, companyPoid);
                        SalesInvoiceHdrDto dto = invoiceService.getSalesInvoiceByPoid(
                                        transactionPoid, groupPoid, companyPoid, includeDetails);
                                        log.info("Sales invoice fetched with transactionPoid: {} groupId: {} companyId: {}", transactionPoid, groupPoid, companyPoid);
                        return success("Sales invoice fetched successfully", dto);
                }
 
                @Operation(summary = "Update Sales Invoice", description = "Updates an existing sales invoice. Cannot update if verified. Calls stored procedures for validation.", responses = {
                                @ApiResponse(responseCode = "200", description = "Successfully updated sales invoice"),
                                @ApiResponse(responseCode = "400", description = "Invalid input or validation error"),
                                @ApiResponse(responseCode = "404", description = "Sales invoice not found"),
                                @ApiResponse(responseCode = "401", description = "Unauthorized")
                }, security = @SecurityRequirement(name = "bearerAuth"))
                @PutMapping("/{transactionPoid}")
                public ResponseEntity<?> updateSalesInvoice(
                                @PathVariable Long transactionPoid,
                                @Valid @RequestBody UpdateSalesInvoiceRequest request,
                                @RequestHeader("X-Group-Poid") Long groupPoid,
                                @RequestHeader("X-Company-Poid") Long companyPoid,
                                @RequestHeader("X-User-Id") String userId) {
                                        log.info("Updating sales invoice with transactionPoid: {} groupId: {} companyId: {}", transactionPoid, groupPoid, companyPoid);
                        SalesInvoiceHdrDto dto = invoiceService.updateSalesInvoice(
                                        transactionPoid, request, groupPoid, companyPoid, userId);
                                        log.info("Sales invoice updated with transactionPoid: {} groupId: {} companyId: {}", transactionPoid, groupPoid, companyPoid);
                        return success("Sales invoice updated successfully", dto);
                }

                @Operation(summary = "Delete Sales Invoice", description = "Deletes a sales invoice (soft delete). Checks dependencies before deletion.", responses = {
                                @ApiResponse(responseCode = "200", description = "Successfully deleted sales invoice"),
                                @ApiResponse(responseCode = "404", description = "Sales invoice not found"),
                                @ApiResponse(responseCode = "400", description = "Cannot delete due to dependencies"),
                                @ApiResponse(responseCode = "401", description = "Unauthorized")
                }, security = @SecurityRequirement(name = "bearerAuth"))
                @DeleteMapping("/{transactionPoid}")
                public ResponseEntity<?> deleteSalesInvoice(
                                @PathVariable Long transactionPoid,
                                @RequestHeader("X-Group-Poid") Long groupPoid,
                                @RequestHeader("X-Company-Poid") Long companyPoid) {
log.info("Deleting sales invoice with transactionPoid: {} groupId: {} companyId: {}", transactionPoid, groupPoid, companyPoid);
                        invoiceService.deleteSalesInvoice(transactionPoid, groupPoid, companyPoid);
                        log.info("Sales invoice deleted with transactionPoid: {} groupId: {} companyId: {}", transactionPoid, groupPoid, companyPoid);
                        return success("Sales invoice deleted successfully", null);
                }

                @Operation(summary = "Get All Sales Invoices", description = "Retrieves all sales invoices with optional filtering by status, customer, date range, etc. Supports pagination with page and size parameters.", responses = {
                                @ApiResponse(responseCode = "200", description = "Successfully retrieved sales invoices"),
                                @ApiResponse(responseCode = "401", description = "Unauthorized")
                }, security = @SecurityRequirement(name = "bearerAuth"))
                @GetMapping
                public ResponseEntity<?> getAllSalesInvoices(
                                @RequestHeader("X-Group-Poid") Long groupPoid,
                                @RequestHeader("X-Company-Poid") Long companyPoid,
                                @RequestParam(required = false) String invStatus,
                                @RequestParam(required = false) String verified,
                                @RequestParam(required = false) Long customerPoid,
                                @RequestParam(required = false) Long principalPoid,
                                @RequestParam(required = false) String partyType,
                                @RequestParam(required = false, defaultValue = "0") Integer page,
                                @RequestParam(required = false, defaultValue = "10") Integer size,
                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime fromDate,
                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime toDate,
                                @RequestParam(required = false) String search) {

                        Timestamp fromTs = (fromDate == null) ? null : Timestamp.from(fromDate.toInstant());
                        Timestamp toTs = (toDate == null) ? null : Timestamp.from(toDate.toInstant());

                        log.info("Fetching all sales invoices with groupId: {} companyId: {} invStatus: {} verified: {} customerPoid: {} principalPoid: {} partyType: {} fromDate: {} toDate: {} search: {} page: {} size: {}", 
                                groupPoid, companyPoid, invStatus, verified, customerPoid, principalPoid, partyType, fromTs, toTs, search, page, size);
                        PaginatedResponse<SalesInvoiceHdrDto> invoices = invoiceService.getAllSalesInvoices(
                                        groupPoid, companyPoid, invStatus, verified, customerPoid, principalPoid,
                                        null, search, fromTs, toTs, page, size);
                        log.info("Fetched {} sales invoices (page {} of {}) with groupId: {} companyId: {}", 
                                invoices.getData().size(), invoices.getPage() + 1, invoices.getTotalPages(), groupPoid, companyPoid);
                        return success("Sales invoices fetched successfully", invoices);
                }

                // ==================== INVOICE DETAILS (ITEM DETAILS) APIs ====================

                @Operation(summary = "Add Invoice Item Detail", description = "Adds a new item detail to the sales invoice. Auto-populates tax percentage if tax is selected.")
                @PostMapping("/{transactionPoid}/item-details")
                public ResponseEntity<?> addItemDetail(
                                @PathVariable Long transactionPoid,
                                @Valid @RequestBody CreateSalesInvoiceDtlRequest request,
                                @RequestHeader("X-Group-Poid") Long groupPoid,
                                @RequestHeader("X-Company-Poid") Long companyPoid,
                                @RequestHeader("X-User-Id") String userId) {
                                        log.info("Adding item detail to sales invoice with transactionPoid: {} groupId: {} companyId: {}", transactionPoid, groupPoid, companyPoid);
                        SalesInvoiceDtlDto dto =  invoiceService.addInvoiceDetail(
                                        transactionPoid, request, groupPoid, companyPoid, userId);
                        log.info("Item detail added to sales invoice with transactionPoid: {} groupId: {} companyId: {}", transactionPoid, groupPoid, companyPoid);
                                        return success("Item detail added successfully", dto);
                }

                @Operation(summary = "Update Invoice Item Detail", description = "Updates an existing invoice item detail. Cannot update if invoice is verified.")
                @PutMapping("/{transactionPoid}/item-details/{detRowId}")
                public ResponseEntity<?> updateItemDetail(
                                @PathVariable Long transactionPoid,
                                @PathVariable Long detRowId,
                                @Valid @RequestBody UpdateSalesInvoiceDtlRequest request,
                                @RequestHeader("X-Group-Poid") Long groupPoid,
                                @RequestHeader("X-Company-Poid") Long companyPoid,
                                @RequestHeader("X-User-Id") String userId) {
log.info("Updating item detail in sales invoice with transactionPoid: {} detRowId: {} groupId: {} companyId: {}", transactionPoid, detRowId, groupPoid, companyPoid);
                        SalesInvoiceDtlDto dto = invoiceService.updateInvoiceDetail(
                                        transactionPoid, detRowId, request, groupPoid, companyPoid, userId);
                        log.info("Item detail updated in sales invoice with transactionPoid: {} detRowId: {} groupId: {} companyId: {}", transactionPoid, detRowId, groupPoid, companyPoid);
                        return success("Item detail updated successfully", dto);
                }

                @Operation(summary = "Delete Invoice Item Detail", description = "Deletes an invoice item detail. Cannot delete if invoice is verified.")
                @DeleteMapping("/{transactionPoid}/item-details/{detRowId}")
                public ResponseEntity<?> deleteItemDetail(
                                @PathVariable Long transactionPoid,
                                @PathVariable Long detRowId,
                                @RequestHeader("X-Group-Poid") Long groupPoid,
                                @RequestHeader("X-Company-Poid") Long companyPoid) {
log.info("Deleting item detail from sales invoice with transactionPoid: {} detRowId: {} groupId: {} companyId: {}", transactionPoid, detRowId, groupPoid, companyPoid);
                        invoiceService.deleteInvoiceDetail(transactionPoid, detRowId, groupPoid,
                                        companyPoid);
                        log.info("Item detail deleted from sales invoice with transactionPoid: {} detRowId: {} groupId: {} companyId: {}", transactionPoid, detRowId, groupPoid, companyPoid);
                        return success("Item detail deleted successfully", null);
                }

                @Operation(summary = "Get Invoice Item Details", description = "Retrieves all item details for a sales invoice.")
                @GetMapping("/{transactionPoid}/item-details")
                public ResponseEntity<?> getItemDetails(
                                @PathVariable Long transactionPoid,
                                @RequestHeader("X-Group-Poid") Long groupPoid,
                                @RequestHeader("X-Company-Poid") Long companyPoid) {
log.info("Fetching item details for sales invoice with transactionPoid: {} groupId: {} companyId: {}", transactionPoid, groupPoid, companyPoid);
                        List<SalesInvoiceDtlDto> itemDetails =  invoiceService.getInvoiceDetails(
                                        transactionPoid, groupPoid, companyPoid);
                                        log.info("Fetched {} item details for sales invoice with transactionPoid: {} groupId: {} companyId: {}", itemDetails != null ? itemDetails.size() : 0, transactionPoid, groupPoid, companyPoid);
                        return success("Item details fetched successfully", itemDetails);
                }

                // ==================== DELIVERY NOTE DETAILS APIs ====================

                @Operation(summary = "Add Delivery Note Detail", description = "Adds a delivery note reference to the sales invoice.")
                @PostMapping("/{transactionPoid}/delivery-note-details")
                public ResponseEntity<?> addDeliveryNoteDetail(
                                @PathVariable Long transactionPoid,
                                @Valid @RequestBody CreateSalesDnDtlRequest request,
                                @RequestHeader("X-Group-Poid") Long groupPoid,
                                @RequestHeader("X-Company-Poid") Long companyPoid,
                                @RequestHeader("X-User-Id") String userId) {
log.info("Adding delivery note detail to sales invoice with transactionPoid: {} groupId: {} companyId: {}", transactionPoid, groupPoid, companyPoid);
                        SalesDnDtlDto dto = invoiceService.addDeliveryNoteDetail(
                                        transactionPoid, request, groupPoid, companyPoid, userId);
                                        log.info("Delivery note detail added to sales invoice with transactionPoid: {} groupId: {} companyId: {}", transactionPoid, groupPoid, companyPoid);
                        return success("Delivery note detail added successfully", dto);
                }

                @Operation(summary = "Update Delivery Note Detail", description = "Updates an existing delivery note detail. Cannot update if invoice is verified.")
                @PutMapping("/{transactionPoid}/delivery-note-details/{detRowId}")
                public ResponseEntity<?> updateDeliveryNoteDetail(
                                @PathVariable Long transactionPoid,
                                @PathVariable Long detRowId,
                                @Valid @RequestBody UpdateSalesDnDtlRequest request,
                                @RequestHeader("X-Group-Poid") Long groupPoid,
                                @RequestHeader("X-Company-Poid") Long companyPoid,
                                @RequestHeader("X-User-Id") String userId) {
log.info("Updating delivery note detail in sales invoice with transactionPoid: {} detRowId: {} groupId: {} companyId: {}", transactionPoid, detRowId, groupPoid, companyPoid);
                        SalesDnDtlDto dto = invoiceService.updateDeliveryNoteDetail(
                                        transactionPoid, detRowId, request, groupPoid, companyPoid, userId);
                        log.info("Delivery note detail updated in sales invoice with transactionPoid: {} detRowId: {} groupId: {} companyId: {}", transactionPoid, detRowId, groupPoid, companyPoid);
                        return success("Delivery note detail updated successfully", dto);
                }

                @Operation(summary = "Delete Delivery Note Detail", description = "Deletes a delivery note detail. Cannot delete if invoice is verified.")
                @DeleteMapping("/{transactionPoid}/delivery-note-details/{detRowId}")
                public ResponseEntity<?> deleteDeliveryNoteDetail(
                                @PathVariable Long transactionPoid,
                                @PathVariable Long detRowId,
                                @RequestHeader("X-Group-Poid") Long groupPoid,
                                @RequestHeader("X-Company-Poid") Long companyPoid) {
log.info("Deleting delivery note detail from sales invoice with transactionPoid: {} detRowId: {} groupId: {} companyId: {}", transactionPoid, detRowId, groupPoid, companyPoid);
                        invoiceService.deleteDeliveryNoteDetail(transactionPoid, detRowId, groupPoid, companyPoid);
                        log.info("Delivery note detail deleted from sales invoice with transactionPoid: {} detRowId: {} groupId: {} companyId: {}", transactionPoid, detRowId, groupPoid, companyPoid);
                        return success("Delivery note detail deleted successfully", null);
                }

                @Operation(summary = "Get Delivery Note Details", description = "Retrieves all delivery note details for a sales invoice.")
                @GetMapping("/{transactionPoid}/delivery-note-details")
                public ResponseEntity<?> getDeliveryNoteDetails(
                                @PathVariable Long transactionPoid,
                                @RequestHeader("X-Group-Poid") Long groupPoid,
                                @RequestHeader("X-Company-Poid") Long companyPoid) {
log.info("Fetching delivery note details for sales invoice with transactionPoid: {} groupId: {} companyId: {}", transactionPoid, groupPoid, companyPoid);
                        List<SalesDnDtlDto> dnDetails = invoiceService.getDeliveryNoteDetails(
                                        transactionPoid, groupPoid, companyPoid);
                                        log.info("Fetched {} delivery note details for sales invoice with transactionPoid: {} groupId: {} companyId: {}", dnDetails != null ? dnDetails.size() : 0, transactionPoid, groupPoid, companyPoid);
                        return success("Delivery note details fetched successfully", dnDetails);
                }

                // ==================== COST BOOKED DETAILS APIs ====================

                @Operation(summary = "Get Cost Booked Details", description = "Retrieves all cost booked details for a sales invoice. This is a read-only table.")
                @GetMapping("/{transactionPoid}/cost-booked-details")
                public ResponseEntity<?> getCostBookedDetails(
                                @PathVariable Long transactionPoid,
                                @RequestHeader("X-Group-Poid") Long groupPoid,
                                @RequestHeader("X-Company-Poid") Long companyPoid) {

                        log.info("Fetching cost booked details for sales invoice with transactionPoid: {} groupId: {} companyId: {}", transactionPoid, groupPoid, companyPoid);
                        List<SalesInvCostbkdDtlDto> costBookedDetails = invoiceService.getCostBookedDetails(transactionPoid, groupPoid, companyPoid);
                        log.info("Fetched {} cost booked details for sales invoice with transactionPoid: {} groupId: {} companyId: {}", costBookedDetails != null ? costBookedDetails.size() : 0, transactionPoid, groupPoid, companyPoid);
                        return success("Cost booked details fetched successfully", costBookedDetails);
                }

                // ==================== BUSINESS LOGIC APIs ====================

                @Operation(summary = "Recalculates GP", description = "Recalculates Gross Profit for the invoice. Calls PROC_AR_SCH_GP_CALC.")
                @PostMapping("/{transactionPoid}/refresh-gp")
                public ResponseEntity<?> calculateGp(
                                @PathVariable Long transactionPoid,
                                @RequestHeader("X-Group-Poid") Long groupPoid,
                                @RequestHeader("X-Company-Poid") Long companyPoid) {
log.info("Calculating GP for sales invoice with transactionPoid: {} groupId: {} companyId: {}", transactionPoid, groupPoid, companyPoid);
                        CalculateGpResponse response = invoiceService.calculateGp(transactionPoid, groupPoid, companyPoid, "");
                        log.info("GP calculated for sales invoice with transactionPoid: {} groupId: {} companyId: {}", transactionPoid, groupPoid, companyPoid);
                        return success(response.getMessage(), response);
                }

                @Operation(summary = "Calculate Due Date", description = "Calculates due date from transaction date and credit days. Calls PROC_CALC_DUEDAYS.")
                @GetMapping("/{transactionPoid}/calculate-due-date")
                public ResponseEntity<?> calculateDueDate(
                                @PathVariable Long transactionPoid,
                                @RequestParam Timestamp transactionDate,
                                @RequestParam Long creditDays,
                                @RequestHeader("X-Group-Poid") Long groupPoid,
                                @RequestHeader("X-Company-Poid") Long companyPoid) {
log.info("Calculating due date for sales invoice with transactionPoid: {} groupId: {} companyId: {}", transactionPoid, groupPoid, companyPoid);
                        CalculateDueDateResponse response = invoiceService.calculateDueDate(transactionPoid,
                                        transactionDate, creditDays, groupPoid, companyPoid);
                        log.info("Due date calculated for sales invoice with transactionPoid: {} groupId: {} companyId: {}", transactionPoid, groupPoid, companyPoid);
                        return success("Due date calculated successfully", response);
                }

                @Operation(summary = "Calculate Item Discount/Commission", description = "Calculates discount and commission for an invoice item. Calls PROC_AR_SCH_DIS_COM_CAL.")
                @PostMapping("/{transactionPoid}/item-details/{detRowId}/calculate-discount-commission")
                public ResponseEntity<?> calculateItemDiscountCommission(
                                @PathVariable Long transactionPoid,
                                @PathVariable Long detRowId,
                                @RequestBody CalculateDiscountCommissionRequest request,
                                @RequestHeader("X-Group-Poid") Long groupPoid,
                                @RequestHeader("X-Company-Poid") Long companyPoid,
                                @RequestHeader("X-User-Id") Long userId) {
log.info("Calculating item discount/commission for sales invoice with transactionPoid: {} detRowId: {} groupId: {} companyId: {} userId: {}", transactionPoid, detRowId, groupPoid, companyPoid, userId);
                        CalculateDiscountCommissionResponse response =  invoiceService.calculateItemDiscountCommission(transactionPoid,  request, detRowId, groupPoid, companyPoid, userId);
                        log.info("Item discount/commission calculated for sales invoice with transactionPoid: {} detRowId: {} groupId: {} companyId: {} userId: {}", transactionPoid, detRowId, groupPoid, companyPoid, userId);
                        return success("", response);
                }

                @Operation(summary = "Calculate Header Discount/Commission", description = "Calculates discount and commission at header level. Calls PROC_AR_SCH_DIS_COM_CAL_HDR.")
                @PostMapping("/{transactionPoid}/item-details/{detRowId}/calculate-header-discount-commission")
                public ResponseEntity<?> calculateHeaderDiscountCommission(
                                @PathVariable Long transactionPoid,
                                @PathVariable Long detRowId,
                                @RequestBody CalculateDiscountCommissionRequest request,
                                @RequestHeader("X-Group-Poid") Long groupPoid,
                                @RequestHeader("X-Company-Poid") Long companyPoid,
                                @RequestHeader("X-User-Id") Long userId) {
log.info("Calculating header discount/commission for sales invoice with transactionPoid: {} detRowId: {} groupId: {} companyId: {} userId: {}", transactionPoid, detRowId, groupPoid, companyPoid, userId);
                        CalculateDiscountCommissionResponse response = invoiceService.calculateHeaderDiscountCommission(
                                        transactionPoid, request, detRowId, groupPoid, companyPoid, userId);
                        log.info("Header discount/commission calculated for sales invoice with transactionPoid: {} detRowId: {} groupId: {} companyId: {} userId: {}", transactionPoid, detRowId, groupPoid, companyPoid, userId);
                        return success("", response);
                }

                @Operation(summary = "Load Quotation", description = "Loads quotation items into invoice. Invoice details table must be empty. Calls PROC_AR_SCH_QTN_LOAD_BUTTON.")
                @PostMapping("/{transactionPoid}/load-quotation")
                public ResponseEntity<?> loadQuotationItems(
                                @PathVariable Long transactionPoid,
                                @RequestBody LoadQuotationItemsRequest request,
                                @RequestHeader("X-Group-Poid") Long groupPoid,
                                @RequestHeader("X-Company-Poid") Long companyPoid,
                                @RequestHeader("X-User-Id") String userId) {
log.info("Loading quotation items into sales invoice with transactionPoid: {} groupId: {} companyId: {} userId: {}", transactionPoid, groupPoid, companyPoid, userId);
                        LoadQuotationItemsResponse response = invoiceService.loadQuotationItems(
                                        transactionPoid, request,
                                        // request.getQtnPoid(), request.getIncentiveAmt(),
                                        // request.getIncentiveAmt2(), request.getIncentiveAmt3(),
                                        groupPoid, companyPoid, userId);
                                        SalesInvoiceHdrDto dto = invoiceService.getSalesInvoiceByPoid(
                                        transactionPoid, groupPoid, companyPoid, true);
                                        response.setInvoice(dto);
                                        log.info("Quotation items loaded into sales invoice with transactionPoid: {} groupId: {} companyId: {} userId: {}", transactionPoid, groupPoid, companyPoid, userId);
                        return success(response.getMessage(), response);
                }

                @Operation(summary = "Load Delivery Note", description = "Loads delivery note items into invoice. Delivery notes must be selected first. Invoice details table must be empty. Calls PROC_AR_SCH_SALESINV_DN_LOAD.")
                @PostMapping("/{transactionPoid}/load-delivery-note")
                public ResponseEntity<?> loadDeliveryNote(
                                @PathVariable Long transactionPoid,
                                @RequestHeader("X-Group-Poid") Long groupPoid,
                                @RequestHeader("X-Company-Poid") Long companyPoid,
                                @RequestHeader("X-User-Id") String userId) {
log.info("Loading delivery note into sales invoice with transactionPoid: {} groupId: {} companyId: {} userId: {}", transactionPoid, groupPoid, companyPoid, userId);
                        LoadDeliveryNoteResponse response = invoiceService.loadDeliveryNote(
                                        transactionPoid, groupPoid, companyPoid, userId);
                                        log.info("Delivery note loaded into sales invoice with transactionPoid: {} groupId: {} companyId: {} userId: {}", transactionPoid, groupPoid, companyPoid, userId);
                        return success(response.getMessage(), response);
                }

                @Operation(summary = "Unload Quotation", description = "Unloads/clears quotation details from invoice. Calls PROC_AR_SCH_UNLOAD_QUOTATION1.")
                @PostMapping("/{transactionPoid}/unload-quotation")
                public ResponseEntity<?> unloadQuotation(
                                @PathVariable Long transactionPoid,
                                @RequestHeader("X-Group-Poid") Long groupPoid,
                                @RequestHeader("X-Company-Poid") Long companyPoid,
                                @RequestHeader("X-User-Id") String userId) {
log.info("Unloading quotation from sales invoice with transactionPoid: {} groupId: {} companyId: {} userId: {}", transactionPoid, groupPoid, companyPoid, userId);
                        UnloadQuotationResponse response = invoiceService.unloadQuotation(
                                        transactionPoid, groupPoid, companyPoid, userId);
                                        log.info("Quotation unloaded from sales invoice with transactionPoid: {} groupId: {} companyId: {} userId: {}", transactionPoid, groupPoid, companyPoid, userId);
                        return success(response.getMessage(), response);
                }

                @Operation(summary = "Load Cost Bookings", description = "Loads cost booking details into the invoice. Calls PROC_AR_SCH_SALES_INV_PJ_LOAD1.")
                @PostMapping("/{transactionPoid}/load-cost-bookings")
                public ResponseEntity<?> loadCostBookings(
                                @PathVariable Long transactionPoid,
                                @RequestHeader("X-Group-Poid") Long groupPoid,
                                @RequestHeader("X-Company-Poid") Long companyPoid,
                                @RequestHeader("X-User-Id") String userId) {
log.info("Loading cost bookings into sales invoice with transactionPoid: {} groupId: {} companyId: {} userId: {}", transactionPoid, groupPoid, companyPoid, userId);
                        LoadCostBookingsResponse response = invoiceService.loadCostBookings(
                                        transactionPoid, groupPoid, companyPoid, userId);
                                        log.info("Cost bookings loaded into sales invoice with transactionPoid: {} groupId: {} companyId: {} userId: {}", transactionPoid, groupPoid, companyPoid, userId);
                        return success(response.getMessage(), response);
                }

                @Operation(summary = "Verify Invoice", description = "Verifies the invoice. Once verified, invoice cannot be edited.")
                @PostMapping("/{transactionPoid}/verify")
                public ResponseEntity<?> verifyInvoice(
                                @PathVariable Long transactionPoid,
                                @RequestHeader("X-Group-Poid") Long groupPoid,
                                @RequestHeader("X-Company-Poid") Long companyPoid,
                                @RequestHeader("X-User-Id") String userId) {
log.info("Verifying sales invoice with transactionPoid: {} groupId: {} companyId: {} userId: {}", transactionPoid, groupPoid, companyPoid, userId);
                        VerifyInvoiceResponse response = invoiceService.verifyInvoice(
                                        transactionPoid, groupPoid, companyPoid, userId);
                                        log.info("Sales invoice verified with transactionPoid: {} groupId: {} companyId: {} userId: {}", transactionPoid, groupPoid, companyPoid, userId);
                        return success(response.getMessage(), response);
                }

                @Operation(summary = "Validate Customer", description = "Validates customer credit details. Calls PROC_VALIDATE_CUSTOMER.")
                @GetMapping("/validate-customer")
                public ResponseEntity<?> validateCustomer(
                                @RequestParam Long customerPoid,
                                @RequestHeader("X-Group-Poid") Long groupPoid,
                                @RequestHeader("X-Company-Poid") Long companyPoid) {
log.info("Validating customer with customerPoid: {} groupId: {} companyId: {}", customerPoid, groupPoid, companyPoid);
                        ValidationResponse response = invoiceService.validateCustomer(
                                        customerPoid, groupPoid, companyPoid);
                                        log.info("Customer validated with customerPoid: {} groupId: {} companyId: {}", customerPoid, groupPoid, companyPoid);
                        return success("Customer validation completed", response);
                }

                @Operation(summary = "Load Credit Details", description = "Loads customer credit details (credit days, payment mode, etc.). Calls PROC_LOAD_CREDIT_DETAILS.")
                @PostMapping("/load-credit-details")
                public ResponseEntity<?> loadCreditDetails(
                                @RequestParam Long customerPoid,
                                @RequestBody CreditDetailsRequest request,
                                @RequestHeader("X-Group-Poid") Long groupPoid,
                                @RequestHeader("X-Company-Poid") Long companyPoid) {
log.info("Loading credit details for customerPoid: {} groupId: {} companyId: {}", customerPoid, groupPoid, companyPoid);
                        CreditDetailsResponse response = invoiceService.loadCreditDetails(
                                        customerPoid, groupPoid, companyPoid, request);
                                        log.info("Credit details loaded for customerPoid: {} groupId: {} companyId: {}", customerPoid, groupPoid, companyPoid);
                        return success(response.getMessage(), response);
                }

                @Operation(summary = "Load Quotation Currency", description = "Loads currency code and rate from quotation. Calls PROC_AR_SCH_QTN_LOAD_CUR1.")
                @GetMapping("/load-quotation-currency/{transactionPoid}")
                public ResponseEntity<?> loadQuotationCurrency(
                                @PathVariable Long transactionPoid,
                                @RequestParam String qtnPoid) {
log.info("Loading quotation currency for qtnPoid: {}", qtnPoid);
                        LoadQuotationCurrencyResponse response = invoiceService.loadQuotationCurrency(transactionPoid, qtnPoid);
                        log.info("Quotation currency loaded for qtnPoid: {}", qtnPoid);
                        return success(response.getMessage(), response);
                }

                @Operation(summary = "Check Sales Invoice Dependencies", description = "Checks if invoice can be deleted by checking for dependencies (receipts, credit notes, GL postings).")
                @GetMapping("/{transactionPoid}/dependencies")
                public ResponseEntity<?> checkSalesInvoiceDependencies(
                                @PathVariable Long transactionPoid,
                                @RequestHeader("X-Group-Poid") Long groupPoid,
                                @RequestHeader("X-Company-Poid") Long companyPoid) {
log.info("Checking dependencies for sales invoice with transactionPoid: {} groupId: {} companyId: {}", transactionPoid, groupPoid, companyPoid);
                        SalesInvoiceDependenciesDto dto = invoiceService.checkSalesInvoiceDependencies(
                                        transactionPoid, groupPoid, companyPoid);
                                        log.info("Dependencies checked for sales invoice with transactionPoid: {} groupId: {} companyId: {}", transactionPoid, groupPoid, companyPoid);
                        return success("Dependency check completed", dto);
                }
        }
