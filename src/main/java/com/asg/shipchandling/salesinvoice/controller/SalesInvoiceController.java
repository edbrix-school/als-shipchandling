package com.asg.shipchandling.salesinvoice.controller;

import com.asg.common.lib.dto.DeleteReasonDto;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.common.lib.enums.LogDetailsEnum;
import com.asg.common.lib.security.util.UserContext;
import com.asg.common.lib.service.DocumentDownloadHeaderService;
import com.asg.common.lib.service.LoggingService;
import com.asg.shipchandling.salesinvoice.dto.*;
import com.asg.shipchandling.salesinvoice.dto.request.CalculateDiscountCommissionRequest;
import com.asg.shipchandling.salesinvoice.dto.request.CreateSalesDnDtlRequest;
import com.asg.shipchandling.salesinvoice.dto.request.CreateSalesInvoiceDtlRequest;
import com.asg.shipchandling.salesinvoice.dto.request.CreateSalesInvoiceRequest;
import com.asg.shipchandling.salesinvoice.dto.request.CreditDetailsRequest;
import com.asg.shipchandling.salesinvoice.dto.request.LoadQuotationItemsRequest;
import com.asg.shipchandling.salesinvoice.dto.request.UpdateSalesDnDtlRequest;
import com.asg.shipchandling.salesinvoice.dto.request.UpdateSalesInvoiceDtlRequest;
import com.asg.shipchandling.salesinvoice.dto.request.UpdateSalesInvoiceRequest;
import com.asg.shipchandling.salesinvoice.dto.response.CalculateDiscountCommissionResponse;
import com.asg.shipchandling.salesinvoice.dto.response.RefreshGpProcResponse;
import com.asg.shipchandling.salesinvoice.dto.response.CreditDetailsResponse;
import com.asg.shipchandling.salesinvoice.dto.response.LoadCostBookingsResponse;
import com.asg.shipchandling.salesinvoice.dto.response.LoadDeliveryNoteResponse;
import com.asg.shipchandling.salesinvoice.dto.response.LoadQuotationSummaryResponse;
import com.asg.shipchandling.salesinvoice.dto.response.LoadQuotationAndCostBookingsResponse;
import com.asg.shipchandling.salesinvoice.dto.response.UnloadQuotationResponse;
import com.asg.shipchandling.salesinvoice.dto.response.ValidationResponse;
import com.asg.shipchandling.salesinvoice.dto.response.VerifyInvoiceResponse;
import com.asg.shipchandling.salesinvoice.service.SalesInvoiceService;
import com.asg.common.lib.security.util.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.asg.common.lib.annotation.AllowedAction;
import com.asg.common.lib.enums.UserRolesRightsEnum;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

import static com.asg.common.lib.dto.response.ApiResponse.error;
import static com.asg.shipchandling.common.ApiResponse.success;

@RestController
@RequestMapping("/v1/sales-invoice-sch")
@RequiredArgsConstructor
@Slf4j
public class SalesInvoiceController {

        private final SalesInvoiceService invoiceService;
        private final DocumentDownloadHeaderService downloadHeaderService;
        private final LoggingService loggingService;

        // ==================== BASIC CRUD OPERATIONS ====================

        @Operation(summary = "Create Sales Invoice", description = "Creates a new Sales Invoice (Ship Chandling). DocRef is auto-generated. Calls stored procedures for validation and authorization.", responses = {
                        @ApiResponse(responseCode = "200", description = "Successfully created sales invoice"),
                        @ApiResponse(responseCode = "400", description = "Invalid input or validation error"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized")
        }, security = @SecurityRequirement(name = "bearerAuth"))
        @PostMapping
        @AllowedAction(UserRolesRightsEnum.CREATE)
        public ResponseEntity<?> createSalesInvoice(
                        @Valid @RequestBody CreateSalesInvoiceRequest request) {
                log.info("Creating sales invoice with groupId: {} companyId: {} userId: {}", UserContext.getGroupPoid(), UserContext.getCompanyPoid(),
                                UserContext.getUserId());
                SalesInvoiceHdrDto dto = invoiceService.createSalesInvoice(
                                request, UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid().toString());
                log.info("Sales invoice created with transactionPoid: {}", dto.getTransactionPoid());
                return success("Sales invoice created successfully", dto);
        }

        @Operation(summary = "Get Sales Invoice by ID", description = "Retrieves a sales invoice by transaction POID. Optionally includes detail tables.", responses = {
                        @ApiResponse(responseCode = "200", description = "Successfully retrieved sales invoice"),
                        @ApiResponse(responseCode = "404", description = "Sales invoice not found"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized")
        }, security = @SecurityRequirement(name = "bearerAuth"))
        @GetMapping("/{transactionPoid}")
        @AllowedAction(UserRolesRightsEnum.VIEW)
        public ResponseEntity<?> getSalesInvoiceByPoid(
                        @PathVariable Long transactionPoid,
                        @RequestParam(required = false, defaultValue = "false") Boolean includeDetails) {
                log.info("Fetching sales invoice with transactionPoid: {} companyId: {} userPoid: {}", transactionPoid,
                                UserContext.getCompanyPoid(), UserContext.getUserId());
                SalesInvoiceHdrDto dto = invoiceService.getSalesInvoiceByPoid(
                                transactionPoid, UserContext.getCompanyPoid(), includeDetails);
                log.info("Sales invoice fetched with transactionPoid: {} companyId: {}", transactionPoid, UserContext.getCompanyPoid());
            loggingService.createLogSummaryEntry(LogDetailsEnum.VIEWED, UserContext.getDocumentId(), transactionPoid.toString());
                return success("Sales invoice fetched successfully", dto);
        }

        @Operation(summary = "Update Sales Invoice", description = "Updates an existing sales invoice. Cannot update if verified. Calls stored procedures for validation.", responses = {
                        @ApiResponse(responseCode = "200", description = "Successfully updated sales invoice"),
                        @ApiResponse(responseCode = "400", description = "Invalid input or validation error"),
                        @ApiResponse(responseCode = "404", description = "Sales invoice not found"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized")
        }, security = @SecurityRequirement(name = "bearerAuth"))
        @PutMapping("/{transactionPoid}")
        @AllowedAction(UserRolesRightsEnum.EDIT)
        public ResponseEntity<?> updateSalesInvoice(
                        @PathVariable Long transactionPoid,
                        @Valid @RequestBody UpdateSalesInvoiceRequest request) {
                log.info("Updating sales invoice with transactionPoid: {} groupId: {} companyId: {}", transactionPoid,
                                UserContext.getGroupPoid(), UserContext.getCompanyPoid());
                SalesInvoiceHdrDto dto = invoiceService.updateSalesInvoice(
                                transactionPoid, request, UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid().toString());
                log.info("Sales invoice updated with transactionPoid: {} groupId: {} companyId: {}", transactionPoid,
                                UserContext.getGroupPoid(), UserContext.getCompanyPoid());
                return success("Sales invoice updated successfully", dto);
        }

        @Operation(summary = "Delete Sales Invoice", description = "Deletes a sales invoice (soft delete). Checks dependencies before deletion.", responses = {
                        @ApiResponse(responseCode = "200", description = "Successfully deleted sales invoice"),
                        @ApiResponse(responseCode = "404", description = "Sales invoice not found"),
                        @ApiResponse(responseCode = "400", description = "Cannot delete due to dependencies"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized")
        }, security = @SecurityRequirement(name = "bearerAuth"))
        @DeleteMapping("/{transactionPoid}")
        @AllowedAction(UserRolesRightsEnum.DELETE)
        public ResponseEntity<?> deleteSalesInvoice(
                        @PathVariable Long transactionPoid,
                        @Valid @RequestBody(required = false) DeleteReasonDto deleteReasonDto) {
                log.info("Deleting sales invoice with transactionPoid: {} groupId: {} companyId: {}", transactionPoid,
                                UserContext.getGroupPoid(), UserContext.getCompanyPoid());
                invoiceService.deleteSalesInvoice(transactionPoid, UserContext.getGroupPoid(), UserContext.getCompanyPoid(),deleteReasonDto);
                log.info("Sales invoice deleted with transactionPoid: {} groupId: {} companyId: {}", transactionPoid,
                                UserContext.getGroupPoid(), UserContext.getCompanyPoid());
                return success("Sales invoice deleted successfully", null);
        }

        @Operation(summary = "Get All Sales Invoices", description = "Retrieves all sales invoices with filtering support. Uses POST method with filter request body. Supports pagination with page and size parameters.", responses = {
                        @ApiResponse(responseCode = "200", description = "Successfully retrieved sales invoices"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized")
        }, security = @SecurityRequirement(name = "bearerAuth"))
        @PostMapping("/list")
        @AllowedAction(UserRolesRightsEnum.VIEW)
        public ResponseEntity<?> listSalesInvoices(@ParameterObject Pageable pageable,
                                                   @RequestBody(required = false) FilterRequestDto filters,
                                                   @RequestParam(required = false) LocalDate startDate,
                                                   @RequestParam(required = false) LocalDate endDate) {
                return success("Sales invoices fetched successfully", invoiceService.listSalesInvoices(UserContext.getDocumentId(), filters, startDate, endDate, pageable));
        }

        // ==================== INVOICE DETAILS (ITEM DETAILS) APIs ====================

        @Operation(summary = "Add Invoice Item Detail", description = "Adds a new item detail to the sales invoice. Auto-populates tax percentage if tax is selected.")
        @PostMapping("/{transactionPoid}/item-details")
        @AllowedAction(UserRolesRightsEnum.CREATE)
        public ResponseEntity<?> addItemDetail(
                        @PathVariable Long transactionPoid,
                        @Valid @RequestBody CreateSalesInvoiceDtlRequest request) {
                log.info("Adding item detail to sales invoice with transactionPoid: {} groupId: {} companyId: {}",
                                transactionPoid, UserContext.getGroupPoid(), UserContext.getCompanyPoid());
                SalesInvoiceDtlDto dto = invoiceService.addInvoiceDetail(
                                transactionPoid, request, UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserId());
                log.info("Item detail added to sales invoice with transactionPoid: {} groupId: {} companyId: {}",
                                transactionPoid, UserContext.getGroupPoid(), UserContext.getCompanyPoid());
                return success("Item detail added successfully", dto);
        }

        @Operation(summary = "Update Invoice Item Detail", description = "Updates an existing invoice item detail. Cannot update if invoice is verified.")
        @PutMapping("/{transactionPoid}/item-details/{detRowId}")
        @AllowedAction(UserRolesRightsEnum.EDIT)
        public ResponseEntity<?> updateItemDetail(
                        @PathVariable Long transactionPoid,
                        @PathVariable Long detRowId,
                        @Valid @RequestBody UpdateSalesInvoiceDtlRequest request) {
                log.info("Updating item detail in sales invoice with transactionPoid: {} detRowId: {} groupId: {} companyId: {}",
                                transactionPoid, detRowId, UserContext.getGroupPoid(), UserContext.getCompanyPoid());
                SalesInvoiceDtlDto dto = invoiceService.updateInvoiceDetail(
                                transactionPoid, detRowId, request, UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid().toString());
                log.info("Item detail updated in sales invoice with transactionPoid: {} detRowId: {} groupId: {} companyId: {}",
                                transactionPoid, detRowId, UserContext.getGroupPoid(), UserContext.getCompanyPoid());
                return success("Item detail updated successfully", dto);
        }

        @Operation(summary = "Delete Invoice Item Detail", description = "Deletes an invoice item detail. Cannot delete if invoice is verified.")
        @DeleteMapping("/{transactionPoid}/item-details/{detRowId}")
        @AllowedAction(UserRolesRightsEnum.DELETE)
        public ResponseEntity<?> deleteItemDetail(
                        @PathVariable Long transactionPoid,
                        @PathVariable Long detRowId) {
                log.info("Deleting item detail from sales invoice with transactionPoid: {} detRowId: {} groupId: {} companyId: {}",
                                transactionPoid, detRowId, UserContext.getGroupPoid(), UserContext.getCompanyPoid());
                invoiceService.deleteInvoiceDetail(transactionPoid, detRowId, UserContext.getGroupPoid(),
                                UserContext.getCompanyPoid());
                log.info("Item detail deleted from sales invoice with transactionPoid: {} detRowId: {} groupId: {} companyId: {}",
                                transactionPoid, detRowId, UserContext.getGroupPoid(), UserContext.getCompanyPoid());
                return success("Item detail deleted successfully", null);
        }

        @Operation(summary = "Get Invoice Item Details", description = "Retrieves all item details for a sales invoice.")
        @GetMapping("/{transactionPoid}/item-details")
        @AllowedAction(UserRolesRightsEnum.VIEW)
        public ResponseEntity<?> getItemDetails(
                        @PathVariable Long transactionPoid) {
                log.info("Fetching item details for sales invoice with transactionPoid: {} groupId: {} companyId: {}",
                                transactionPoid, UserContext.getGroupPoid(), UserContext.getCompanyPoid());
                List<SalesInvoiceDtlDto> itemDetails = invoiceService.getInvoiceDetails(
                                transactionPoid, UserContext.getGroupPoid(), UserContext.getCompanyPoid());
                log.info("Fetched {} item details for sales invoice with transactionPoid: {} groupId: {} companyId: {}",
                                itemDetails != null ? itemDetails.size() : 0, transactionPoid, UserContext.getGroupPoid(), UserContext.getCompanyPoid());
                return success("Item details fetched successfully", itemDetails);
        }

        // ==================== DELIVERY NOTE DETAILS APIs ====================

        @Operation(summary = "Add Delivery Note Detail", description = "Adds a delivery note reference to the sales invoice.")
        @PostMapping("/{transactionPoid}/delivery-note-details")
        @AllowedAction(UserRolesRightsEnum.CREATE)
        public ResponseEntity<?> addDeliveryNoteDetail(
                        @PathVariable Long transactionPoid,
                        @Valid @RequestBody CreateSalesDnDtlRequest request) {
                log.info("Adding delivery note detail to sales invoice with transactionPoid: {} groupId: {} companyId: {}",
                                transactionPoid, UserContext.getGroupPoid(), UserContext.getCompanyPoid());
                SalesDnDtlDto dto = invoiceService.addDeliveryNoteDetail(
                                transactionPoid, request, UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserId());
                log.info("Delivery note detail added to sales invoice with transactionPoid: {} groupId: {} companyId: {}",
                                transactionPoid, UserContext.getGroupPoid(), UserContext.getCompanyPoid());
                return success("Delivery note detail added successfully", dto);
        }

        @Operation(summary = "Update Delivery Note Detail", description = "Updates an existing delivery note detail. Cannot update if invoice is verified.")
        @PutMapping("/{transactionPoid}/delivery-note-details/{detRowId}")
        @AllowedAction(UserRolesRightsEnum.EDIT)
        public ResponseEntity<?> updateDeliveryNoteDetail(
                        @PathVariable Long transactionPoid,
                        @PathVariable Long detRowId,
                        @Valid @RequestBody UpdateSalesDnDtlRequest request) {
                log.info("Updating delivery note detail in sales invoice with transactionPoid: {} detRowId: {} groupId: {} companyId: {}",
                                transactionPoid, detRowId, UserContext.getGroupPoid(), UserContext.getCompanyPoid());
                SalesDnDtlDto dto = invoiceService.updateDeliveryNoteDetail(
                                transactionPoid, detRowId, request, UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserId());
                log.info("Delivery note detail updated in sales invoice with transactionPoid: {} detRowId: {} groupId: {} companyId: {}",
                                transactionPoid, detRowId, UserContext.getGroupPoid(), UserContext.getCompanyPoid());
                return success("Delivery note detail updated successfully", dto);
        }

        @Operation(summary = "Delete Delivery Note Detail", description = "Deletes a delivery note detail. Cannot delete if invoice is verified.")
        @DeleteMapping("/{transactionPoid}/delivery-note-details/{detRowId}")
        @AllowedAction(UserRolesRightsEnum.DELETE)
        public ResponseEntity<?> deleteDeliveryNoteDetail(
                        @PathVariable Long transactionPoid,
                        @PathVariable Long detRowId) {
                log.info("Deleting delivery note detail from sales invoice with transactionPoid: {} detRowId: {} groupId: {} companyId: {}",
                                transactionPoid, detRowId, UserContext.getGroupPoid(), UserContext.getCompanyPoid());
                invoiceService.deleteDeliveryNoteDetail(transactionPoid, detRowId, UserContext.getGroupPoid(), UserContext.getCompanyPoid());
                log.info("Delivery note detail deleted from sales invoice with transactionPoid: {} detRowId: {} groupId: {} companyId: {}",
                                transactionPoid, detRowId, UserContext.getGroupPoid(), UserContext.getCompanyPoid());
                return success("Delivery note detail deleted successfully", null);
        }

        @Operation(summary = "Get Delivery Note Details", description = "Retrieves all delivery note details for a sales invoice.")
        @GetMapping("/{transactionPoid}/delivery-note-details")
        @AllowedAction(UserRolesRightsEnum.VIEW)
        public ResponseEntity<?> getDeliveryNoteDetails(
                        @PathVariable Long transactionPoid) {
                log.info("Fetching delivery note details for sales invoice with transactionPoid: {} groupId: {} companyId: {}",
                                transactionPoid, UserContext.getGroupPoid(), UserContext.getCompanyPoid());
                List<SalesDnDtlDto> dnDetails = invoiceService.getDeliveryNoteDetails(
                                transactionPoid, UserContext.getGroupPoid(), UserContext.getCompanyPoid());
                log.info("Fetched {} delivery note details for sales invoice with transactionPoid: {} groupId: {} companyId: {}",
                                dnDetails != null ? dnDetails.size() : 0, transactionPoid, UserContext.getGroupPoid(), UserContext.getCompanyPoid());
                return success("Delivery note details fetched successfully", dnDetails);
        }

        // ==================== COST BOOKED DETAILS APIs ====================

        @Operation(summary = "Get Cost Booked Details", description = "Retrieves all cost booked details for a sales invoice. This is a read-only table.")
        @GetMapping("/{transactionPoid}/cost-booked-details")
        @AllowedAction(UserRolesRightsEnum.VIEW)
        public ResponseEntity<?> getCostBookedDetails(
                        @PathVariable Long transactionPoid) {

                log.info("Fetching cost booked details for sales invoice with transactionPoid: {} groupId: {} companyId: {}",
                                transactionPoid, UserContext.getGroupPoid(), UserContext.getCompanyPoid());
                List<SalesInvCostbkdDtlDto> costBookedDetails = invoiceService.getCostBookedDetails(transactionPoid,
                                UserContext.getGroupPoid(), UserContext.getCompanyPoid());
                log.info("Fetched {} cost booked details for sales invoice with transactionPoid: {} groupId: {} companyId: {}",
                                costBookedDetails != null ? costBookedDetails.size() : 0, transactionPoid, UserContext.getGroupPoid(),
                                UserContext.getCompanyPoid());
                return success("Cost booked details fetched successfully", costBookedDetails);
        }

        // ==================== BUSINESS LOGIC APIs ====================

        @Operation(summary = "Recalculates GP", description = "Recalculates discount/commission/GP for the invoice. Calls PROC_AR_SCH_DIS_COM_CAL.")
        @PostMapping("/{transactionPoid}/refresh-gp")
        @AllowedAction(UserRolesRightsEnum.EDIT)
        public ResponseEntity<?> calculateGp(
                        @PathVariable Long transactionPoid,
                        @RequestBody CalculateDiscountCommissionRequest request) {
                log.info("Calculating GP for sales invoice with transactionPoid: {} groupId: {} companyId: {}",
                                transactionPoid, UserContext.getGroupPoid(), UserContext.getCompanyPoid());
                RefreshGpProcResponse response = invoiceService.calculateGp(
                                transactionPoid, request, UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid());
                log.info("GP calculated for sales invoice with transactionPoid: {} groupId: {} companyId: {}",
                                transactionPoid, UserContext.getGroupPoid(), UserContext.getCompanyPoid());
                return success(response.getMessage(), response);
        }

        //not used anymore
        @Operation(summary = "Calculate Due Date", description = "Calculates due date from transaction date and credit days. Calls PROC_CALC_DUEDAYS.")
        @GetMapping("/{customerPoid}/calculate-due-date")
        @AllowedAction(UserRolesRightsEnum.VIEW)
        public ResponseEntity<?> calculateDueDate(
                        @PathVariable Long customerPoid,
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate docDate,
                        @RequestParam Long creditDays) {
                log.info("Calculating due date for sales invoice with customerPoid: {} groupId: {} companyId: {}",
                        customerPoid, UserContext.getGroupPoid(), UserContext.getCompanyPoid());
                CreditDetailsResponse response = invoiceService.calculateDueDate(customerPoid,
                        docDate, creditDays);
                log.info("Due date calculated for sales invoice with customerPoid: {} groupId: {} companyId: {}",
                        customerPoid, UserContext.getGroupPoid(), UserContext.getCompanyPoid());
                return success("Due date calculated successfully", response);
        }

        @Operation(summary = "Calculate Item Discount/Commission", description = "Calculates discount and commission for an invoice item. Calls PROC_AR_SCH_DIS_COM_CAL.")
        @PostMapping("/{transactionPoid}/item-details/{detRowId}/calculate-discount-commission")
        @AllowedAction(UserRolesRightsEnum.EDIT)
        public ResponseEntity<?> calculateItemDiscountCommission(
                        @PathVariable Long transactionPoid,
                        @PathVariable Long detRowId,
                        @RequestBody CalculateDiscountCommissionRequest request) {
                Long userId = Long.parseLong(UserContext.getUserId());
                log.info("Calculating item discount/commission for sales invoice with transactionPoid: {} detRowId: {} groupId: {} companyId: {} userId: {}",
                                transactionPoid, detRowId, UserContext.getGroupPoid(), UserContext.getCompanyPoid(), userId);
                CalculateDiscountCommissionResponse response = invoiceService.calculateItemDiscountCommission(
                                transactionPoid, request, detRowId, UserContext.getGroupPoid(), UserContext.getCompanyPoid(), userId);
                log.info("Item discount/commission calculated for sales invoice with transactionPoid: {} detRowId: {} groupId: {} companyId: {} userId: {}",
                                transactionPoid, detRowId, UserContext.getGroupPoid(), UserContext.getCompanyPoid(), userId);
                return success("", response);
        }

        @Operation(summary = "Calculate Header Discount/Commission", description = "Calculates discount and commission at header level. Calls PROC_AR_SCH_DIS_COM_CAL_HDR.")
        @PostMapping("/{transactionPoid}/item-details/{detRowId}/calculate-header-discount-commission")
        @AllowedAction(UserRolesRightsEnum.EDIT)
        public ResponseEntity<?> calculateHeaderDiscountCommission(
                        @PathVariable Long transactionPoid,
                        @PathVariable Long detRowId,
                        @RequestBody CalculateDiscountCommissionRequest request) {
                Long userId = Long.parseLong(UserContext.getUserId());
                log.info("Calculating header discount/commission for sales invoice with transactionPoid: {} detRowId: {} groupId: {} companyId: {} userId: {}",
                                transactionPoid, detRowId, UserContext.getGroupPoid(), UserContext.getCompanyPoid(), userId);
                CalculateDiscountCommissionResponse response = invoiceService.calculateHeaderDiscountCommission(
                                transactionPoid, request, detRowId, UserContext.getGroupPoid(), UserContext.getCompanyPoid(), userId);
                log.info("Header discount/commission calculated for sales invoice with transactionPoid: {} detRowId: {} groupId: {} companyId: {} userId: {}",
                                transactionPoid, detRowId, UserContext.getGroupPoid(), UserContext.getCompanyPoid(), userId);
                return success("", response);
        }

        @Operation(summary = "Load Quotation", description = "Loads quotation items into invoice and cost booking details. Calls PROC_AR_SCH_UNLOAD_QUOTATION1, PROC_AR_SCH_QTN_LOAD_BUTTON, and PROC_AR_SCH_SALES_INV_PJ_LOAD1.")
        @PostMapping("/{transactionPoid}/load-quotation")
        @AllowedAction(UserRolesRightsEnum.CREATE)
        public ResponseEntity<?> loadQuotationItems(
                        @PathVariable Long transactionPoid,
                        @RequestBody LoadQuotationItemsRequest request) {
                log.info("Loading quotation items into sales invoice with transactionPoid: {} groupId: {} companyId: {} userId: {}",
                                transactionPoid, UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserId());
                LoadQuotationAndCostBookingsResponse response = invoiceService.loadQuotationAndCostBookings(
                                transactionPoid, request,
                                UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserId());
                log.info("Quotation items loaded into sales invoice with transactionPoid: {} groupId: {} companyId: {} userId: {}",
                                transactionPoid, UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserId());
                return success("", response);
        }

        @Operation(summary = "Load Delivery Note", description = "Loads delivery note items into invoice. Delivery notes must be selected first. Invoice details table must be empty. Calls PROC_AR_SCH_SALESINV_DN_LOAD.")
        @PostMapping("/{transactionPoid}/load-delivery-note")
        @AllowedAction(UserRolesRightsEnum.CREATE)
        public ResponseEntity<?> loadDeliveryNote(
                        @PathVariable Long transactionPoid) {
                log.info("Loading delivery note into sales invoice with transactionPoid: {} groupId: {} companyId: {} userId: {}",
                                transactionPoid, UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserId());
                LoadDeliveryNoteResponse response = invoiceService.loadDeliveryNote(
                                transactionPoid, UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserId());
                log.info("Delivery note loaded into sales invoice with transactionPoid: {} groupId: {} companyId: {} userId: {}",
                                transactionPoid, UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserId());
                return success(response.getMessage(), response);
        }

        @Operation(summary = "Unload Quotation", description = "Unloads/clears quotation details from invoice. Calls PROC_AR_SCH_UNLOAD_QUOTATION1.")
        @PostMapping("/{transactionPoid}/unload-quotation")
        @AllowedAction(UserRolesRightsEnum.EDIT)
        public ResponseEntity<?> unloadQuotation(
                        @PathVariable Long transactionPoid,
                        @RequestParam Long qtnPoid) {
                log.info("Unloading quotation from sales invoice with transactionPoid: {} groupId: {} companyId: {} userId: {}",
                                transactionPoid, UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserId());
                UnloadQuotationResponse response = invoiceService.unloadQuotation(
                                transactionPoid, qtnPoid, UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserId());
                log.info("Quotation unloaded from sales invoice with transactionPoid: {} groupId: {} companyId: {} userId: {}",
                                transactionPoid, UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserId());
                return success(response.getMessage(), response);
        }

        @Operation(summary = "Load Cost Bookings", description = "Loads cost booking details into the invoice. Calls PROC_AR_SCH_SALES_INV_PJ_LOAD1.")
        @PostMapping("/{transactionPoid}/load-cost-bookings")
        @AllowedAction(UserRolesRightsEnum.CREATE)
        public ResponseEntity<?> loadCostBookings(
                        @PathVariable Long transactionPoid) {
                log.info("Loading cost bookings into sales invoice with transactionPoid: {} groupId: {} companyId: {} userId: {}",
                                transactionPoid, UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserId());
                LoadCostBookingsResponse response = invoiceService.loadCostBookings(
                                transactionPoid, UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserId());
                log.info("Cost bookings loaded into sales invoice with transactionPoid: {} groupId: {} companyId: {} userId: {}",
                                transactionPoid, UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserId());
                return success(response.getMessage(), response);
        }

        @Operation(summary = "Verify Invoice", description = "Verifies the invoice. Once verified, invoice cannot be edited.")
        @PostMapping("/{transactionPoid}/verify")
        @AllowedAction(UserRolesRightsEnum.EDIT)
        public ResponseEntity<?> verifyInvoice(
                        @PathVariable Long transactionPoid) {
                log.info("Verifying sales invoice with transactionPoid: {} groupId: {} companyId: {} userId: {}",
                                transactionPoid, UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserId());
                VerifyInvoiceResponse response = invoiceService.verifyInvoice(
                                transactionPoid, UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserId());
                log.info("Sales invoice verified with transactionPoid: {} groupId: {} companyId: {} userId: {}",
                                transactionPoid, UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserId());
                return success(response.getMessage(), response);
        }

        @Operation(summary = "Validate Customer", description = "Validates customer credit details. Calls PROC_VALIDATE_CUSTOMER.")
        @GetMapping("/validate-customer")
        @AllowedAction(UserRolesRightsEnum.VIEW)
        public ResponseEntity<?> validateCustomer(
                        @RequestParam Long customerPoid) {
                log.info("Validating customer with customerPoid: {} groupId: {} companyId: {}", customerPoid, UserContext.getGroupPoid(),
                                UserContext.getCompanyPoid());
                ValidationResponse response = invoiceService.validateCustomer(
                                customerPoid, UserContext.getGroupPoid(), UserContext.getCompanyPoid());
                log.info("Customer validated with customerPoid: {} groupId: {} companyId: {}", customerPoid, UserContext.getGroupPoid(),
                                UserContext.getCompanyPoid());
                return success("Customer validation completed", response);
        }

        @Operation(summary = "Load Credit Details", description = "Loads customer credit details (credit days, payment mode, etc.). Calls PROC_LOAD_CREDIT_DETAILS.")
        @PostMapping("/load-credit-details")
        @AllowedAction(UserRolesRightsEnum.VIEW)
        public ResponseEntity<?> loadCreditDetails(
                        @RequestBody CreditDetailsRequest request) {
                log.info("Loading credit details for partyPoid: {} groupId: {} companyId: {}", request.getPartyPoid(),
                                UserContext.getGroupPoid(), UserContext.getCompanyPoid());
                CreditDetailsResponse response = invoiceService.loadCreditDetails(
                                UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getDocumentId(), request);
                log.info("Credit details loaded for partyPoid: {} groupId: {} companyId: {}", request.getPartyPoid(),
                                UserContext.getGroupPoid(), UserContext.getCompanyPoid());
                return success(response.getMessage(), response);
        }

        @Operation(summary = "Load Quotation Summary", description = "Loads quotation summary fields using the cursor returned by PROC_AR_SCH_QTN_LOAD_CUR1.")
        @PostMapping("/load-quotation-summary/{transactionPoid}")
        @AllowedAction(UserRolesRightsEnum.EDIT)
        public ResponseEntity<?> loadQuotationSummary(
                        @PathVariable Long transactionPoid,
                        @RequestParam Long qtnPoid) {
                log.info("Loading quotation summary for qtnPoid: {}", qtnPoid);
                LoadQuotationSummaryResponse response = invoiceService.loadQuotationSummary(transactionPoid, qtnPoid);
                log.info("Quotation summary loaded for qtnPoid: {}", qtnPoid);
                return success(response.getMessage(), response);
        }

        @Operation(summary = "Check Sales Invoice Dependencies", description = "Checks if invoice can be deleted by checking for dependencies (receipts, credit notes, GL postings).")
        @GetMapping("/{transactionPoid}/dependencies")
        @AllowedAction(UserRolesRightsEnum.DELETE)
        public ResponseEntity<?> checkSalesInvoiceDependencies(
                        @PathVariable Long transactionPoid) {
                log.info("Checking dependencies for sales invoice with transactionPoid: {} groupId: {} companyId: {}",
                                transactionPoid, UserContext.getGroupPoid(), UserContext.getCompanyPoid());
                SalesInvoiceDependenciesDto dto = invoiceService.checkSalesInvoiceDependencies(
                                transactionPoid, UserContext.getGroupPoid(), UserContext.getCompanyPoid());
                log.info("Dependencies checked for sales invoice with transactionPoid: {} groupId: {} companyId: {}",
                                transactionPoid, UserContext.getGroupPoid(), UserContext.getCompanyPoid());
                return success("Dependency check completed", dto);
        }

    @AllowedAction(UserRolesRightsEnum.PRINT)
    @Operation(
            summary = "Generate PDF for Sales Invoice",
            description = "Generate PDF report for a specific Sales Invoice",
            responses = {
                    @ApiResponse(responseCode = "200", description = "PDF generated successfully",
                            content = @Content(mediaType = "application/pdf")),
                    @ApiResponse(responseCode = "404", description = "Sales Invoice not found"),
                    @ApiResponse(responseCode = "500", description = "Failed to generate PDF")
            }
    )
    @GetMapping("/print/{transactionPoid}")
    public ResponseEntity<?> print(
            @Parameter(description = "Transaction POID", example = "281")
            @PathVariable Long transactionPoid) {
        try {
            String docId = UserContext.getDocumentId();
            byte[] pdf = invoiceService.print(transactionPoid);
            return ResponseEntity.ok()
                    .headers(downloadHeaderService.buildAttachmentHeaders(docId, transactionPoid, "imco-deposit-refund", "pdf"))
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);
        } catch (Exception e) {
            log.error("Failed to generate PDF for Sales Invoice: {}", transactionPoid, e);
            return error("Failed to generate PDF: " + e.getMessage(), 500);
        }
    }
}
