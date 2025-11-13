package com.alsharif.shipchandling.requestforquotation.controller;

import com.alsharif.shipchandling.requestforquotation.dto.RfqDependenciesDto;
import com.alsharif.shipchandling.requestforquotation.dto.request.*;
import com.alsharif.shipchandling.requestforquotation.dto.response.*;
import com.alsharif.shipchandling.requestforquotation.service.ApRequestForQtnService;
import com.alsharif.shipchandling.common.ApiResponse.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.alsharif.shipchandling.common.ApiResponse.success;

@RestController
@RequestMapping("/api/ap/request-for-quotations")
@RequiredArgsConstructor
public class ApRequestForQuotationController {

    private final ApRequestForQtnService rfqService;

    @Operation(
            summary = "Create Request For Quotation",
            description = "Creates a new RFQ document. DocRef is auto-generated. Calls stored procedure after save.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully created RFQ"),
                    @ApiResponse(responseCode = "400", description = "Invalid input or validation error"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized")
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping
    public ResponseEntity<?> createRequestForQuotation(
            @Valid @RequestBody CreateApRequestForQtnRequest request,
            @RequestHeader("X-Group-Poid") Long groupPoid,
            @RequestHeader("X-Company-Poid") Long companyPoid,
            @RequestHeader("X-User-Id") String userId) {
        
        ApRequestForQtnHdrDto dto = rfqService.createRequestForQuotation(request, groupPoid, companyPoid, userId);
        return success("RFQ created successfully", dto);
    }

    @Operation(summary = "Get RFQ by ID")
    @GetMapping("/{transactionPoid}")
    public ResponseEntity<?> getRequestForQuotationByPoid(
            @PathVariable Long transactionPoid,
            @RequestHeader("X-Group-Poid") Long groupPoid,
            @RequestHeader("X-Company-Poid") Long companyPoid,
            @RequestParam(required = false, defaultValue = "false") Boolean includeDetails) {
        
        ApRequestForQtnHdrDto dto = rfqService.getRequestForQuotationByPoid(
                transactionPoid, groupPoid, companyPoid, includeDetails);
        return success("RFQ fetched successfully", dto);
    }

    @Operation(summary = "Update RFQ")
    @PutMapping("/{transactionPoid}")
    public ResponseEntity<?> updateRequestForQuotation(
            @PathVariable Long transactionPoid,
            @Valid @RequestBody UpdateApRequestForQtnRequest request,
            @RequestHeader("X-Group-Poid") Long groupPoid,
            @RequestHeader("X-Company-Poid") Long companyPoid,
            @RequestHeader("X-User-Id") String userId) {
        
        ApRequestForQtnHdrDto dto = rfqService.updateRequestForQuotation(
                transactionPoid, request, groupPoid, companyPoid, userId);
        return success("RFQ updated successfully", dto);
    }

    @Operation(summary = "Delete RFQ")
    @DeleteMapping("/{transactionPoid}")
    public ResponseEntity<?> deleteRequestForQuotation(
            @PathVariable Long transactionPoid,
            @RequestHeader("X-Group-Poid") Long groupPoid,
            @RequestHeader("X-Company-Poid") Long companyPoid) {
        
        rfqService.deleteRequestForQuotation(transactionPoid, groupPoid, companyPoid);
        return success("RFQ deleted successfully", null);
    }

    @Operation(summary = "Get all RFQs")
    @GetMapping
    public ResponseEntity<?> getAllRequestForQuotations(
            @RequestHeader("X-Group-Poid") Long groupPoid,
            @RequestHeader("X-Company-Poid") Long companyPoid,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long divisionPoid,
            @RequestParam(required = false) Long salesQtnPoid,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) java.time.LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) java.time.LocalDate toDate) {
        
        List<ApRequestForQtnHdrDto> rfqs = rfqService.getAllRequestForQuotations(
                groupPoid, companyPoid, status, divisionPoid, salesQtnPoid, search, fromDate, toDate);
        return success("RFQs fetched successfully", rfqs);
    }

    // Detail Table APIs
    @Operation(summary = "Add Item Detail", description = "Adds a new item detail to RFQ. Auto-populates last price and default unit if applicable.")
    @PostMapping("/{transactionPoid}/item-details")
    public ResponseEntity<?> addItemDetail(
            @PathVariable Long transactionPoid,
            @Valid @RequestBody CreateApRequestForQtnItemDtlRequest request,
            @RequestHeader("X-Group-Poid") Long groupPoid,
            @RequestHeader("X-Company-Poid") Long companyPoid,
            @RequestHeader("X-User-Id") String userId) {
        
        ApRequestForQtnItemDtlDto dto = rfqService.addItemDetail(
                transactionPoid, request, groupPoid, companyPoid, userId);
        return success("Item detail added successfully", dto);
    }

    @Operation(summary = "Update Item Detail")
    @PutMapping("/{transactionPoid}/item-details/{detRowId}")
    public ResponseEntity<?> updateItemDetail(
            @PathVariable Long transactionPoid,
            @PathVariable Long detRowId,
            @Valid @RequestBody CreateApRequestForQtnItemDtlRequest request,
            @RequestHeader("X-Group-Poid") Long groupPoid,
            @RequestHeader("X-Company-Poid") Long companyPoid,
            @RequestHeader("X-User-Id") String userId) {
        
        ApRequestForQtnItemDtlDto dto = rfqService.updateItemDetail(
                transactionPoid, detRowId, request, groupPoid, companyPoid, userId);
        return success("Item detail updated successfully", dto);
    }

    @Operation(summary = "Delete Item Detail")
    @DeleteMapping("/{transactionPoid}/item-details/{detRowId}")
    public ResponseEntity<?> deleteItemDetail(
            @PathVariable Long transactionPoid,
            @PathVariable Long detRowId,
            @RequestHeader("X-Group-Poid") Long groupPoid,
            @RequestHeader("X-Company-Poid") Long companyPoid) {
        
        rfqService.deleteItemDetail(transactionPoid, detRowId, groupPoid, companyPoid);
        return success("Item detail deleted successfully", null);
    }

    @Operation(summary = "Get Item Details")
    @GetMapping("/{transactionPoid}/item-details")
    public ResponseEntity<?> getItemDetails(
            @PathVariable Long transactionPoid,
            @RequestHeader("X-Group-Poid") Long groupPoid,
            @RequestHeader("X-Company-Poid") Long companyPoid) {
        
        List<ApRequestForQtnItemDtlDto> itemDetails = rfqService.getItemDetails(
                transactionPoid, groupPoid, companyPoid);
        return success("Item details fetched successfully", itemDetails);
    }

    @Operation(summary = "Add Supplier Detail")
    @PostMapping("/{transactionPoid}/supplier-details")
    public ResponseEntity<?> addSupplierDetail(
            @PathVariable Long transactionPoid,
            @Valid @RequestBody CreateApRequestForQtnSupDtlRequest request,
            @RequestHeader("X-Group-Poid") Long groupPoid,
            @RequestHeader("X-Company-Poid") Long companyPoid,
            @RequestHeader("X-User-Id") String userId) {
        
        ApRequestForQtnSupDtlDto dto = rfqService.addSupplierDetail(
                transactionPoid, request, groupPoid, companyPoid, userId);
        return success("Supplier detail added successfully", dto);
    }

    @Operation(summary = "Update Supplier Detail")
    @PutMapping("/{transactionPoid}/supplier-details/{detRowId}")
    public ResponseEntity<?> updateSupplierDetail(
            @PathVariable Long transactionPoid,
            @PathVariable Long detRowId,
            @Valid @RequestBody CreateApRequestForQtnSupDtlRequest request,
            @RequestHeader("X-Group-Poid") Long groupPoid,
            @RequestHeader("X-Company-Poid") Long companyPoid,
            @RequestHeader("X-User-Id") String userId) {
        
        ApRequestForQtnSupDtlDto dto = rfqService.updateSupplierDetail(
                transactionPoid, detRowId, request, groupPoid, companyPoid, userId);
        return success("Supplier detail updated successfully", dto);
    }

    @Operation(summary = "Delete Supplier Detail")
    @DeleteMapping("/{transactionPoid}/supplier-details/{detRowId}")
    public ResponseEntity<?> deleteSupplierDetail(
            @PathVariable Long transactionPoid,
            @PathVariable Long detRowId,
            @RequestHeader("X-Group-Poid") Long groupPoid,
            @RequestHeader("X-Company-Poid") Long companyPoid) {
        
        rfqService.deleteSupplierDetail(transactionPoid, detRowId, groupPoid, companyPoid);
        return success("Supplier detail deleted successfully", null);
    }

    @Operation(summary = "Get Supplier Details")
    @GetMapping("/{transactionPoid}/supplier-details")
    public ResponseEntity<?> getSupplierDetails(
            @PathVariable Long transactionPoid,
            @RequestHeader("X-Group-Poid") Long groupPoid,
            @RequestHeader("X-Company-Poid") Long companyPoid) {
        
        List<ApRequestForQtnSupDtlDto> supplierDetails = rfqService.getSupplierDetails(
                transactionPoid, groupPoid, companyPoid);
        return success("Supplier details fetched successfully", supplierDetails);
    }

    // Business Logic APIs
    @Operation(summary = "Add Related Suppliers", 
               description = "Automatically adds suppliers to RFQ based on item details. Calls PROC_AP_RFQ_ADD_SUPPLIERS.")
    @PostMapping("/{transactionPoid}/add-suppliers")
    public ResponseEntity<?> addRelatedSuppliers(
            @PathVariable Long transactionPoid,
            @RequestHeader("X-Group-Poid") Long groupPoid,
            @RequestHeader("X-Company-Poid") Long companyPoid,
            @RequestHeader("X-User-Id") String userId) {
        
        AddSuppliersResponse response = rfqService.addRelatedSuppliers(
                transactionPoid, groupPoid, companyPoid, userId);
        return success(response.getMessage(), response);
    }

    @Operation(summary = "Send Mail to Suppliers", 
               description = "Sends RFQ document via email to all suppliers. Calls PROC_AP_RFQ_CREATE_SEND_MAIL.")
    @PostMapping("/{transactionPoid}/send-mail-to-suppliers")
    public ResponseEntity<?> sendMailToSuppliers(
            @PathVariable Long transactionPoid,
            @RequestHeader("X-Group-Poid") Long groupPoid,
            @RequestHeader("X-Company-Poid") Long companyPoid,
            @RequestHeader("X-User-Id") String userId) {
        
        SendMailResponse response = rfqService.sendMailToSuppliers(
                transactionPoid, groupPoid, companyPoid, userId);
        return success(response.getMessage(), response);
    }

    @Operation(summary = "Create Purchase Order", 
               description = "Creates a Purchase Order from RFQ for selected supplier. Calls PROC_AP_RFQ_CREATE_PO_NEW.")
    @PostMapping("/{transactionPoid}/create-purchase-order")
    public ResponseEntity<?> createPurchaseOrder(
            @PathVariable Long transactionPoid,
            @RequestBody CreatePurchaseOrderRequest request,
            @RequestHeader("X-Group-Poid") Long groupPoid,
            @RequestHeader("X-Company-Poid") Long companyPoid,
            @RequestHeader("X-User-Id") String userId) {
        
        CreatePurchaseOrderResponse response = rfqService.createPurchaseOrder(
                transactionPoid, request.getSupplierPoid(), groupPoid, companyPoid, userId);
        return success(response.getMessage(), response);
    }

    @Operation(summary = "Update Cost", 
               description = "Updates cost in Purchase Orders, Quotations, Delivery Notes, and Sales Invoices. Calls PROC_AP_RFQ_PRICE_UPDATE.")
    @PostMapping("/{transactionPoid}/update-cost")
    public ResponseEntity<?> updateCost(
            @PathVariable Long transactionPoid,
            @RequestBody UpdateCostRequest request,
            @RequestHeader("X-Group-Poid") Long groupPoid,
            @RequestHeader("X-Company-Poid") Long companyPoid,
            @RequestHeader("X-User-Id") String userId) {
        
        UpdateCostResponse response = rfqService.updateCost(
                transactionPoid, request.getConfirm(), groupPoid, companyPoid, userId);
        return success(response.getMessage(), response);
    }

    @Operation(summary = "Get Last Price", 
               description = "Retrieves last purchase price for stock, unit, and supplier. Calls PROC_AP_RFQ_CREATE_LAST_PRICE.")
    @GetMapping("/{transactionPoid}/item-details/{detRowId}/last-price")
    public ResponseEntity<?> getLastPrice(
            @PathVariable Long transactionPoid,
            @PathVariable Long detRowId,
            @RequestParam Long stockPoid,
            @RequestParam Long stockUnitPoid,
            @RequestParam Long supplierPoid,
            @RequestHeader("X-Group-Poid") Long groupPoid,
            @RequestHeader("X-Company-Poid") Long companyPoid,
            @RequestHeader("X-User-Id") String userId) {
        
        LastPriceResponse response = rfqService.getLastPrice(
                stockPoid, stockUnitPoid, supplierPoid, groupPoid, companyPoid, userId);
        return success("Last price fetched successfully", response);
    }

    @Operation(summary = "Get Default Stock Unit", 
               description = "Retrieves default stock unit for a stock item. Calls PROC_AP_RFQ_SET_DFLT_DTL.")
    @GetMapping("/{transactionPoid}/item-details/{detRowId}/default-unit")
    public ResponseEntity<?> getDefaultStockUnit(
            @PathVariable Long transactionPoid,
            @PathVariable Long detRowId,
            @RequestParam Long stockPoid) {
        
        DefaultUnitResponse response = rfqService.getDefaultStockUnit(stockPoid);
        return success("Default unit fetched successfully", response);
    }

    @Operation(summary = "Get Items Without Suppliers", 
               description = "Identifies items in RFQ that don't have suppliers assigned. Calls PROC_AP_RFQ_ITEMS_WITHOUT_SUP.")
    @GetMapping("/{transactionPoid}/items-without-suppliers")
    public ResponseEntity<?> getItemsWithoutSuppliers(
            @PathVariable Long transactionPoid,
            @RequestHeader("X-Group-Poid") Long groupPoid,
            @RequestHeader("X-Company-Poid") Long companyPoid,
            @RequestHeader("X-User-Id") String userId) {
        
        ItemsWithoutSuppliersResponse response = rfqService.getItemsWithoutSuppliers(
                transactionPoid, groupPoid, companyPoid, userId);
        return success("Items without suppliers fetched successfully", response);
    }

    @Operation(summary = "Check RFQ Dependencies", 
               description = "Checks if RFQ can be deleted by checking for dependencies (Purchase Orders, etc.)")
    @GetMapping("/{transactionPoid}/dependencies")
    public ResponseEntity<?> checkRfqDependencies(
            @PathVariable Long transactionPoid,
            @RequestHeader("X-Group-Poid") Long groupPoid,
            @RequestHeader("X-Company-Poid") Long companyPoid) {
        
        RfqDependenciesDto dto = rfqService.checkRfqDependencies(
                transactionPoid, groupPoid, companyPoid);
        return success("Dependency check completed", dto);
    }
}
