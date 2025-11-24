package com.alsharif.shipchandling.deliverynote.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.alsharif.shipchandling.deliverynote.dto.CreateSalesDeliveryNoteItemDtlRequest;
import com.alsharif.shipchandling.deliverynote.dto.CreateSalesDeliveryNoteRequest;
import com.alsharif.shipchandling.deliverynote.dto.LoadQuotationItemsResponse;
import com.alsharif.shipchandling.deliverynote.dto.SalesDeliveryNoteDependenciesDto;
import com.alsharif.shipchandling.deliverynote.dto.SalesDeliveryNoteHdrDto;
import com.alsharif.shipchandling.deliverynote.dto.SalesDeliveryNoteItemDtlDto;
import com.alsharif.shipchandling.deliverynote.dto.ValidateCustomerChangeResponse;
import com.alsharif.shipchandling.deliverynote.dto.ValidationResponse;
import com.alsharif.shipchandling.deliverynote.service.SalesDeliveryNoteService;

import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.util.List;

import com.alsharif.shipchandling.deliverynote.dto.PaginatedResponse;

import static com.alsharif.shipchandling.utility.ResponseUtil.success;

@RestController
@RequestMapping("deliverynote")
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
                        @RequestHeader("X-Company-Poid") Long companyPoid,
                        @RequestHeader("X-Group-Poid") Long groupPoid,
                        @RequestHeader("X-User-Id") String userId) {
                log.info("createDeliveryNote started for companyPoid={} groupPoid={}", companyPoid, groupPoid);
                SalesDeliveryNoteHdrDto dto = deliveryNoteService.createDeliveryNote(
                                request, groupPoid, companyPoid, userId);
                log.info("createDeliveryNote completed for companyPoid={} groupPoid={}", companyPoid, groupPoid);
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
                        @RequestHeader("X-Group-Poid") Long groupPoid,
                        @RequestHeader("X-Company-Poid") Long companyPoid,
                        @RequestParam(required = false, defaultValue = "false") Boolean includeDetails,
                        @RequestParam String documentId,
                        @RequestParam String actionRequested) {

                log.info("getDeliveryNoteByPoid started for companyPoid={} groupPoid={}", companyPoid, groupPoid);
                SalesDeliveryNoteHdrDto dto = deliveryNoteService.getDeliveryNoteByPoid(groupPoid,
                                transactionPoid, companyPoid, includeDetails);
                log.info("getDeliveryNoteByPoid completed for companyPoid={} groupPoid={}", companyPoid, groupPoid);
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
                        @RequestHeader("X-Company-Poid") Long companyPoid,
                        @RequestHeader("X-Group-Poid") Long groupPoid,
                        @RequestHeader("X-User-Id") String userId) {

                log.info("updateDeliveryNote started for companyPoid={} groupPoid={}", companyPoid, groupPoid);
                SalesDeliveryNoteHdrDto dto = deliveryNoteService.updateDeliveryNote(
                                groupPoid, transactionPoid, request, companyPoid, userId);
                log.info("updateDeliveryNote completed for companyPoid={} groupPoid={}", companyPoid, groupPoid);
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
                        @RequestHeader("X-Group-Poid") Long groupPoid,
                        @RequestHeader("X-Company-Poid") Long companyPoid) {

                log.info("deleteDeliveryNote started for companyPoid={} groupPoid={}", companyPoid, groupPoid);
                deliveryNoteService.deleteDeliveryNote(groupPoid, transactionPoid, companyPoid);
                log.info("deleteDeliveryNote completed for companyPoid={} groupPoid={}", companyPoid, groupPoid);
                return success("Delivery note deleted successfully", null);
        }

        @Operation(summary = "Get All Delivery Notes", description = "Retrieves all delivery notes with optional filtering by status, customer, salesman, quotation reference, date range, etc. Supports pagination with page and size parameters.", responses = {
                        @ApiResponse(responseCode = "200", description = "Successfully retrieved delivery notes"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized")
        }, security = @SecurityRequirement(name = "bearerAuth"))
        @GetMapping
        public ResponseEntity<?> getAllDeliveryNotes(
                        @RequestHeader("X-Group-Poid") Long groupPoid,
                        @RequestHeader("X-Company-Poid") Long companyPoid,
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
                        groupPoid, companyPoid, page, size, deliveryStatus, customerPoid, salesmanPoid, qtnRefNo, 
                        fromTs, toTs, search);
                PaginatedResponse<SalesDeliveryNoteHdrDto> deliveryNotes = deliveryNoteService.getAllDeliveryNotes(
                                groupPoid, companyPoid, deliveryStatus, customerPoid, salesmanPoid, qtnRefNo, 
                                fromTs, toTs, search, page, size);
                log.info("getAllDeliveryNotes completed for companyPoid={} groupPoid={} totalElements={}", 
                        companyPoid, groupPoid, deliveryNotes.getTotalElements());
                return success("Delivery notes fetched successfully", deliveryNotes);
        }

        // ==================== VALIDATION APIs ====================

        @Operation(summary = "Validate Document Reference", description = "Checks if document reference is unique within the group")
        @GetMapping("/validate-doc-ref")
        public ResponseEntity<?> validateDocRef(
                        @RequestParam String docRef,
                        @RequestHeader("X-Group-Poid") Long groupPoid,
                        @RequestParam(required = false) Long transactionPoid) {
                log.info("validateDocRef started for  groupPoid={}", groupPoid);
                ValidationResponse response = deliveryNoteService.validateDocRef(docRef, transactionPoid);
                log.info("validateDocRef completed for groupPoid={}", groupPoid);
                return success("Validation completed", response);
        }

        // ==================== ITEM DETAILS APIs ====================

        @Operation(summary = "Add Item Detail", description = "Adds a new item detail to the delivery note. Recalculates totals.")
        @PostMapping("/{transactionPoid}/item-details")
        public ResponseEntity<?> addItemDetail(
                        @PathVariable Long transactionPoid,
                        @Valid @RequestBody CreateSalesDeliveryNoteItemDtlRequest request,
                        @RequestHeader("X-Group-Poid") Long groupPoid,
                        @RequestHeader("X-Company-Poid") Long companyPoid,
                        @RequestHeader("X-User-Id") String userId) {

                log.info("addItemDetail started for companyPoid={} groupPoid={}", companyPoid, groupPoid);
                SalesDeliveryNoteItemDtlDto dto = deliveryNoteService.addItemDetail(
                                transactionPoid, request, companyPoid, userId);
                log.info("addItemDetail completed for companyPoid={} groupPoid={}", companyPoid, groupPoid);
                return success("Item detail added successfully", dto);
        }

        @Operation(summary = "Update Item Detail", description = "Updates an existing item detail. StockPoid and Quantity are read-only if item is loaded from quotation. Recalculates totals.")
        @PutMapping("/{transactionPoid}/item-details/{detRowId}")
        public ResponseEntity<?> updateItemDetail(
                        @PathVariable Long transactionPoid,
                        @PathVariable Long detRowId,
                        @Valid @RequestBody CreateSalesDeliveryNoteItemDtlRequest request,
                        @RequestHeader("X-Group-Poid") Long groupPoid,
                        @RequestHeader("X-Company-Poid") Long companyPoid,
                        @RequestHeader("X-User-Id") String userId) {

                log.info("updateItemDetail started for companyPoid={} groupPoid={}", companyPoid, groupPoid);
                SalesDeliveryNoteItemDtlDto dto = deliveryNoteService.updateItemDetail(
                                transactionPoid, detRowId, request, companyPoid, userId);
                log.info("updateItemDetail completed for companyPoid={} groupPoid={}", companyPoid, groupPoid);
                return success("Item detail updated successfully", dto);
        }

        @Operation(summary = "Delete Item Detail", description = "Deletes an item detail. Recalculates totals.")
        @DeleteMapping("/{transactionPoid}/item-details/{detRowId}")
        public ResponseEntity<?> deleteItemDetail(
                        @PathVariable Long transactionPoid,
                        @PathVariable Long detRowId,
                        @RequestHeader("X-Group-Poid") Long groupPoid,
                        @RequestHeader("X-Company-Poid") Long companyPoid) {

                log.info("deleteItemDetail started for companyPoid={} groupPoid={}", companyPoid, groupPoid);
                deliveryNoteService.deleteItemDetail(transactionPoid, detRowId, companyPoid);
                log.info("deleteItemDetail completed for companyPoid={} groupPoid={}", companyPoid, groupPoid);
                return success("Item detail deleted successfully", null);
        }

        @Operation(summary = "Get Item Details", description = "Retrieves all item details for a delivery note.")
        @GetMapping("/{transactionPoid}/item-details")
        public ResponseEntity<?> getItemDetails(
                        @PathVariable Long transactionPoid,
                        @RequestHeader("X-Group-Poid") Long groupPoid,
                        @RequestHeader("X-Company-Poid") Long companyPoid) {

                log.info("getItemDetails started for companyPoid={} groupPoid={}", companyPoid, groupPoid);
                List<SalesDeliveryNoteItemDtlDto> itemDetails = deliveryNoteService.getItemDetails(
                                transactionPoid, companyPoid);
                log.info("getItemDetails completed for companyPoid={} groupPoid={}", companyPoid, groupPoid);
                return success("Item details fetched successfully", itemDetails);
        }

        // ==================== BUSINESS LOGIC APIs ====================

        @Operation(summary = "Validate Customer Change", description = "Validates if customer can be changed. Calls PROC_SALES_SCDN_CUST_VALIDATE.")
        @GetMapping("/{transactionPoid}/validate-customer-change")
        public ResponseEntity<?> validateCustomerChange(
                        @PathVariable Long transactionPoid,
                        @RequestHeader("X-Group-Poid") Long groupPoid,
                        @RequestHeader("X-Company-Poid") Long companyPoid) {

                log.info("validateCustomerChange started for companyPoid={} groupPoid={}", companyPoid, groupPoid);
                ValidateCustomerChangeResponse response = deliveryNoteService.validateCustomerChange(
                                transactionPoid, companyPoid);
                log.info("validateCustomerChange completed for companyPoid={} groupPoid={}", companyPoid, groupPoid);
                return success(response.getMessage(), response);
        }

        @Operation(summary = "Load Quotation Items", description = "Loads items from quotation into delivery note. Requires QtnRefNo to be set. Removes items with CheckAll='N' before loading. Calls PROC_DN_LOAD_QUOTATION_DETAIL.")
        @GetMapping("/{transactionPoid}/load-quotation-items")
        public ResponseEntity<?> loadQuotationItems(
                        @PathVariable Long transactionPoid,
                        @RequestHeader("X-Group-Poid") Long groupPoid,
                        @RequestHeader("X-Company-Poid") Long companyPoid,
                        @RequestHeader("X-User-Id") String userId) {

                log.info("loadQuotationItems started for companyPoid={} groupPoid={}", companyPoid, groupPoid);
                LoadQuotationItemsResponse response = deliveryNoteService.loadQuotationItems(
                                groupPoid, transactionPoid, companyPoid, userId);
                log.info("loadQuotationItems completed for companyPoid={} groupPoid={}", companyPoid, groupPoid);
                return success(response.getMessage(), response);
        }

        @Operation(summary = "Check Delivery Note Dependencies", description = "Checks if delivery note can be deleted by checking for dependencies (sales invoices, etc.)")
        @GetMapping("/{transactionPoid}/dependencies")
        public ResponseEntity<?> checkDeliveryNoteDependencies(
                        @PathVariable Long transactionPoid,
                        @RequestHeader("X-Group-Poid") Long groupPoid,
                        @RequestHeader("X-Company-Poid") Long companyPoid) {

                log.info("checkDeliveryNoteDependencies started for companyPoid={} groupPoid={}", companyPoid,
                                groupPoid);
                SalesDeliveryNoteDependenciesDto dto = deliveryNoteService.checkDeliveryNoteDependencies(
                                transactionPoid, companyPoid);
                log.info("checkDeliveryNoteDependencies completed for companyPoid={} groupPoid={}", companyPoid,
                                groupPoid);
                return success("Dependency check completed", dto);
        }
}
