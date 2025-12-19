package com.asg.shipchandling.deliverynote.controller;

import com.asg.common.lib.annotation.AllowedAction;
import com.asg.common.lib.dto.FilterRequestDto;
import com.asg.common.lib.enums.UserRolesRightsEnum;
import com.asg.shipchandling.deliverynote.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.asg.shipchandling.deliverynote.service.SalesDeliveryNoteService;
import com.asg.common.lib.security.util.UserContext;

import java.time.LocalDate;
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
    @AllowedAction(UserRolesRightsEnum.CREATE)
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
    @AllowedAction(UserRolesRightsEnum.VIEW)
    public ResponseEntity<?> getDeliveryNoteByPoid(
            @PathVariable Long transactionPoid,
            @RequestParam(required = false, defaultValue = "true") Boolean includeDetails) {

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
    @AllowedAction(UserRolesRightsEnum.EDIT)
    public ResponseEntity<?> updateDeliveryNote(
            @PathVariable Long transactionPoid,
            @Valid @RequestBody UpdateSalesDeliveryNoteRequest request) {

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
    @AllowedAction(UserRolesRightsEnum.DELETE)
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
    @AllowedAction(UserRolesRightsEnum.VIEW)
    public ResponseEntity<?> getAllDeliveryNotes(@ParameterObject Pageable pageable,
                                                 @RequestBody(required = false) FilterRequestDto filters,
                                                 @RequestParam(required = false) LocalDate startDate,
                                                 @RequestParam(required = false) LocalDate endDate) {
        return success("Delivery notes fetched successfully", deliveryNoteService.listDeliveryNotes(UserContext.getDocumentId(), filters, startDate, endDate, pageable));
    }

    // ==================== VALIDATION APIs ====================

    // @Operation(summary = "Validate Document Reference", description = "Checks if document reference is unique within the group")
    // @GetMapping("/validate-doc-ref")
    // public ResponseEntity<?> validateDocRef(
    //                 @RequestParam String docRef,
    //                 @RequestParam(required = false) Long transactionPoid) {
    //         log.info("validateDocRef started for  groupPoid={}", UserContext.getGroupPoid());
    //         ValidationResponse response = deliveryNoteService.validateDocRef(docRef, transactionPoid);
    //         log.info("validateDocRef completed for groupPoid={}", UserContext.getGroupPoid());
    //         return success("Validation completed", response);
    // }

    // ==================== ITEM DETAILS APIs ====================

    // @Operation(summary = "Add Item Detail", description = "Adds a new item detail to the delivery note. Recalculates totals.")
    // @PostMapping("/{transactionPoid}/item-details")
    // public ResponseEntity<?> addItemDetail(
    //                 @PathVariable Long transactionPoid,
    //                 @Valid @RequestBody CreateSalesDeliveryNoteItemDtlRequest request) {

    //         log.info("addItemDetail started for companyPoid={} groupPoid={}", UserContext.getCompanyPoid(), UserContext.getGroupPoid());
    //         SalesDeliveryNoteItemDtlDto dto = deliveryNoteService.addItemDetail(
    //                         transactionPoid, request, UserContext.getCompanyPoid(), UserContext.getUserId());
    //         log.info("addItemDetail completed for companyPoid={} groupPoid={}", UserContext.getCompanyPoid(), UserContext.getGroupPoid());
    //         return success("Item detail added successfully", dto);
    // }

    // @Operation(summary = "Update Item Detail", description = "Updates an existing item detail. StockPoid and Quantity are read-only if item is loaded from quotation. Recalculates totals.")
    // @PutMapping("/{transactionPoid}/item-details/{detRowId}")
    // public ResponseEntity<?> updateItemDetail(
    //                 @PathVariable Long transactionPoid,
    //                 @PathVariable Long detRowId,
    //                 @Valid @RequestBody CreateSalesDeliveryNoteItemDtlRequest request) {

    //         log.info("updateItemDetail started for companyPoid={} groupPoid={}", UserContext.getCompanyPoid(), UserContext.getGroupPoid());
    //         SalesDeliveryNoteItemDtlDto dto = deliveryNoteService.updateItemDetail(
    //                         transactionPoid, detRowId, request, UserContext.getCompanyPoid(), UserContext.getUserId());
    //         log.info("updateItemDetail completed for companyPoid={} groupPoid={}", UserContext.getCompanyPoid(), UserContext.getGroupPoid());
    //         return success("Item detail updated successfully", dto);
    // }

    // @Operation(summary = "Delete Item Detail", description = "Deletes an item detail. Recalculates totals.")
    // @DeleteMapping("/{transactionPoid}/item-details/{detRowId}")
    // public ResponseEntity<?> deleteItemDetail(
    //                 @PathVariable Long transactionPoid,
    //                 @PathVariable Long detRowId) {

    //         log.info("deleteItemDetail started for companyPoid={} groupPoid={}", UserContext.getCompanyPoid(), UserContext.getGroupPoid());
    //         deliveryNoteService.deleteItemDetail(transactionPoid, detRowId, UserContext.getCompanyPoid());
    //         log.info("deleteItemDetail completed for companyPoid={} groupPoid={}", UserContext.getCompanyPoid(), UserContext.getGroupPoid());
    //         return success("Item detail deleted successfully", null);
    // }

    // @Operation(summary = "Get Item Details", description = "Retrieves all item details for a delivery note.")
    // @GetMapping("/{transactionPoid}/item-details")
    // public ResponseEntity<?> getItemDetails(
    //                 @PathVariable Long transactionPoid) {

    //         log.info("getItemDetails started for companyPoid={} groupPoid={}", UserContext.getCompanyPoid(), UserContext.getGroupPoid());
    //         List<SalesDeliveryNoteItemDtlDto> itemDetails = deliveryNoteService.getItemDetails(
    //                         transactionPoid, UserContext.getCompanyPoid());
    //         log.info("getItemDetails completed for companyPoid={} groupPoid={}", UserContext.getCompanyPoid(), UserContext.getGroupPoid());
    //         return success("Item details fetched successfully", itemDetails);
    // }

    // ==================== BUSINESS LOGIC APIs ====================

    // @Operation(summary = "Validate Customer Change", description = "Validates if customer can be changed. Calls PROC_SALES_SCDN_CUST_VALIDATE.")
    // @GetMapping("/{transactionPoid}/validate-customer-change")
    // public ResponseEntity<?> validateCustomerChange(
    //                 @PathVariable Long transactionPoid,
    //                 @RequestHeader("X-Customer-Poid") Long customerPoid) {

    //         log.info("validateCustomerChange started for customerPoid={} groupPoid={}", customerPoid, UserContext.getGroupPoid());
    //         ValidateCustomerChangeResponse response = deliveryNoteService.validateCustomerChange(
    //                         transactionPoid, customerPoid);
    //         log.info("validateCustomerChange completed for customerPoid={} groupPoid={}", customerPoid, UserContext.getGroupPoid());
    //         return success(response.getMessage(), response);
    // }

    @Operation(summary = "Load Quotation Items", description = "Loads items from quotation into delivery note. Requires QtnRefNo to be set. Removes items with CheckAll='N' before loading. Calls PROC_DN_LOAD_QUOTATION_DETAIL.")
    @GetMapping("/{transactionPoid}/load-quotation-items")
    @AllowedAction(UserRolesRightsEnum.VIEW)
    public ResponseEntity<?> loadQuotationItems(
            @PathVariable Long transactionPoid) {

        log.info("loadQuotationItems started for companyPoid={} groupPoid={}", UserContext.getCompanyPoid(), UserContext.getGroupPoid());
        List<QuotationItemDto> response = deliveryNoteService.loadQuotationItems(UserContext.getGroupPoid(), transactionPoid, UserContext.getCompanyPoid(), UserContext.getUserPoid());
        log.info("loadQuotationItems completed for companyPoid={} groupPoid={}", UserContext.getCompanyPoid(), UserContext.getGroupPoid());
        return success("Quotation items loaded successfully", Map.of("items", response));
    }

    // @Operation(summary = "Check Delivery Note Dependencies", description = "Checks if delivery note can be deleted by checking for dependencies (sales invoices, etc.)")
    // @GetMapping("/{transactionPoid}/dependencies")
    // public ResponseEntity<?> checkDeliveryNoteDependencies(
    //                 @PathVariable Long transactionPoid) {

    //         log.info("checkDeliveryNoteDependencies started for companyPoid={} groupPoid={}", UserContext.getCompanyPoid(),
    //                         UserContext.getGroupPoid());
    //         SalesDeliveryNoteDependenciesDto dto = deliveryNoteService.checkDeliveryNoteDependencies(
    //                         transactionPoid, UserContext.getCompanyPoid());
    //         log.info("checkDeliveryNoteDependencies completed for companyPoid={} groupPoid={}", UserContext.getCompanyPoid(),
    //                         UserContext.getGroupPoid());
    //         return success("Dependency check completed", dto);
    // }
}
