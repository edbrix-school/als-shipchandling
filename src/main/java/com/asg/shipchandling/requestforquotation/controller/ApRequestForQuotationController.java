package com.asg.shipchandling.requestforquotation.controller;

import com.asg.shipchandling.requestforquotation.dto.RfqDependenciesDto;
import com.asg.shipchandling.requestforquotation.dto.request.*;
import com.asg.shipchandling.requestforquotation.dto.response.*;
import com.asg.shipchandling.requestforquotation.service.ApRequestForQtnService;
import com.asg.common.lib.annotation.AllowedAction;
import com.asg.common.lib.enums.UserRolesRightsEnum;
import com.asg.common.lib.security.util.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.data.domain.Page;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.asg.shipchandling.common.ApiResponse.success;

@RestController
@RequestMapping("/v1/request-for-quotations")
@RequiredArgsConstructor
public class ApRequestForQuotationController {

        private final ApRequestForQtnService rfqService;

        @Operation(summary = "Get all RFQs", description = "Returns paginated list of RFQs with optional filters. Supports pagination with page and size parameters.", responses = {
                        @ApiResponse(responseCode = "200", description = "Task list fetched successfully", content = @Content(schema = @Schema(implementation = Page.class)))
        })
        @AllowedAction(UserRolesRightsEnum.VIEW)
        @PostMapping("/search")
        public ResponseEntity<?> getAllRequestForQuotations(
                        @RequestBody(required = false) GetAllRfqFilterRequest filterRequest,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "20") int size) {

                // If filterRequest is null, create a default one
                if (filterRequest == null) {
                        filterRequest = new GetAllRfqFilterRequest();
                        filterRequest.setIsDeleted("N");
                        filterRequest.setOperator("AND");
                        filterRequest.setFilters(new java.util.ArrayList<>());
                }

                org.springframework.data.domain.Page<ApRequestForQtnListResponseDto> rfqPage = rfqService
                                .getAllRequestForQuotationsWithFilters(UserContext.getGroupPoid(), UserContext.getCompanyPoid(), filterRequest, page, size);

                // Create displayFields
                Map<String, String> displayFields = new HashMap<>();
                displayFields.put("TRANSACTION_DATE", "date");
                displayFields.put("DOC_REF", "text");
                displayFields.put("SALES_QTN_REF", "text");
                displayFields.put("TRANSACTION_POID", "text");

                // Create paginated response with new structure
                Map<String, Object> response = new HashMap<>();
                response.put("content", rfqPage.getContent());
                response.put("pageNumber", rfqPage.getNumber());
                response.put("displayFields", displayFields);
                response.put("pageSize", rfqPage.getSize());
                response.put("totalElements", rfqPage.getTotalElements());
                response.put("totalPages", rfqPage.getTotalPages());
                response.put("last", rfqPage.isLast());

                return success("Task list fetched successfully", response);
        }

        @Operation(summary = "Create Request For Quotation", description = "Creates a new RFQ document. DocRef is auto-generated. Calls stored procedure after save.", responses = {
                        @ApiResponse(responseCode = "200", description = "Successfully created RFQ", content = @Content(schema = @Schema(implementation = ApRequestForQtnHdrDto.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid input or validation error"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized")
        }, security = @SecurityRequirement(name = "bearerAuth"))
        @AllowedAction(UserRolesRightsEnum.CREATE)
        @PostMapping
        public ResponseEntity<?> createRequestForQuotation(
                        @Valid @RequestBody CreateApRequestForQtnRequest request) {

                ApRequestForQtnHdrDto dto = rfqService.createRequestForQuotation(request, UserContext.getGroupPoid(), UserContext.getCompanyPoid(),
                                UserContext.getUserPoid().toString());
                return success("RFQ created successfully", dto);
        }

        @Operation(summary = "Get RFQ by ID", description = "Returns a specific RFQ document by its Poid.", responses = {
                        @ApiResponse(responseCode = "200", description = "Successfully fetched RFQ", content = @Content(schema = @Schema(implementation = ApRequestForQtnHdrDto.class)))
        })
        @AllowedAction(UserRolesRightsEnum.VIEW)
        @GetMapping("/{transactionPoid:\\d+}")
        public ResponseEntity<?> getRequestForQuotationByPoid(
                        @PathVariable Long transactionPoid,
                        @RequestParam(required = false, defaultValue = "false") Boolean includeDetails) {

                ApRequestForQtnHdrDto dto = rfqService.getRequestForQuotationByPoid(
                                transactionPoid, UserContext.getGroupPoid(), UserContext.getCompanyPoid(), includeDetails);
                return success("RFQ fetched successfully", dto);
        }

        @Operation(summary = "Update RFQ", description = "Updates an existing RFQ document by its Poid.", responses = {
                        @ApiResponse(responseCode = "200", description = "Successfully updated RFQ", content = @Content(schema = @Schema(implementation = ApRequestForQtnHdrDto.class)))
        })
        @AllowedAction(UserRolesRightsEnum.EDIT)
        @PutMapping("/{transactionPoid:\\d+}")
        public ResponseEntity<?> updateRequestForQuotation(
                        @PathVariable Long transactionPoid,
                        @Valid @RequestBody UpdateApRequestForQtnRequest request) {

                ApRequestForQtnHdrDto dto = rfqService.updateRequestForQuotation(
                                transactionPoid, request, UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid().toString());
                return success("RFQ updated successfully", dto);
        }

        @Operation(summary = "Delete RFQ", description = "Deletes an existing RFQ document by its Poid.")
        @AllowedAction(UserRolesRightsEnum.DELETE)
        @DeleteMapping("/{transactionPoid}")
        public ResponseEntity<?> deleteRequestForQuotation(
                        @PathVariable Long transactionPoid) {

                rfqService.deleteRequestForQuotation(transactionPoid, UserContext.getGroupPoid(), UserContext.getCompanyPoid());
                return success("RFQ deleted successfully", null);
        }

        // Detail Table APIs
        // @Operation(summary = "Add Item Detail", description = "Adds a new item detail
        // to RFQ. Auto-populates last price and default unit if applicable.", tags =
        // "RFQ's Item Details")
        // @PostMapping("/{transactionPoid}/item-details")
        // public ResponseEntity<?> addItemDetail(
        // @PathVariable Long transactionPoid,
        // @Valid @RequestBody CreateApRequestForQtnItemDtlRequest request,
        // @RequestHeader("X-Group-Poid") Long groupPoid,
        // @RequestHeader("X-Company-Poid") Long companyPoid,
        // @RequestHeader("X-User-Id") String userId) {

        // ApRequestForQtnItemDtlDto dto = rfqService.addItemDetail(
        // transactionPoid, request, groupPoid, companyPoid, userId);
        // return success("Item detail added successfully", dto);
        // }

        // DEPRECATED: Use PUT /api/ap/request-for-quotations/{transactionPoid} with action field in itemDetails
        // @Operation(summary = "Update Item Detail", description = "Updates an existing item detail in RFQ.", tags = "RFQ's Item Details", responses = {
        //                 @ApiResponse(responseCode = "200", description = "Item detail updated successfully", content = @Content(schema = @Schema(implementation = ApRequestForQtnItemDtlDto.class)))
        // })
        // @PutMapping("/{transactionPoid}/item-details/{detRowId}")
        // public ResponseEntity<?> updateItemDetail(
        //                 @PathVariable Long transactionPoid,
        //                 @PathVariable Long detRowId,
        //                 @Valid @RequestBody CreateApRequestForQtnItemDtlRequest request,
        //                 @RequestHeader("X-Group-Poid") Long groupPoid,
        //                 @RequestHeader("X-Company-Poid") Long companyPoid,
        //                 @RequestHeader("X-User-Id") String userId) {

        //         ApRequestForQtnItemDtlDto dto = rfqService.updateItemDetail(
        //                         transactionPoid, detRowId, request, groupPoid, companyPoid, userId);
        //         return success("Item detail updated successfully", dto);
        // }

        // @Operation(summary = "Delete Item Detail", description = "Deletes an existing
        // item detail in RFQ.", tags = "RFQ's Item Details")
        // @DeleteMapping("/{transactionPoid}/item-details/{detRowId}")
        // public ResponseEntity<?> deleteItemDetail(
        // @PathVariable Long transactionPoid,
        // @PathVariable Long detRowId,
        // @RequestHeader("X-Group-Poid") Long groupPoid,
        // @RequestHeader("X-Company-Poid") Long companyPoid) {

        // rfqService.deleteItemDetail(transactionPoid, detRowId, groupPoid,
        // companyPoid);
        // return success("Item detail deleted successfully", null);
        // }

        // @Operation(summary = "Get Item Details", description = "Returns a list of item details for a specific RFQ.", responses = {
        //                 @ApiResponse(responseCode = "200", description = "Item details fetched successfully", content = @Content(schema = @Schema(implementation = ApRequestForQtnItemDtlDto.class)))
        // })
        // @AllowedAction(UserRolesRightsEnum.VIEW)
        // @GetMapping("/{transactionPoid}/item-details")
        // public ResponseEntity<?> getItemDetails(
        //                 @PathVariable Long transactionPoid) {

        //         List<ApRequestForQtnItemDtlDto> itemDetails = rfqService.getItemDetails(
        //                         transactionPoid, UserContext.getGroupPoid(), UserContext.getCompanyPoid());
        //         return success("Item details fetched successfully", itemDetails);
        // }

        // DEPRECATED: Use POST/PUT /api/ap/request-for-quotations/{transactionPoid} with action field in supplierDetails
        // @Operation(summary = "Add Supplier Detail", description = "Adds a new supplier detail to RFQ. Auto-populates last price and default unit if applicable.", tags = "RFQ's Supplier Details", responses = {
        //                 @ApiResponse(responseCode = "200", description = "Supplier detail added successfully", content = @Content(schema = @Schema(implementation = ApRequestForQtnSupDtlDto.class)))
        // })
        // @PostMapping("/{transactionPoid}/supplier-details")
        // public ResponseEntity<?> addSupplierDetail(
        //                 @PathVariable Long transactionPoid,
        //                 @Valid @RequestBody CreateApRequestForQtnSupDtlRequest request,
        //                 @RequestHeader("X-Group-Poid") Long groupPoid,
        //                 @RequestHeader("X-Company-Poid") Long companyPoid,
        //                 @RequestHeader("X-User-Id") String userId) {

        //         ApRequestForQtnSupDtlDto dto = rfqService.addSupplierDetail(
        //                         transactionPoid, request, groupPoid, companyPoid, userId);
        //         return success("Supplier detail added successfully", dto);
        // }

        // DEPRECATED: Use PUT /api/ap/request-for-quotations/{transactionPoid} with action field in supplierDetails
        // @Operation(summary = "Update Supplier Detail", description = "Updates an existing supplier detail in RFQ.", tags = "RFQ's Supplier Details", responses = {
        //                 @ApiResponse(responseCode = "200", description = "Supplier detail updated successfully", content = @Content(schema = @Schema(implementation = ApRequestForQtnSupDtlDto.class)))
        // })
        // @PutMapping("/{transactionPoid}/supplier-details/{detRowId}")
        // public ResponseEntity<?> updateSupplierDetail(
        //                 @PathVariable Long transactionPoid,
        //                 @PathVariable Long detRowId,
        //                 @Valid @RequestBody CreateApRequestForQtnSupDtlRequest request,
        //                 @RequestHeader("X-Group-Poid") Long groupPoid,
        //                 @RequestHeader("X-Company-Poid") Long companyPoid,
        //                 @RequestHeader("X-User-Id") String userId) {

        //         ApRequestForQtnSupDtlDto dto = rfqService.updateSupplierDetail(
        //                         transactionPoid, detRowId, request, groupPoid, companyPoid, userId);
        //         return success("Supplier detail updated successfully", dto);
        // }

        // DEPRECATED: Use PUT /api/ap/request-for-quotations/{transactionPoid} with action="isDeleted" in supplierDetails
        // @Operation(summary = "Delete Supplier Detail", description = "Deletes an existing supplier detail in RFQ.", tags = "RFQ's Supplier Details", responses = {
        //                 @ApiResponse(responseCode = "200", description = "Supplier detail deleted successfully")
        // })
        // @DeleteMapping("/{transactionPoid}/supplier-details/{detRowId}")
        // public ResponseEntity<?> deleteSupplierDetail(
        //                 @PathVariable Long transactionPoid,
        //                 @PathVariable Long detRowId,
        //                 @RequestHeader("X-Group-Poid") Long groupPoid,
        //                 @RequestHeader("X-Company-Poid") Long companyPoid) {

        //         rfqService.deleteSupplierDetail(transactionPoid, detRowId, groupPoid, companyPoid);
        //         return success("Supplier detail deleted successfully", null);
        // }

        // @Operation(summary = "Get Supplier Details", description = "Returns a list of supplier details for a specific RFQ.", responses = {
        //                 @ApiResponse(responseCode = "200", description = "Supplier details fetched successfully", content = @Content(schema = @Schema(implementation = ApRequestForQtnSupDtlDto.class)))
        // })
        // @AllowedAction(UserRolesRightsEnum.VIEW)
        // @GetMapping("/{transactionPoid}/supplier-details")
        // public ResponseEntity<?> getSupplierDetails(
        //                 @PathVariable Long transactionPoid) {

        //         List<ApRequestForQtnSupDtlDto> supplierDetails = rfqService.getSupplierDetails(
        //                         transactionPoid, UserContext.getGroupPoid(), UserContext.getCompanyPoid());
        //         return success("Supplier details fetched successfully", supplierDetails);
        // }

        // Business Logic APIs
        @Operation(summary = "Add Related Suppliers", description = "Automatically adds suppliers to RFQ based on item details. Calls PROC_AP_RFQ_ADD_SUPPLIERS.", responses = {
                        @ApiResponse(responseCode = "200", description = "Suppliers added successfully", content = @Content(schema = @Schema(implementation = AddSuppliersResponse.class)))
        })
        @AllowedAction(UserRolesRightsEnum.EDIT)
        @PostMapping("/{transactionPoid}/add-suppliers")
        public ResponseEntity<?> addRelatedSuppliers(
                        @PathVariable Long transactionPoid) {

                AddSuppliersResponse response = rfqService.addRelatedSuppliers(
                                transactionPoid, UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid().toString());
                return success(response.getMessage(), response);
        }

        @Operation(summary = "Send Mail to Suppliers", description = "Sends RFQ document via email to all suppliers. Calls PROC_AP_RFQ_CREATE_SEND_MAIL.", responses = {
                        @ApiResponse(responseCode = "200", description = "Mail sent successfully", content = @Content(schema = @Schema(implementation = SendMailResponse.class)))
        })
        @AllowedAction(UserRolesRightsEnum.EMAIL)
        @PostMapping("/{transactionPoid}/send-mail-to-suppliers")
        public ResponseEntity<?> sendMailToSuppliers(
                        @PathVariable Long transactionPoid) {

                SendMailResponse response = rfqService.sendMailToSuppliers(
                                transactionPoid, UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid().toString());
                return success(response.getMessage(), response);
        }

        @Operation(summary = "Create Purchase Order", description = "Creates a Purchase Order from RFQ for selected supplier. Calls PROC_AP_RFQ_CREATE_PO_NEW.", responses = {
                        @ApiResponse(responseCode = "200", description = "Purchase Order created successfully", content = @Content(schema = @Schema(implementation = CreatePurchaseOrderResponse.class)))
        })
        @AllowedAction(UserRolesRightsEnum.CREATE)
        @PostMapping("/{transactionPoid}/create-purchase-order")
        public ResponseEntity<?> createPurchaseOrder(
                        @PathVariable Long transactionPoid,
                        @RequestBody CreatePurchaseOrderRequest request) {

                CreatePurchaseOrderResponse response = rfqService.createPurchaseOrder(
                                transactionPoid, request.getSupplierPoid(), UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid().toString());
                return success(response.getMessage(), response);
        }

        @Operation(summary = "Update Cost", description = "Updates cost in Purchase Orders, Quotations, Delivery Notes, and Sales Invoices. Calls PROC_AP_RFQ_PRICE_UPDATE.", responses = {
                        @ApiResponse(responseCode = "200", description = "Cost updated successfully", content = @Content(schema = @Schema(implementation = UpdateCostResponse.class)))
        })
        @AllowedAction(UserRolesRightsEnum.EDIT)
        @PostMapping("/{transactionPoid}/update-cost")
        public ResponseEntity<?> updateCost(
                        @PathVariable Long transactionPoid,
                        @RequestBody UpdateCostRequest request) {

                UpdateCostResponse response = rfqService.updateCost(
                                transactionPoid, request.getConfirm(), UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid().toString());
                return success(response.getMessage(), response);
        }

        @Operation(summary = "Get Last Price", description = "Retrieves last purchase price for stock, unit, and supplier. Calls PROC_AP_RFQ_CREATE_LAST_PRICE.", responses = {
                        @ApiResponse(responseCode = "200", description = "Last price fetched successfully", content = @Content(schema = @Schema(implementation = LastPriceResponse.class)))
        })
        @AllowedAction(UserRolesRightsEnum.VIEW)
        @GetMapping("/{transactionPoid}/item-details/{detRowId}/last-price")
        public ResponseEntity<?> getLastPrice(
                        @PathVariable Long transactionPoid,
                        @PathVariable Long detRowId,
                        @RequestParam Long stockPoid,
                        @RequestParam Long stockUnitPoid,
                        @RequestParam Long supplierPoid) {

                LastPriceResponse response = rfqService.getLastPrice(
                                stockPoid, stockUnitPoid, supplierPoid, UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid().toString());
                return success("Last price fetched successfully", response);
        }

        @Operation(summary = "Get Default Stock Unit", description = "Retrieves default stock unit for a stock item. Calls PROC_AP_RFQ_SET_DFLT_DTL.", responses = {
                        @ApiResponse(responseCode = "200", description = "Default unit fetched successfully", content = @Content(schema = @Schema(implementation = DefaultUnitResponse.class)))
        })
        @AllowedAction(UserRolesRightsEnum.VIEW)
        @GetMapping("/{transactionPoid}/item-details/{detRowId}/default-unit")
        public ResponseEntity<?> getDefaultStockUnit(
                        @PathVariable Long transactionPoid,
                        @PathVariable Long detRowId,
                        @RequestParam Long stockPoid) {

                DefaultUnitResponse response = rfqService.getDefaultStockUnit(stockPoid);
                return success("Default unit fetched successfully", response);
        }

        @Operation(summary = "Get Items Without Suppliers", description = "Identifies items in RFQ that don't have suppliers assigned. Calls PROC_AP_RFQ_ITEMS_WITHOUT_SUP.", responses = {
                        @ApiResponse(responseCode = "200", description = "Items without suppliers fetched successfully", content = @Content(schema = @Schema(implementation = ItemsWithoutSuppliersResponse.class)))
        })
        @AllowedAction(UserRolesRightsEnum.VIEW)
        @GetMapping("/{transactionPoid}/items-without-suppliers")
        public ResponseEntity<?> getItemsWithoutSuppliers(
                        @PathVariable Long transactionPoid) {

                ItemsWithoutSuppliersResponse response = rfqService.getItemsWithoutSuppliers(
                                transactionPoid, UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserPoid().toString());
                return success("Items without suppliers fetched successfully", response);
        }

        @Operation(summary = "Get Tax Percentage", description = "Retrieves tax percentage for the provided Tax POID using active tax configuration.", responses = {
                        @ApiResponse(responseCode = "200", description = "Tax percentage fetched successfully")
        })
        @AllowedAction(UserRolesRightsEnum.VIEW)
        @GetMapping("/tax-percentage")
        public ResponseEntity<?> getTaxPercentage(
                        @RequestParam Long taxPoid) {

                BigDecimal taxPercentage = rfqService.getTaxPercentage(UserContext.getGroupPoid(), UserContext.getCompanyPoid(), taxPoid);
                Map<String, Object> response = Map.of(
                                "taxPoid", taxPoid,
                                "taxPercentage", taxPercentage);
                return success("Tax percentage fetched successfully", response);
        }

        // @Operation(summary = "Check RFQ Dependencies", description = "Checks if RFQ can be deleted by checking for dependencies (Purchase Orders, etc.)")
        // @AllowedAction(UserRolesRightsEnum.DELETE)
        // @GetMapping("/{transactionPoid}/dependencies")
        // public ResponseEntity<?> checkRfqDependencies(
        //                 @PathVariable Long transactionPoid) {

        //         RfqDependenciesDto dto = rfqService.checkRfqDependencies(
        //                         transactionPoid, UserContext.getGroupPoid(), UserContext.getCompanyPoid());
        //         return success("Dependency check completed", dto);
        // }
}
