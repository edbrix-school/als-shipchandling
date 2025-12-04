package com.asg.shipchandling.deliverynote.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
import com.asg.shipchandling.deliverynote.dto.request.GetAllDeliveryNoteFilterRequest;
import com.asg.shipchandling.deliverynote.service.SalesDeliveryNoteService;
import com.asg.common.lib.security.util.UserContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;

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
                        @Valid @RequestBody CreateSalesDeliveryNoteRequest request) {
                log.info("createDeliveryNote started for companyPoid={} groupPoid={}", UserContext.getCompanyPoid(), UserContext.getGroupPoid());
                SalesDeliveryNoteHdrDto dto = deliveryNoteService.createDeliveryNote(
                                request, UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserId());
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
                        @Valid @RequestBody CreateSalesDeliveryNoteRequest request) {

                log.info("updateDeliveryNote started for companyPoid={} groupPoid={}", UserContext.getCompanyPoid(), UserContext.getGroupPoid());
                SalesDeliveryNoteHdrDto dto = deliveryNoteService.updateDeliveryNote(
                                UserContext.getGroupPoid(), transactionPoid, request, UserContext.getCompanyPoid(), UserContext.getUserId());
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
                        @PathVariable Long transactionPoid) {

                log.info("deleteDeliveryNote started for companyPoid={} groupPoid={}", UserContext.getCompanyPoid(), UserContext.getGroupPoid());
                deliveryNoteService.deleteDeliveryNote(UserContext.getGroupPoid(), transactionPoid, UserContext.getCompanyPoid());
                log.info("deleteDeliveryNote completed for companyPoid={} groupPoid={}", UserContext.getCompanyPoid(), UserContext.getGroupPoid());
                return success("Delivery note deleted successfully", null);
        }

        @Operation(summary = "Get All Delivery Notes", description = "Returns paginated list of delivery notes with optional filters. Supports pagination with page and size parameters.", responses = {
                        @ApiResponse(responseCode = "200", description = "Delivery notes fetched successfully", content = @Content(schema = @Schema(implementation = Page.class))),
                        @ApiResponse(responseCode = "401", description = "Unauthorized")
        }, security = @SecurityRequirement(name = "bearerAuth"))
        @PostMapping("/list")
        public ResponseEntity<?> getAllDeliveryNotes(
                        @RequestBody(required = false) GetAllDeliveryNoteFilterRequest filterRequest,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "20") int size) {

                // If filterRequest is null, create a default one
                if (filterRequest == null) {
                        filterRequest = new GetAllDeliveryNoteFilterRequest();
                        filterRequest.setIsDeleted("N");
                        filterRequest.setOperator("AND");
                        filterRequest.setFilters(new java.util.ArrayList<>());
                }

                Page<SalesDeliveryNoteHdrDto> deliveryNotePage = deliveryNoteService
                                .getAllDeliveryNotesWithFilters(UserContext.getGroupPoid(), UserContext.getCompanyPoid(), filterRequest, page, size);

                // Create displayFields
                Map<String, String> displayFields = new HashMap<>();
                displayFields.put("docRef", "text");
                displayFields.put("customerName", "text");
                displayFields.put("qtnRefNo", "text");

                // Create paginated response with new structure
                Map<String, Object> response = new HashMap<>();
                response.put("content", deliveryNotePage.getContent());
                response.put("pageNumber", deliveryNotePage.getNumber());
                response.put("displayFields", displayFields);
                response.put("pageSize", deliveryNotePage.getSize());
                response.put("totalElements", deliveryNotePage.getTotalElements());
                response.put("totalPages", deliveryNotePage.getTotalPages());
                response.put("last", deliveryNotePage.isLast());

                return success("Delivery notes fetched successfully", response);
        }

        // ==================== VALIDATION APIs ====================

        @Operation(summary = "Validate Document Reference", description = "Checks if document reference is unique within the group")
        @GetMapping("/validate-doc-ref")
        public ResponseEntity<?> validateDocRef(
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
                        @Valid @RequestBody CreateSalesDeliveryNoteItemDtlRequest request) {

                log.info("addItemDetail started for companyPoid={} groupPoid={}", UserContext.getCompanyPoid(), UserContext.getGroupPoid());
                SalesDeliveryNoteItemDtlDto dto = deliveryNoteService.addItemDetail(
                                transactionPoid, request, UserContext.getCompanyPoid(), UserContext.getUserId());
                log.info("addItemDetail completed for companyPoid={} groupPoid={}", UserContext.getCompanyPoid(), UserContext.getGroupPoid());
                return success("Item detail added successfully", dto);
        }

        @Operation(summary = "Update Item Detail", description = "Updates an existing item detail. StockPoid and Quantity are read-only if item is loaded from quotation. Recalculates totals.")
        @PutMapping("/{transactionPoid}/item-details/{detRowId}")
        public ResponseEntity<?> updateItemDetail(
                        @PathVariable Long transactionPoid,
                        @PathVariable Long detRowId,
                        @Valid @RequestBody CreateSalesDeliveryNoteItemDtlRequest request) {

                log.info("updateItemDetail started for companyPoid={} groupPoid={}", UserContext.getCompanyPoid(), UserContext.getGroupPoid());
                SalesDeliveryNoteItemDtlDto dto = deliveryNoteService.updateItemDetail(
                                transactionPoid, detRowId, request, UserContext.getCompanyPoid(), UserContext.getUserId());
                log.info("updateItemDetail completed for companyPoid={} groupPoid={}", UserContext.getCompanyPoid(), UserContext.getGroupPoid());
                return success("Item detail updated successfully", dto);
        }

        @Operation(summary = "Delete Item Detail", description = "Deletes an item detail. Recalculates totals.")
        @DeleteMapping("/{transactionPoid}/item-details/{detRowId}")
        public ResponseEntity<?> deleteItemDetail(
                        @PathVariable Long transactionPoid,
                        @PathVariable Long detRowId) {

                log.info("deleteItemDetail started for companyPoid={} groupPoid={}", UserContext.getCompanyPoid(), UserContext.getGroupPoid());
                deliveryNoteService.deleteItemDetail(transactionPoid, detRowId, UserContext.getCompanyPoid());
                log.info("deleteItemDetail completed for companyPoid={} groupPoid={}", UserContext.getCompanyPoid(), UserContext.getGroupPoid());
                return success("Item detail deleted successfully", null);
        }

        @Operation(summary = "Get Item Details", description = "Retrieves all item details for a delivery note.")
        @GetMapping("/{transactionPoid}/item-details")
        public ResponseEntity<?> getItemDetails(
                        @PathVariable Long transactionPoid) {

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
                        @RequestHeader("X-Customer-Poid") Long customerPoid) {

                log.info("validateCustomerChange started for customerPoid={} groupPoid={}", customerPoid, UserContext.getGroupPoid());
                ValidateCustomerChangeResponse response = deliveryNoteService.validateCustomerChange(
                                transactionPoid, customerPoid);
                log.info("validateCustomerChange completed for customerPoid={} groupPoid={}", customerPoid, UserContext.getGroupPoid());
                return success(response.getMessage(), response);
        }

        @Operation(summary = "Load Quotation Items", description = "Loads items from quotation into delivery note. Requires QtnRefNo to be set. Removes items with CheckAll='N' before loading. Calls PROC_DN_LOAD_QUOTATION_DETAIL.")
        @GetMapping("/{transactionPoid}/load-quotation-items")
        public ResponseEntity<?> loadQuotationItems(
                        @PathVariable Long transactionPoid) {

                log.info("loadQuotationItems started for companyPoid={} groupPoid={}", UserContext.getCompanyPoid(), UserContext.getGroupPoid());
                LoadQuotationItemsResponse response = deliveryNoteService.loadQuotationItems(
                                UserContext.getGroupPoid(), transactionPoid, UserContext.getCompanyPoid(), UserContext.getUserId());
                log.info("loadQuotationItems completed for companyPoid={} groupPoid={}", UserContext.getCompanyPoid(), UserContext.getGroupPoid());
                return success(response.getMessage(), response);
        }

        @Operation(summary = "Check Delivery Note Dependencies", description = "Checks if delivery note can be deleted by checking for dependencies (sales invoices, etc.)")
        @GetMapping("/{transactionPoid}/dependencies")
        public ResponseEntity<?> checkDeliveryNoteDependencies(
                        @PathVariable Long transactionPoid) {

                log.info("checkDeliveryNoteDependencies started for companyPoid={} groupPoid={}", UserContext.getCompanyPoid(),
                                UserContext.getGroupPoid());
                SalesDeliveryNoteDependenciesDto dto = deliveryNoteService.checkDeliveryNoteDependencies(
                                transactionPoid, UserContext.getCompanyPoid());
                log.info("checkDeliveryNoteDependencies completed for companyPoid={} groupPoid={}", UserContext.getCompanyPoid(),
                                UserContext.getGroupPoid());
                return success("Dependency check completed", dto);
        }
}
