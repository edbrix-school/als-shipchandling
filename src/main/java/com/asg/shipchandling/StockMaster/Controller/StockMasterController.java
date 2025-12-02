package com.asg.shipchandling.StockMaster.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import static com.asg.shipchandling.common.ApiResponse.success;

import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Sort;
import com.asg.shipchandling.StockMaster.entity.StockMasterEntity;
import com.asg.shipchandling.StockMaster.dto.CreateStockMasterDtlRequest;
import com.asg.shipchandling.StockMaster.dto.CreateStockMasterRequest;
import com.asg.shipchandling.StockMaster.dto.CreateStockMasterWarehouseDtlRequest;
import com.asg.shipchandling.StockMaster.dto.StockMasterDependenciesDto;
import com.asg.shipchandling.StockMaster.dto.StockMasterDtlDto;
import com.asg.shipchandling.StockMaster.dto.StockMasterDto;
import com.asg.shipchandling.StockMaster.dto.StockMasterViewResponse;
import com.asg.shipchandling.StockMaster.dto.StockMasterWarehouseDtlDto;
import com.asg.shipchandling.StockMaster.dto.UpdateStockMasterRequest;
import com.asg.shipchandling.StockMaster.dto.ValidationResponse;
import com.asg.shipchandling.StockMaster.service.StockMasterService;
import com.asg.common.lib.security.util.UserContext;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import org.springframework.web.bind.annotation.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/v1/stockmaster")
public class StockMasterController {
    @Autowired
    private StockMasterService stockMasterService;

    @GetMapping("/{stockPoid}")
    public ResponseEntity<?> getStockMasterById(
            @PathVariable Long stockPoid,
            @RequestParam(required = false, defaultValue = "false") boolean includeDetails,
            @RequestParam(required = false) Long groupPoid) {

        Long finalGroupPoid = (groupPoid != null) ? groupPoid : UserContext.getGroupPoid();
        StockMasterViewResponse response = stockMasterService.getStockMasterById(stockPoid, includeDetails, finalGroupPoid);

        if (response == null) {
            return ResponseEntity.notFound().build();
        }

        return success("Stock master fetched successfully", response);
    }

    @GetMapping("/List")
    public ResponseEntity<?> getStockMasters(
            @RequestParam Map<String, String> filters,
            @RequestParam(required = false) String documentId,
            @RequestParam(required = false) String actionRequested,
            @RequestParam(required = false) Long parentPoid,
            @RequestParam(defaultValue = "false") boolean tree,
            @RequestParam(required = false) String filterValue,
            @RequestParam(defaultValue = "false") boolean includeDeleted,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "seqno") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortOrder) {

        // Check if this is a tree structure request with documentId and actionRequested
        if (tree && documentId != null && "VIEW".equalsIgnoreCase(actionRequested)) {
            Long groupPoid = UserContext.getGroupPoid();
            Long companyPoid = UserContext.getCompanyPoid();
            Long userPoid = filters.containsKey("userPoid") ? Long.parseLong(filters.get("userPoid")) : null;
            
            List<Map<String, Object>> treeStructure = stockMasterService.getStockMastersTreeStructure(
                    groupPoid, filterValue, includeDeleted, companyPoid, userPoid);
            
            return success("Stock Master tree structure retrieved successfully", treeStructure);
        }

        // Check if this is a hierarchical view request (flat list)
        if (documentId != null && "VIEW".equalsIgnoreCase(actionRequested)) {
            Long groupPoid = UserContext.getGroupPoid();
            Long companyPoid = UserContext.getCompanyPoid();
            Long userPoid = filters.containsKey("userPoid") ? Long.parseLong(filters.get("userPoid")) : null;
            
            List<Map<String, Object>> hierarchicalList = stockMasterService.getStockMastersHierarchical(
                    groupPoid, parentPoid, filterValue, includeDeleted, companyPoid, userPoid);
            
            Map<String, Object> data = Map.of(
                    "content", hierarchicalList,
                    "totalElements", hierarchicalList.size());
            
            return success("Stock Master list retrieved successfully", data);
        }

        Sort sort = sortOrder.equalsIgnoreCase("DESC")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        if (tree) {
            Long groupPoid = UserContext.getGroupPoid();
            List<Map<String, Object>> categories = stockMasterService.getStockMastersTree(groupPoid);
            Map<String, Object> data = Map.of("categories", categories);
            return success("Stock masters tree fetched successfully", data);
        } else {
            Page<StockMasterEntity> result = stockMasterService.getStockMasters(filters, pageable);
            Map<String, Object> data = Map.of(
                    "content", result.getContent(),
                    "totalElements", result.getTotalElements(),
                    "totalPages", result.getTotalPages());
            return success("Stock masters list fetched successfully", data);
        }
    }

    @Operation(summary = "Validate Stock Code", description = "Checks if a stock code is unique within the group. Used for real-time validation in UI.", responses = {
            @ApiResponse(responseCode = "200", description = "Validation result", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required", content = @Content(mediaType = "application/json"))
    }, security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/validate-code")
    public ResponseEntity<?> validateStockCode(
            @Parameter(description = "Stock code to validate", required = true) @RequestParam String stockCode,

            @Parameter(description = "Stock Master POID to exclude (for update scenarios)", required = false) @RequestParam(required = false) Long excludeStockPoid) {

        ValidationResponse response = stockMasterService.validateStockCode(stockCode, UserContext.getGroupPoid(), excludeStockPoid);

        return success("Validation completed", response);
    }

    @Operation(summary = "Validate Stock Name", description = "Checks if a stock name is unique within the group. Used for real-time validation in UI.", responses = {
            @ApiResponse(responseCode = "200", description = "Validation result", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required", content = @Content(mediaType = "application/json"))
    }, security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/validate-name")
    public ResponseEntity<?> validateStockName(
            @Parameter(description = "Stock name to validate", required = true) @RequestParam String stockName,

            @Parameter(description = "Stock Master POID to exclude (for update scenarios)", required = false) @RequestParam(required = false) Long excludeStockPoid) {

        ValidationResponse response = stockMasterService.validateStockName(stockName, UserContext.getGroupPoid(), excludeStockPoid);

        return success("Validation completed", response);
    }


    @Operation(summary = "Check Stock Master Dependencies", 
               description = "Checks if stock item can be deleted by checking for dependencies (stock balance, transactions, etc.)")
    @GetMapping("/{stockPoid}/dependencies")
    public ResponseEntity<?> checkStockMasterDependencies(
            @PathVariable Long stockPoid) {
        
        StockMasterDependenciesDto dto = stockMasterService.checkStockMasterDependencies(stockPoid, UserContext.getGroupPoid());
        return success("Dependency check completed", dto);
    }


     @Operation(summary = "Delete stock master")
    @DeleteMapping("/{stockPoid}")
    public ResponseEntity<?> deleteStockMaster(
            @PathVariable Long stockPoid) {
        
        stockMasterService.deleteStockMaster(stockPoid, UserContext.getGroupPoid());
        return success("Stock item deleted successfully", null);
    }


    @Operation(
            summary = "Create stock master",
            description = "Creates a new stock item. StockCode is auto-generated. Calls stored procedures for validation.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully created stock item"),
                    @ApiResponse(responseCode = "400", description = "Invalid input or validation error"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized")
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/Create")
    public ResponseEntity<?> createStockMaster(
            @Valid @RequestBody CreateStockMasterRequest request,
            @RequestHeader("userId") String userId) {

        StockMasterDto dto = stockMasterService.createStockMaster(request, UserContext.getGroupPoid(), UserContext.getCompanyPoid(), userId);
        return success("Stock item created successfully", dto);
    }



   @Operation(
    summary = "Update stock master",
    description = "Updates an existing stock item by POID. Validates uniqueness, applies business rules, and calls stored procedures.",
    responses = {
        @ApiResponse(responseCode = "200", description = "Successfully updated stock item"),
        @ApiResponse(responseCode = "400", description = "Invalid input or validation error"),
        @ApiResponse(responseCode = "403", description = "Group mismatch"),
        @ApiResponse(responseCode = "404", description = "Stock not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    },
    security = @SecurityRequirement(name = "bearerAuth")
)
@PutMapping("/{stockPoid}")
public ResponseEntity<StockMasterDto> updateStockMaster(
        @PathVariable Long stockPoid,
        @Valid @RequestBody UpdateStockMasterRequest request,
        @RequestHeader("userId") String userId) {

    StockMasterDto updated = stockMasterService.updateStockMaster(stockPoid, request, UserContext.getGroupPoid(), UserContext.getCompanyPoid(), userId);
    return ResponseEntity.ok(updated);
}


    @Operation(summary = "Add Supplier Detail")
    @PostMapping("/{stockPoid}/supplier-details")
    public ResponseEntity<?> addSupplierDetail(
            @PathVariable Long stockPoid,
            @Valid @RequestBody CreateStockMasterDtlRequest request,
            @RequestHeader("userId") String userId) {
        
        StockMasterDtlDto dto = stockMasterService.addSupplierDetail(
                stockPoid, request, UserContext.getGroupPoid(), userId);
        return success("Supplier detail added successfully", dto);
    }


    @Operation(summary = "Update Supplier Detail")
    @PutMapping("/{stockPoid}/supplier-details/{detRowId}")
    public ResponseEntity<?> updateSupplierDetail(
            @PathVariable Long stockPoid,
            @PathVariable Long detRowId,
            @Valid @RequestBody CreateStockMasterDtlRequest request,
            @RequestHeader("userId") String userId) {
        
        StockMasterDtlDto dto = stockMasterService.updateSupplierDetail(
                stockPoid, detRowId, request, UserContext.getGroupPoid(), userId);
        return success("Supplier detail updated successfully", dto);
    }


     @Operation(summary = "Delete Supplier Detail")
    @DeleteMapping("/{stockPoid}/supplier-details/{detRowId}")
    public ResponseEntity<?> deleteSupplierDetail(
            @PathVariable Long stockPoid,
            @PathVariable Long detRowId) {
        
        stockMasterService.deleteSupplierDetail(stockPoid, detRowId, UserContext.getGroupPoid());
        return success("Supplier detail deleted successfully", null);
    }

    @Operation(summary = "Get Supplier Details")
    @GetMapping("/{stockPoid}/supplier-details")
    public ResponseEntity<?> getSupplierDetails(
            @PathVariable Long stockPoid) {
        
        List<StockMasterDtlDto> supplierDetails = stockMasterService.getSupplierDetails(stockPoid, UserContext.getGroupPoid());
        return success("Supplier details fetched successfully", supplierDetails);
    }


     @Operation(summary = "Add Warehouse Detail")
    @PostMapping("/{stockPoid}/warehouse-details")
    public ResponseEntity<?> addWarehouseDetail(
            @PathVariable Long stockPoid,
            @Valid @RequestBody CreateStockMasterWarehouseDtlRequest request,
            @RequestHeader("userId") String userId) {
        
        StockMasterWarehouseDtlDto dto = stockMasterService.addWarehouseDetail(
                stockPoid, request, UserContext.getGroupPoid(), userId);
        return success("Warehouse detail added successfully", dto);
    }


    @Operation(summary = "Update Warehouse Detail")
    @PutMapping("/{stockPoid}/warehouse-details/{detRowId}")
    public ResponseEntity<?> updateWarehouseDetail(
            @PathVariable Long stockPoid,
            @PathVariable Long detRowId,
            @Valid @RequestBody CreateStockMasterWarehouseDtlRequest request,
            @RequestHeader("userId") String userId) {
        
        StockMasterWarehouseDtlDto dto = stockMasterService.updateWarehouseDetail(
                stockPoid, detRowId, request, UserContext.getGroupPoid(), userId);
        return success("Warehouse detail updated successfully", dto);
    }

    @Operation(summary = "Delete Warehouse Detail")
    @DeleteMapping("/{stockPoid}/warehouse-details/{detRowId}")
    public ResponseEntity<?> deleteWarehouseDetail(
            @PathVariable Long stockPoid,
            @PathVariable Long detRowId) {
        
        stockMasterService.deleteWarehouseDetail(stockPoid, detRowId, UserContext.getGroupPoid());
        return success("Warehouse detail deleted successfully", null);
    }

    @Operation(summary = "Get Warehouse Details")
    @GetMapping("/{stockPoid}/warehouse-details")
    public ResponseEntity<?> getWarehouseDetails(
            @PathVariable Long stockPoid) {
        
        List<StockMasterWarehouseDtlDto> warehouseDetails = stockMasterService.getWarehouseDetails(stockPoid, UserContext.getGroupPoid());
        return success("Warehouse details fetched successfully", warehouseDetails);
    }

    @GetMapping("/by-barcode/{barcode}")
    public ResponseEntity<StockMasterDto> getStockByBarcode(
            @PathVariable String barcode) {

        StockMasterDto stock = stockMasterService.getStockMasterByBarcode(barcode, UserContext.getGroupPoid());
        return ResponseEntity.ok(stock);
    }

}
