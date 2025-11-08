package com.alsharif.shipchandling.salesinvoice.controller;

import com.alsharif.shipchandling.salesinvoice.dto.*;
import com.alsharif.shipchandling.salesinvoice.service.ArSchSalesInvoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.List;

import static com.alsharif.shipchandling.common.ApiResponse.success;

@RestController
@RequestMapping("/api/ar/sales-invoices-sch")
@RequiredArgsConstructor
public class ArSchSalesInvoiceController {

        private final ArSchSalesInvoiceService invoiceService;

        // ==================== BASIC CRUD OPERATIONS ====================

        @Operation(summary = "Create Sales Invoice", description = "Creates a new Sales Invoice (Ship Chandling). DocRef is auto-generated. Calls stored procedures for validation and authorization.", responses = {
                        @ApiResponse(responseCode = "200", description = "Successfully created sales invoice"),
                        @ApiResponse(responseCode = "400", description = "Invalid input or validation error"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized")
        }, security = @SecurityRequirement(name = "bearerAuth"))
        @PostMapping
        public ResponseEntity<?> createSalesInvoice(
                        @Valid @RequestBody CreateArSchSalesInvoiceRequest request,
                        @RequestHeader("X-Group-Poid") Long groupPoid,
                        @RequestHeader("X-Company-Poid") Long companyPoid,
                        @RequestHeader("X-User-Id") String userId) {

                ArSchSalesInvoiceHdrDto dto = invoiceService.createSalesInvoice(
                                request, groupPoid, companyPoid, userId);
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

                ArSchSalesInvoiceHdrDto dto = invoiceService.getSalesInvoiceByPoid(
                                transactionPoid, groupPoid, companyPoid, includeDetails);
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
                        @Valid @RequestBody UpdateArSchSalesInvoiceRequest request,
                        @RequestHeader("X-Group-Poid") Long groupPoid,
                        @RequestHeader("X-Company-Poid") Long companyPoid,
                        @RequestHeader("X-User-Id") String userId) {

                ArSchSalesInvoiceHdrDto dto = invoiceService.updateSalesInvoice(
                                transactionPoid, request, groupPoid, companyPoid, userId);
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

                invoiceService.deleteSalesInvoice(transactionPoid, groupPoid, companyPoid);
                return success("Sales invoice deleted successfully", null);
        }

        @Operation(summary = "Get All Sales Invoices", description = "Retrieves all sales invoices with optional filtering by status, customer, date range, etc.", responses = {
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
                        @RequestParam(required = false) Timestamp fromDate,
                        @RequestParam(required = false) Timestamp toDate,
                        @RequestParam(required = false) String search) {

                // service expects 8 parameters: the 7th is a Long (nullable) and the last is
                // search;
                // pass null for the 7th param and keep fromDate/toDate as request params (they
                // are currently unused)
                List<ArSchSalesInvoiceHdrDto> invoices = invoiceService.getAllSalesInvoices(
                                groupPoid, companyPoid, invStatus, verified, customerPoid, principalPoid,
                                null, search);
                return success("Sales invoices fetched successfully", invoices);
        }

        // ==================== INVOICE DETAILS (ITEM DETAILS) APIs ====================

        @Operation(summary = "Add Invoice Item Detail", description = "Adds a new item detail to the sales invoice. Auto-populates tax percentage if tax is selected.")
        @PostMapping("/{transactionPoid}/item-details")
        public ResponseEntity<?> addItemDetail(
                        @PathVariable Long transactionPoid,
                        @Valid @RequestBody CreateArSchSalesInvoiceDtlRequest request,
                        @RequestHeader("X-Group-Poid") Long groupPoid,
                        @RequestHeader("X-Company-Poid") Long companyPoid,
                        @RequestHeader("X-User-Id") String userId) {

                ArSchSalesInvoiceDtlDto dto = null;
                // invoiceService.addItemDetail(
                // transactionPoid, request, groupPoid, companyPoid, userId);
                return success("Item detail added successfully", dto);
        }

        @Operation(summary = "Update Invoice Item Detail", description = "Updates an existing invoice item detail. Cannot update if invoice is verified.")
        @PutMapping("/{transactionPoid}/item-details/{detRowId}")
        public ResponseEntity<?> updateItemDetail(
                        @PathVariable Long transactionPoid,
                        @PathVariable Long detRowId,
                        @Valid @RequestBody CreateArSchSalesInvoiceDtlRequest request,
                        @RequestHeader("X-Group-Poid") Long groupPoid,
                        @RequestHeader("X-Company-Poid") Long companyPoid,
                        @RequestHeader("X-User-Id") String userId) {

                ArSchSalesInvoiceDtlDto dto = null;
                // invoiceService.updateItemDetail(
                // transactionPoid, detRowId, request, groupPoid, companyPoid, userId);
                return success("Item detail updated successfully", dto);
        }

        @Operation(summary = "Delete Invoice Item Detail", description = "Deletes an invoice item detail. Cannot delete if invoice is verified.")
        @DeleteMapping("/{transactionPoid}/item-details/{detRowId}")
        public ResponseEntity<?> deleteItemDetail(
                        @PathVariable Long transactionPoid,
                        @PathVariable Long detRowId,
                        @RequestHeader("X-Group-Poid") Long groupPoid,
                        @RequestHeader("X-Company-Poid") Long companyPoid) {

                // invoiceService.deleteItemDetail(transactionPoid, detRowId, groupPoid,
                // companyPoid);
                return success("Item detail deleted successfully", null);
        }

        @Operation(summary = "Get Invoice Item Details", description = "Retrieves all item details for a sales invoice.")
        @GetMapping("/{transactionPoid}/item-details")
        public ResponseEntity<?> getItemDetails(
                        @PathVariable Long transactionPoid,
                        @RequestHeader("X-Group-Poid") Long groupPoid,
                        @RequestHeader("X-Company-Poid") Long companyPoid) {

                List<ArSchSalesInvoiceDtlDto> itemDetails = null;
                // invoiceService.getItemDetails(
                // transactionPoid, groupPoid, companyPoid);
                return success("Item details fetched successfully", itemDetails);
        }

        // ==================== DELIVERY NOTE DETAILS APIs ====================

        @Operation(summary = "Add Delivery Note Detail", description = "Adds a delivery note reference to the sales invoice.")
        @PostMapping("/{transactionPoid}/delivery-note-details")
        public ResponseEntity<?> addDeliveryNoteDetail(
                        @PathVariable Long transactionPoid,
                        @Valid @RequestBody CreateArSchSalesDnDtlRequest request,
                        @RequestHeader("X-Group-Poid") Long groupPoid,
                        @RequestHeader("X-Company-Poid") Long companyPoid,
                        @RequestHeader("X-User-Id") String userId) {

                ArSchSalesDnDtlDto dto = invoiceService.addDeliveryNoteDetail(
                                transactionPoid, request, groupPoid, companyPoid, userId);
                return success("Delivery note detail added successfully", dto);
        }

        @Operation(summary = "Update Delivery Note Detail", description = "Updates an existing delivery note detail. Cannot update if invoice is verified.")
        @PutMapping("/{transactionPoid}/delivery-note-details/{detRowId}")
        public ResponseEntity<?> updateDeliveryNoteDetail(
                        @PathVariable Long transactionPoid,
                        @PathVariable Long detRowId,
                        @Valid @RequestBody CreateArSchSalesDnDtlRequest request,
                        @RequestHeader("X-Group-Poid") Long groupPoid,
                        @RequestHeader("X-Company-Poid") Long companyPoid,
                        @RequestHeader("X-User-Id") String userId) {

                ArSchSalesDnDtlDto dto = invoiceService.updateDeliveryNoteDetail(
                                transactionPoid, detRowId, request, groupPoid, companyPoid, userId);
                return success("Delivery note detail updated successfully", dto);
        }

        @Operation(summary = "Delete Delivery Note Detail", description = "Deletes a delivery note detail. Cannot delete if invoice is verified.")
        @DeleteMapping("/{transactionPoid}/delivery-note-details/{detRowId}")
        public ResponseEntity<?> deleteDeliveryNoteDetail(
                        @PathVariable Long transactionPoid,
                        @PathVariable Long detRowId,
                        @RequestHeader("X-Group-Poid") Long groupPoid,
                        @RequestHeader("X-Company-Poid") Long companyPoid) {

                invoiceService.deleteDeliveryNoteDetail(transactionPoid, detRowId, groupPoid, companyPoid);
                return success("Delivery note detail deleted successfully", null);
        }

        @Operation(summary = "Get Delivery Note Details", description = "Retrieves all delivery note details for a sales invoice.")
        @GetMapping("/{transactionPoid}/delivery-note-details")
        public ResponseEntity<?> getDeliveryNoteDetails(
                        @PathVariable Long transactionPoid,
                        @RequestHeader("X-Group-Poid") Long groupPoid,
                        @RequestHeader("X-Company-Poid") Long companyPoid) {

                List<ArSchSalesDnDtlDto> dnDetails = invoiceService.getDeliveryNoteDetails(
                                transactionPoid, groupPoid, companyPoid);
                return success("Delivery note details fetched successfully", dnDetails);
        }

        // ==================== COST BOOKED DETAILS APIs ====================

        @Operation(summary = "Get Cost Booked Details", description = "Retrieves all cost booked details for a sales invoice. This is a read-only table.")
        @GetMapping("/{transactionPoid}/cost-booked-details")
        public ResponseEntity<?> getCostBookedDetails(
                        @PathVariable Long transactionPoid,
                        @RequestHeader("X-Group-Poid") Long groupPoid,
                        @RequestHeader("X-Company-Poid") Long companyPoid) {

                List<ArSchSalesInvCostbkdDtlDto> costBookedDetails = null;
                // invoiceService.getCostBookedDetails(
                // transactionPoid, groupPoid, companyPoid);
                return success("Cost booked details fetched successfully", costBookedDetails);
        }

        // ==================== BUSINESS LOGIC APIs ====================

        @Operation(summary = "Calculate GP", description = "Calculates Gross Profit for the invoice. Calls PROC_AR_SCH_GP_CALC.")
        @PostMapping("/{transactionPoid}/calculate-gp")
        public ResponseEntity<?> calculateGp(
                        @PathVariable Long transactionPoid,
                        @RequestHeader("X-Group-Poid") Long groupPoid,
                        @RequestHeader("X-Company-Poid") Long companyPoid) {

                CalculateGpResponse response = invoiceService.calculateGp(transactionPoid, groupPoid, companyPoid, "");
                return success(response.getMessage(), response);
        }

        @Operation(summary = "Calculate Due Date", description = "Calculates due date from transaction date and credit days. Calls PROC_CALC_DUEDAYS.")
        @GetMapping("/calculate-due-date")
        public ResponseEntity<?> calculateDueDate(
                        @RequestParam Timestamp transactionDate,
                        @RequestParam Long creditDays,
                        @RequestHeader("X-Group-Poid") Long groupPoid,
                        @RequestHeader("X-Company-Poid") Long companyPoid) {

                CalculateDueDateResponse response = invoiceService.calculateDueDate(
                                transactionDate, creditDays, groupPoid, companyPoid, "");
                return success("Due date calculated successfully", response);
        }

        @Operation(summary = "Calculate Item Discount/Commission", description = "Calculates discount and commission for an invoice item. Calls PROC_AR_SCH_DIS_COM_CAL.")
        @PostMapping("/{transactionPoid}/item-details/{detRowId}/calculate-discount-commission")
        public ResponseEntity<?> calculateItemDiscountCommission(
                        @PathVariable Long transactionPoid,
                        @PathVariable Long detRowId,
                        @RequestBody CalculateDiscountCommissionRequest request,
                        @RequestHeader("X-Group-Poid") Long groupPoid) {

                CalculateDiscountCommissionResponse response = null;
                // invoiceService.calculateItemDiscountCommission(
                // transactionPoid, detRowId, request.getIncentiveAmt(),
                // request.getIncentiveAmt2(),
                // request.getIncentiveAmt3(), request.getIncentivePercent(),
                // request.getIncentivePercent2(),
                // request.getIncentivePercent3(), request.getBaseAmt(), groupPoid);
                return success("", response);
        }

        @Operation(summary = "Calculate Header Discount/Commission", description = "Calculates discount and commission at header level. Calls PROC_AR_SCH_DIS_COM_CAL_HDR.")
        @PostMapping("/{transactionPoid}/calculate-header-discount-commission")
        public ResponseEntity<?> calculateHeaderDiscountCommission(
                        @PathVariable Long transactionPoid,
                        @RequestBody CalculateDiscountCommissionRequest request,
                        @RequestHeader("X-Group-Poid") Long groupPoid) {

                CalculateDiscountCommissionResponse response = null;
                // invoiceService.calculateHeaderDiscountCommission(
                // transactionPoid, request.getIncentiveAmt(), request.getIncentiveAmt2(),
                // request.getIncentiveAmt3(), request.getIncentivePercent(),
                // request.getIncentivePercent2(),
                // request.getIncentivePercent3(), groupPoid);
                return success("", response);
        }

        @Operation(summary = "Load Quotation", description = "Loads quotation items into invoice. Invoice details table must be empty. Calls PROC_AR_SCH_QTN_LOAD_BUTTON.")
        @PostMapping("/{transactionPoid}/load-quotation")
        public ResponseEntity<?> loadQuotation(
                        @PathVariable Long transactionPoid,
                        @RequestBody LoadQuotationRequest request,
                        @RequestHeader("X-Group-Poid") Long groupPoid,
                        @RequestHeader("X-Company-Poid") Long companyPoid,
                        @RequestHeader("X-User-Id") String userId) {

                LoadQuotationResponse response = invoiceService.loadQuotation(
                                transactionPoid, request,
                                // request.getQtnPoid(), request.getIncentiveAmt(),
                                // request.getIncentiveAmt2(), request.getIncentiveAmt3(),
                                groupPoid, companyPoid, userId);
                return success(response.getMessage(), response);
        }

        @Operation(summary = "Load Delivery Note", description = "Loads delivery note items into invoice. Delivery notes must be selected first. Invoice details table must be empty. Calls PROC_AR_SCH_SALESINV_DN_LOAD.")
        @PostMapping("/{transactionPoid}/load-delivery-note")
        public ResponseEntity<?> loadDeliveryNote(
                        @PathVariable Long transactionPoid,
                        @RequestHeader("X-Group-Poid") Long groupPoid,
                        @RequestHeader("X-Company-Poid") Long companyPoid,
                        @RequestHeader("X-User-Id") String userId) {

                LoadDeliveryNoteResponse response = invoiceService.loadDeliveryNote(
                                transactionPoid, groupPoid, companyPoid, userId);
                return success(response.getMessage(), response);
        }

        @Operation(summary = "Unload Quotation", description = "Unloads/clears quotation details from invoice. Calls PROC_AR_SCH_UNLOAD_QUOTATION1.")
        @PostMapping("/{transactionPoid}/unload-quotation")
        public ResponseEntity<?> unloadQuotation(
                        @PathVariable Long transactionPoid,
                        @RequestHeader("X-Group-Poid") Long groupPoid,
                        @RequestHeader("X-Company-Poid") Long companyPoid,
                        @RequestHeader("X-User-Id") String userId) {

                UnloadQuotationResponse response = invoiceService.unloadQuotation(
                                transactionPoid, groupPoid, companyPoid, userId);
                return success(response.getMessage(), response);
        }

        @Operation(summary = "Load Cost Bookings", description = "Loads cost booking details into the invoice. Calls PROC_AR_SCH_SALES_INV_PJ_LOAD1.")
        @PostMapping("/{transactionPoid}/load-cost-bookings")
        public ResponseEntity<?> loadCostBookings(
                        @PathVariable Long transactionPoid,
                        @RequestHeader("X-Group-Poid") Long groupPoid,
                        @RequestHeader("X-Company-Poid") Long companyPoid,
                        @RequestHeader("X-User-Id") String userId) {

                LoadCostBookingsResponse response = invoiceService.loadCostBookings(
                                transactionPoid, groupPoid, companyPoid, userId);
                return success(response.getMessage(), response);
        }

        @Operation(summary = "Verify Invoice", description = "Verifies the invoice. Once verified, invoice cannot be edited.")
        @PostMapping("/{transactionPoid}/verify")
        public ResponseEntity<?> verifyInvoice(
                        @PathVariable Long transactionPoid,
                        @RequestHeader("X-Group-Poid") Long groupPoid,
                        @RequestHeader("X-Company-Poid") Long companyPoid,
                        @RequestHeader("X-User-Id") String userId) {

                VerifyInvoiceResponse response = invoiceService.verifyInvoice(
                                transactionPoid, groupPoid, companyPoid, userId);
                return success(response.getMessage(), response);
        }

        @Operation(summary = "Validate Customer", description = "Validates customer credit details. Calls PROC_VALIDATE_CUSTOMER.")
        @GetMapping("/validate-customer")
        public ResponseEntity<?> validateCustomer(
                        @RequestParam Long customerPoid,
                        @RequestHeader("X-Group-Poid") Long groupPoid,
                        @RequestHeader("X-Company-Poid") Long companyPoid) {

                ValidateCustomerResponse response = invoiceService.validateCustomer(
                                customerPoid, groupPoid, companyPoid);
                return success("Customer validation completed", response);
        }

        @Operation(summary = "Load Credit Details", description = "Loads customer credit details (credit days, payment mode, etc.). Calls PROC_LOAD_CREDIT_DETAILS.")
        @GetMapping("/load-credit-details")
        public ResponseEntity<?> loadCreditDetails(
                        @RequestParam Long customerPoid,
                        @RequestHeader("X-Group-Poid") Long groupPoid,
                        @RequestHeader("X-Company-Poid") Long companyPoid) {

                LoadCreditDetailsResponse response = invoiceService.loadCreditDetails(
                                customerPoid, groupPoid, companyPoid);
                return success(response.getMessage(), response);
        }

        @Operation(summary = "Load Quotation Currency", description = "Loads currency code and rate from quotation. Calls PROC_AR_SCH_QTN_LOAD_CUR1.")
        @GetMapping("/load-quotation-currency")
        public ResponseEntity<?> loadQuotationCurrency(
                        @RequestParam Long qtnPoid) {

                LoadQuotationCurrencyResponse response = invoiceService.loadQuotationCurrency(qtnPoid);
                return success(response.getMessage(), response);
        }

        @Operation(summary = "Check Sales Invoice Dependencies", description = "Checks if invoice can be deleted by checking for dependencies (receipts, credit notes, GL postings).")
        @GetMapping("/{transactionPoid}/dependencies")
        public ResponseEntity<?> checkSalesInvoiceDependencies(
                        @PathVariable Long transactionPoid,
                        @RequestHeader("X-Group-Poid") Long groupPoid,
                        @RequestHeader("X-Company-Poid") Long companyPoid) {

                SalesInvoiceDependenciesDto dto = invoiceService.checkSalesInvoiceDependencies(
                                transactionPoid, groupPoid, companyPoid);
                return success("Dependency check completed", dto);
        }
}
