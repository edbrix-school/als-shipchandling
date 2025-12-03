package com.asg.shipchandling.deliverynote.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.asg.shipchandling.deliverynote.dto.CreateSalesDeliveryNoteItemDtlRequest;
import com.asg.shipchandling.deliverynote.dto.CreateSalesDeliveryNoteRequest;
import com.asg.shipchandling.deliverynote.dto.LoadQuotationItemsResponse;
import com.asg.shipchandling.deliverynote.dto.SalesDeliveryNoteDependenciesDto;
import com.asg.shipchandling.deliverynote.dto.SalesDeliveryNoteHdrDto;
import com.asg.shipchandling.deliverynote.dto.SalesDeliveryNoteItemDtlDto;
import com.asg.shipchandling.deliverynote.dto.ValidateCustomerChangeResponse;
import com.asg.shipchandling.deliverynote.dto.ValidationResponse;
import com.asg.shipchandling.deliverynote.service.SalesDeliveryNoteService;
import com.asg.common.lib.security.util.UserContext;

import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.util.List;

import com.asg.shipchandling.deliverynote.dto.PaginatedResponse;

import static com.asg.shipchandling.common.ApiResponse.success;
@RestController
@RequestMapping("/v1/deliverynote")
@RequiredArgsConstructor
@Slf4j
public class SalesDeliveryNoteController {

        private final SalesDeliveryNoteService deliveryNoteService;

        // ==================== BASIC CRUD OPERATIONS ====================

        @Operation(summary = "Create Delivery Note", description = "Creates a new Delivery Note (Ship Chandling). DocRef is auto-generated. Items with CheckAll='N' are excluded.", responses = {
                        @ApiResponse(responseCode = "200", description = "Successfully created delivery note"),
                        @ApiResponse(responseCode = "400", description = "Invalid input or validation error"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized")
        }, security = @SecurityRequirement(name = "bearerAuth"))
        @PostMapping
        public ResponseEntity<?> createDeliveryNote(
                        @Valid @RequestBody CreateSalesDeliveryNoteRequest request,
                        @RequestHeader("X-User-Id") String userId,
                        @RequestParam(required = true) String documentId,
                        @RequestParam(required = true) String actionRequested) {
                log.info("createDeliveryNote started for companyPoid={} groupPoid={}", UserContext.getCompanyPoid(), UserContext.getGroupPoid());
                SalesDeliveryNoteHdrDto dto = deliveryNoteService.createDeliveryNote(
                                request, UserContext.getGroupPoid(), UserContext.getCompanyPoid(), userId);
                log.info("createDeliveryNote completed for companyPoid={} groupPoid={}", UserContext.getCompanyPoid(), UserContext.getGroupPoid());
                return success("Delivery note created successfully", dto);
        }

        @Operation(summary = "Get Delivery Note by ID", description = "Retrieves a delivery note by transaction POID. Optionally includes item details.", responses = {
                        @ApiResponse(responseCode = "200", description = "Successfully retrieved delivery note"),
                        @ApiResponse(responseCode = "404", description = "Delivery note not found"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized")
        }, security = @SecurityRequirement(name = "bearerAuth"))
        @GetMapping("/{transactionPoid}")
        public ResponseEntity<?> getDeliveryNoteByPoid(
                        @PathVariable Long transactionPoid,
                        @RequestParam(required = true) String documentId,
                        @RequestParam(required = true) String actionRequested,
                        @RequestParam(required = false, defaultValue = "false") Boolean includeDetails) {

                log.info("getDeliveryNoteByPoid started for companyPoid={} groupPoid={}", UserContext.getCompanyPoid(), UserContext.getGroupPoid());
                SalesDeliveryNoteHdrDto dto = deliveryNoteService.getDeliveryNoteByPoid(UserContext.getGroupPoid(),
                                transactionPoid, UserContext.getCompanyPoid(), includeDetails);
                log.info("getDeliveryNoteByPoid completed for companyPoid={} groupPoid={}", UserContext.getCompanyPoid(), UserContext.getGroupPoid());
                return success("Delivery note fetched successfully", dto);
        }

        @Operation(summary = "Update Delivery Note", description = "Updates an existing delivery note. Validates customer change if customer is being changed. Items with CheckAll='N' are removed before save.", responses = {
                        @ApiResponse(responseCode = "200", description = "Successfully updated delivery note"),
                        @ApiResponse(responseCode = "400", description = "Invalid input or validation error"),
                        @ApiResponse(responseCode = "404", description = "Delivery note not found"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized")
        }, security = @SecurityRequirement(name = "bearerAuth"))
        @PutMapping("/{transactionPoid}")
        public ResponseEntity<?> updateDeliveryNote(
                        @PathVariable Long transactionPoid,
                        @Valid @RequestBody CreateSalesDeliveryNoteRequest request,
                        @RequestHeader("X-User-Id") String userId,
                        @RequestParam(required = true) String documentId,
                        @RequestParam(required = true) String actionRequested) {

                log.info("updateDeliveryNote started for companyPoid={} groupPoid={}", UserContext.getCompanyPoid(), UserContext.getGroupPoid());
                SalesDeliveryNoteHdrDto dto = deliveryNoteService.updateDeliveryNote(
                                UserContext.getGroupPoid(), transactionPoid, request, UserContext.getCompanyPoid(), userId);
                log.info("updateDeliveryNote completed for companyPoid={} groupPoid={}", UserContext.getCompanyPoid(), UserContext.getGroupPoid());
                return success("Delivery note updated successfully", dto);
        }

        @Operation(summary = "Delete Delivery Note", description = "Deletes a delivery note (soft delete). Checks dependencies before deletion.", responses = {
                        @ApiResponse(responseCode = "200", description = "Successfully deleted delivery note"),
                        @ApiResponse(responseCode = "404", description = "Delivery note not found"),
                        @ApiResponse(responseCode = "400", description = "Cannot delete due to dependencies"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized")
        }, security = @SecurityRequirement(name = "bearerAuth"))
        @DeleteMapping("/{transactionPoid}")
        public ResponseEntity<?> deleteDeliveryNote(
                        @PathVariable Long transactionPoid,
                        @RequestParam(required = true) String documentId,
                        @RequestParam(required = true) String actionRequested) {

                log.info("deleteDeliveryNote started for companyPoid={} groupPoid={}", UserContext.getCompanyPoid(), UserContext.getGroupPoid());
                deliveryNoteService.deleteDeliveryNote(UserContext.getGroupPoid(), transactionPoid, UserContext.getCompanyPoid());
                log.info("deleteDeliveryNote completed for companyPoid={} groupPoid={}", UserContext.getCompanyPoid(), UserContext.getGroupPoid());
                return success("Delivery note deleted successfully", null);
        }

        @Operation(summary = "Get All Delivery Notes", description = "Retrieves all delivery notes with optional filtering by status, customer, salesman, quotation reference, date range, etc. Supports pagination with page and size parameters.", responses = {
                        @ApiResponse(responseCode = "200", description = "Successfully retrieved delivery notes"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized")
        }, security = @SecurityRequirement(name = "bearerAuth"))
        @GetMapping
        public ResponseEntity<?> getAllDeliveryNotes(
                        @RequestParam(required = true) String documentId,
                        @RequestParam(required = true) String actionRequested,
                        @RequestParam(required = false) String deliveryStatus,
                        @RequestParam(required = false) Long customerPoid,
                        @RequestParam(required = false) Long salesmanPoid,
                        @RequestParam(required = false) String qtnRefNo,
                        @RequestParam(required = false, defaultValue = "0") Integer page,
                        @RequestParam(required = false, defaultValue = "10") Integer size,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime fromDate,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime toDate,
                        @RequestParam(required = false) String search) {

                Timestamp fromTs = (fromDate == null) ? null : Timestamp.from(fromDate.toInstant());
                Timestamp toTs = (toDate == null) ? null : Timestamp.from(toDate.toInstant());

                log.info("getAllDeliveryNotes started for groupPoid={} companyPoid={} page={} size={} deliveryStatus={} customerPoid={} salesmanPoid={} qtnRefNo={} fromTs={} toTs={} search={}", 
                        UserContext.getGroupPoid(), UserContext.getCompanyPoid(), page, size, deliveryStatus, customerPoid, salesmanPoid, qtnRefNo, 
                        fromTs, toTs, search);
                PaginatedResponse<SalesDeliveryNoteHdrDto> deliveryNotes = deliveryNoteService.getAllDeliveryNotes(
                                UserContext.getGroupPoid(), UserContext.getCompanyPoid(), deliveryStatus, customerPoid, salesmanPoid, qtnRefNo, 
                                fromTs, toTs, search, page, size);
                log.info("getAllDeliveryNotes completed for companyPoid={} groupPoid={} totalElements={}", 
                        UserContext.getCompanyPoid(), UserContext.getGroupPoid(), deliveryNotes.getTotalElements());
                return success("Delivery notes fetched successfully", deliveryNotes);
        }

        // ==================== VALIDATION APIs ====================

        @Operation(summary = "Validate Document Reference", description = "Checks if document reference is unique within the group")
        @GetMapping("/validate-doc-ref")
        public ResponseEntity<?> validateDocRef(
                        @RequestParam(required = true) String documentId,
                        @RequestParam(required = true) String actionRequested,
                        @RequestParam String docRef,
                        @RequestParam(required = false) Long transactionPoid) {
                log.info("validateDocRef started for  groupPoid={}", UserContext.getGroupPoid());
                ValidationResponse response = deliveryNoteService.validateDocRef(docRef, transactionPoid);
                log.info("validateDocRef completed for groupPoid={}", UserContext.getGroupPoid());
                return success("Validation completed", response);
        }

        // ==================== ITEM DETAILS APIs ====================

        @Operation(summary = "Add Item Detail", description = "Adds a new item detail to the delivery note. Recalculates totals.")
        @PostMapping("/{transactionPoid}/item-details")
        public ResponseEntity<?> addItemDetail(
                        @PathVariable Long transactionPoid,
                        @Valid @RequestBody CreateSalesDeliveryNoteItemDtlRequest request,
                        @RequestHeader("X-User-Id") String userId,
                        @RequestParam(required = true) String documentId,
                        @RequestParam(required = true) String actionRequested) {

                log.info("addItemDetail started for companyPoid={} groupPoid={}", UserContext.getCompanyPoid(), UserContext.getGroupPoid());
                SalesDeliveryNoteItemDtlDto dto = deliveryNoteService.addItemDetail(
                                transactionPoid, request, UserContext.getCompanyPoid(), userId);
                log.info("addItemDetail completed for companyPoid={} groupPoid={}", UserContext.getCompanyPoid(), UserContext.getGroupPoid());
                return success("Item detail added successfully", dto);
        }

        @Operation(summary = "Update Item Detail", description = "Updates an existing item detail. StockPoid and Quantity are read-only if item is loaded from quotation. Recalculates totals.")
        @PutMapping("/{transactionPoid}/item-details/{detRowId}")
        public ResponseEntity<?> updateItemDetail(
                        @PathVariable Long transactionPoid,
                        @PathVariable Long detRowId,
                        @Valid @RequestBody CreateSalesDeliveryNoteItemDtlRequest request,
                        @RequestHeader("X-User-Id") String userId,
                        @RequestParam(required = true) String documentId,
                        @RequestParam(required = true) String actionRequested) {

                log.info("updateItemDetail started for companyPoid={} groupPoid={}", UserContext.getCompanyPoid(), UserContext.getGroupPoid());
                SalesDeliveryNoteItemDtlDto dto = deliveryNoteService.updateItemDetail(
                                transactionPoid, detRowId, request, UserContext.getCompanyPoid(), userId);
                log.info("updateItemDetail completed for companyPoid={} groupPoid={}", UserContext.getCompanyPoid(), UserContext.getGroupPoid());
                return success("Item detail updated successfully", dto);
        }

        @Operation(summary = "Delete Item Detail", description = "Deletes an item detail. Recalculates totals.")
        @DeleteMapping("/{transactionPoid}/item-details/{detRowId}")
        public ResponseEntity<?> deleteItemDetail(
                        @PathVariable Long transactionPoid,
                        @PathVariable Long detRowId,
                        @RequestParam(required = true) String documentId,
                        @RequestParam(required = true) String actionRequested) {

                log.info("deleteItemDetail started for companyPoid={} groupPoid={}", UserContext.getCompanyPoid(), UserContext.getGroupPoid());
                deliveryNoteService.deleteItemDetail(transactionPoid, detRowId, UserContext.getCompanyPoid());
                log.info("deleteItemDetail completed for companyPoid={} groupPoid={}", UserContext.getCompanyPoid(), UserContext.getGroupPoid());
                return success("Item detail deleted successfully", null);
        }

        @Operation(summary = "Get Item Details", description = "Retrieves all item details for a delivery note.")
        @GetMapping("/{transactionPoid}/item-details")
        public ResponseEntity<?> getItemDetails(
                        @PathVariable Long transactionPoid,
                        @RequestParam(required = true) String documentId,
                        @RequestParam(required = true) String actionRequested) {

                log.info("getItemDetails started for companyPoid={} groupPoid={}", UserContext.getCompanyPoid(), UserContext.getGroupPoid());
                List<SalesDeliveryNoteItemDtlDto> itemDetails = deliveryNoteService.getItemDetails(
                                transactionPoid, UserContext.getCompanyPoid());
                log.info("getItemDetails completed for companyPoid={} groupPoid={}", UserContext.getCompanyPoid(), UserContext.getGroupPoid());
                return success("Item details fetched successfully", itemDetails);
        }

        // ==================== BUSINESS LOGIC APIs ====================

        @Operation(summary = "Validate Customer Change", description = "Validates if customer can be changed. Calls PROC_SALES_SCDN_CUST_VALIDATE.")
        @GetMapping("/{transactionPoid}/validate-customer-change")
        public ResponseEntity<?> validateCustomerChange(
                        @PathVariable Long transactionPoid,
                        @RequestHeader("X-Customer-Poid") Long customerPoid,
                        @RequestParam(required = true) String documentId,
                        @RequestParam(required = true) String actionRequested) {

                log.info("validateCustomerChange started for customerPoid={} groupPoid={}", customerPoid, UserContext.getGroupPoid());
                ValidateCustomerChangeResponse response = deliveryNoteService.validateCustomerChange(
                                transactionPoid, customerPoid);
                log.info("validateCustomerChange completed for customerPoid={} groupPoid={}", customerPoid, UserContext.getGroupPoid());
                return success(response.getMessage(), response);
        }

        @Operation(summary = "Load Quotation Items", description = "Loads items from quotation into delivery note. Requires QtnRefNo to be set. Removes items with CheckAll='N' before loading. Calls PROC_DN_LOAD_QUOTATION_DETAIL.")
        @GetMapping("/{transactionPoid}/load-quotation-items")
        public ResponseEntity<?> loadQuotationItems(
                        @PathVariable Long transactionPoid,
                        @RequestHeader("X-User-Id") String userId,
                        @RequestParam(required = true) String documentId,
                        @RequestParam(required = true) String actionRequested) {

                log.info("loadQuotationItems started for companyPoid={} groupPoid={}", UserContext.getCompanyPoid(), UserContext.getGroupPoid());
                LoadQuotationItemsResponse response = deliveryNoteService.loadQuotationItems(
                                UserContext.getGroupPoid(), transactionPoid, UserContext.getCompanyPoid(), userId);
                log.info("loadQuotationItems completed for companyPoid={} groupPoid={}", UserContext.getCompanyPoid(), UserContext.getGroupPoid());
                return success(response.getMessage(), response);
        }

        @Operation(summary = "Check Delivery Note Dependencies", description = "Checks if delivery note can be deleted by checking for dependencies (sales invoices, etc.)")
        @GetMapping("/{transactionPoid}/dependencies")
        public ResponseEntity<?> checkDeliveryNoteDependencies(
                        @PathVariable Long transactionPoid,
                        @RequestParam(required = true) String documentId,
                        @RequestParam(required = true) String actionRequested) {

                log.info("checkDeliveryNoteDependencies started for companyPoid={} groupPoid={}", UserContext.getCompanyPoid(),
                                UserContext.getGroupPoid());
                SalesDeliveryNoteDependenciesDto dto = deliveryNoteService.checkDeliveryNoteDependencies(
                                transactionPoid, UserContext.getCompanyPoid());
                log.info("checkDeliveryNoteDependencies completed for companyPoid={} groupPoid={}", UserContext.getCompanyPoid(),
                                UserContext.getGroupPoid());
                return success("Dependency check completed", dto);
        }
}
