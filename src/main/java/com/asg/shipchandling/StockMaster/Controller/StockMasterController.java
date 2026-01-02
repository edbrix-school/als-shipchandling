package com.asg.shipchandling.StockMaster.Controller;

import com.asg.common.lib.dto.FilterRequestDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Sort;
import com.asg.shipchandling.StockMaster.dto.CreateStockMasterRequest;
import com.asg.shipchandling.StockMaster.dto.StockMasterDto;
import com.asg.shipchandling.StockMaster.dto.StockMasterViewResponse;
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
import com.asg.common.lib.annotation.AllowedAction;
import com.asg.common.lib.enums.UserRolesRightsEnum;

@RestController
@RequestMapping("/v1/stockmaster")
public class StockMasterController {
    @Autowired
    private StockMasterService stockMasterService;

    @Operation(summary = "Get Stock Master by ID", description = "Retrieves a stock master by its POID. Supplier and warehouse details are always included in the response regardless of includeDetails parameter.")
    @GetMapping("/{stockPoid}")
    @AllowedAction(UserRolesRightsEnum.VIEW)
    public ResponseEntity<?> getStockMasterById(
            @PathVariable Long stockPoid,
            @Parameter(description = "Deprecated: Supplier and warehouse details are always included. This parameter is kept for backward compatibility only.")
            @RequestParam(required = false, defaultValue = "false") boolean includeDetails,
            @RequestParam(required = false) Long groupPoid) {

        Long finalGroupPoid = (groupPoid != null) ? groupPoid : UserContext.getGroupPoid();
        StockMasterViewResponse response = stockMasterService.getStockMasterById(stockPoid, includeDetails, finalGroupPoid);

        if (response == null) {
            return ResponseEntity.notFound().build();
        }

        return com.asg.shipchandling.common.ApiResponse.success("Stock master fetched successfully", response);
    }

    @Operation(summary = "Get Stock Masters List", description = "Retrieves a paginated list of stock masters with optional filtering and tree structure support")
    @GetMapping
    @AllowedAction(UserRolesRightsEnum.VIEW)
    public ResponseEntity<?> getStockMastersRoot(
            @Valid @RequestBody FilterRequestDto filterRequest,
            @RequestParam(required = false) Long parentPoid,
            @RequestParam(defaultValue = "false") boolean tree,
            @RequestParam(required = false) String filterValue,
            @RequestParam(defaultValue = "false") boolean includeDeleted,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "seqno") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortOrder) {

        // Delegate to the existing getStockMasters method
        return getStockMasters(filterRequest, parentPoid, tree, filterValue, includeDeleted, page, size, sortBy, sortOrder);
    }

    @GetMapping("/list")
    @AllowedAction(UserRolesRightsEnum.VIEW)
    public ResponseEntity<?> getStockMasters(
            @Valid @RequestBody(required = false) FilterRequestDto filterRequest,
            @RequestParam(required = false) Long parentPoid,
            @RequestParam(defaultValue = "false") boolean tree,
            @RequestParam(required = false) String filterValue,
            @RequestParam(defaultValue = "false") boolean includeDeleted,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "seqno") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortOrder) {

        Long groupPoid = UserContext.getGroupPoid();
        Long companyPoid = UserContext.getCompanyPoid();
        Long userPoid = UserContext.getUserPoid();

        // Check if this is a tree structure request with documentId
        if (tree) {
            List<Map<String, Object>> treeStructure = stockMasterService.getStockMastersTreeStructure(
                    groupPoid, filterValue, includeDeleted, companyPoid, userPoid);

            return com.asg.shipchandling.common.ApiResponse.success("Stock Master tree structure retrieved successfully", treeStructure);
        }

        // Check if this is a hierarchical view request (flat list)
        if (parentPoid != null) {
            List<Map<String, Object>> hierarchicalList = stockMasterService.getStockMastersHierarchical(
                    groupPoid, parentPoid, filterValue, includeDeleted, companyPoid, userPoid);

            Map<String, Object> data = Map.of(
                    "content", hierarchicalList,
                    "totalElements", hierarchicalList.size());

            return com.asg.shipchandling.common.ApiResponse.success("Stock Master list retrieved successfully", data);
        }

        Sort sort = sortOrder.equalsIgnoreCase("DESC")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        if (tree) {
            List<Map<String, Object>> categories = stockMasterService.getStockMastersTree(groupPoid);
            Map<String, Object> data = Map.of("categories", categories);
            return com.asg.shipchandling.common.ApiResponse.success("Stock masters tree fetched successfully", data);
        } else {
            Map<String, Object> result =stockMasterService.listStockMaster(UserContext.getDocumentId(),  filterRequest,  pageable);
            return com.asg.shipchandling.common.ApiResponse.success("Stock masters list fetched successfully", result);
        }
    }

    @Operation(summary = "Validate Stock Code", description = "Checks if a stock code is unique within the group. Used for real-time validation in UI.", responses = {
            @ApiResponse(responseCode = "200", description = "Validation result", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required", content = @Content(mediaType = "application/json"))
    }, security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/validate-code")
    @AllowedAction(UserRolesRightsEnum.VIEW)
    public ResponseEntity<?> validateStockCode(
            @Parameter(description = "Stock code to validate", required = true) @RequestParam String stockCode,

            @Parameter(description = "Stock Master POID to exclude (for update scenarios)", required = false) @RequestParam(required = false) Long excludeStockPoid) {

        ValidationResponse response = stockMasterService.validateStockCode(stockCode, UserContext.getGroupPoid(), excludeStockPoid);

        return com.asg.shipchandling.common.ApiResponse.success("Validation completed", response);
    }

    // @Operation(summary = "Validate Stock Name", description = "Checks if a stock name is unique within the group. Used for real-time validation in UI.", responses = {
    //         @ApiResponse(responseCode = "200", description = "Validation result", content = @Content(mediaType = "application/json")),
    //         @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required", content = @Content(mediaType = "application/json"))
    // }, security = @SecurityRequirement(name = "bearerAuth"))
    // @GetMapping("/validate-name")
    // @AllowedAction(UserRolesRightsEnum.VIEW)
    // public ResponseEntity<?> validateStockName(
    //         @Parameter(description = "Stock name to validate", required = true) @RequestParam String stockName,

    //         @Parameter(description = "Stock Master POID to exclude (for update scenarios)", required = false) @RequestParam(required = false) Long excludeStockPoid) {

    //     ValidationResponse response = stockMasterService.validateStockName(stockName, UserContext.getGroupPoid(), excludeStockPoid);

    //     return com.asg.shipchandling.common.ApiResponse.success("Validation completed", response);
    // }


    // @Operation(summary = "Check Stock Master Dependencies", 
    //            description = "Checks if stock item can be deleted by checking for dependencies (stock balance, transactions, etc.)")
    // @GetMapping("/{stockPoid}/dependencies")
    // @AllowedAction(UserRolesRightsEnum.VIEW)
    // public ResponseEntity<?> checkStockMasterDependencies(
    //         @PathVariable Long stockPoid) {

    //     StockMasterDependenciesDto dto = stockMasterService.checkStockMasterDependencies(stockPoid, UserContext.getGroupPoid());
    //     return com.asg.shipchandling.common.ApiResponse.success("Dependency check completed", dto);
    // }


     @Operation(summary = "Delete stock master")
    @DeleteMapping("/{stockPoid}")
    @AllowedAction(UserRolesRightsEnum.DELETE)
    public ResponseEntity<?> deleteStockMaster(
            @PathVariable Long stockPoid) {

        stockMasterService.deleteStockMaster(stockPoid, UserContext.getGroupPoid());
        return com.asg.shipchandling.common.ApiResponse.success("Stock item deleted successfully", null);
    }


    // @Operation(
    //         summary = "Create or Update stock master",
    //         description = "Creates a new stock item or updates existing one based on actionRequired. StockCode is auto-generated. Calls stored procedures for validation.",
    //         responses = {
    //                 @ApiResponse(responseCode = "200", description = "Successfully created/updated stock item"),
    //                 @ApiResponse(responseCode = "400", description = "Invalid input or validation error"),
    //                 @ApiResponse(responseCode = "401", description = "Unauthorized")
    //         },
    //         security = @SecurityRequirement(name = "bearerAuth")
    // )
    // @PostMapping
    // @AllowedAction(UserRolesRightsEnum.CREATE)
    // public ResponseEntity<?> createOrUpdateStockMaster(
    //         @RequestParam(required = true) String documentId,
    //         @RequestParam(required = true) String actionRequired,
    //         @Valid @RequestBody CreateStockMasterRequest request) {

    //     if ("CREATE".equalsIgnoreCase(actionRequired)) {
    //         StockMasterDto dto = stockMasterService.createStockMaster(request, UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserId());
    //         return com.asg.shipchandling.common.ApiResponse.success("Stock item created successfully", dto);
    //     } else if ("UPDATE".equalsIgnoreCase(actionRequired)) {
    //         if (request.getStockPoid() == null) {
    //             return com.asg.shipchandling.common.ApiResponse.badRequest("stockPoid is required for UPDATE operation");
    //         }
    //         UpdateStockMasterRequest updateRequest = convertToUpdateRequest(request);
    //         StockMasterDto dto = stockMasterService.updateStockMaster(request.getStockPoid(), updateRequest, UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserId());
    //         return com.asg.shipchandling.common.ApiResponse.success("Stock Master Updated Successfully", dto);
    //     } else {
    //         return com.asg.shipchandling.common.ApiResponse.badRequest("Invalid actionRequired. Must be CREATE or UPDATE");
    //     }
    // }

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
    @PostMapping("/create")
    @AllowedAction(UserRolesRightsEnum.CREATE)
    public ResponseEntity<?> createStockMaster(
        @Valid @RequestBody CreateStockMasterRequest request) {
        // Use documentId for logging/context (can be used for audit trail or validation)
        // documentId is available for use in service layer if needed

        StockMasterDto dto = stockMasterService.createStockMaster(request, UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserId());
        return com.asg.shipchandling.common.ApiResponse.success("Stock item created successfully", dto);
    }

    // private UpdateStockMasterRequest convertToUpdateRequest(CreateStockMasterRequest request) {
    //     UpdateStockMasterRequest updateRequest = new UpdateStockMasterRequest();
    //     BeanUtils.copyProperties(request, updateRequest);
    //     return updateRequest;
    // }



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
@AllowedAction(UserRolesRightsEnum.EDIT)
public ResponseEntity<?> updateStockMaster(
        @PathVariable Long stockPoid,
        @Valid @RequestBody UpdateStockMasterRequest request) {

    StockMasterDto updated = stockMasterService.updateStockMaster(stockPoid, request, UserContext.getGroupPoid(), UserContext.getCompanyPoid(), UserContext.getUserId());
    return com.asg.shipchandling.common.ApiResponse.success("Stock Master Updated Successfully", updated);
}


    // @Operation(summary = "Add Supplier Detail")
    // @PostMapping("/{stockPoid}/supplier-details")
    // @AllowedAction(UserRolesRightsEnum.CREATE)
    // public ResponseEntity<?> addSupplierDetail(
    //         @PathVariable Long stockPoid,
    //         @Valid @RequestBody CreateStockMasterDtlRequest request) {

    //     StockMasterDtlDto dto = stockMasterService.addSupplierDetail(
    //             stockPoid, request, UserContext.getGroupPoid(), UserContext.getUserId());
    //     return com.asg.shipchandling.common.ApiResponse.success("Supplier detail added successfully", dto);
    // }


    // @Operation(summary = "Update Supplier Detail")
    // @PutMapping("/{stockPoid}/supplier-details/{detRowId}")
    // @AllowedAction(UserRolesRightsEnum.EDIT)
    // public ResponseEntity<?> updateSupplierDetail(
    //         @PathVariable Long stockPoid,
    //         @PathVariable Long detRowId,
    //         @Valid @RequestBody CreateStockMasterDtlRequest request){

    //     StockMasterDtlDto dto = stockMasterService.updateSupplierDetail(
    //             stockPoid, detRowId, request, UserContext.getGroupPoid(), UserContext.getUserId());
    //     return com.asg.shipchandling.common.ApiResponse.success("Supplier detail updated successfully", dto);
    // }


    //  @Operation(summary = "Delete Supplier Detail")
    // @DeleteMapping("/{stockPoid}/supplier-details/{detRowId}")
    // @AllowedAction(UserRolesRightsEnum.DELETE)
    // public ResponseEntity<?> deleteSupplierDetail(
    //         @PathVariable Long stockPoid,
    //         @PathVariable Long detRowId) {

    //     stockMasterService.deleteSupplierDetail(stockPoid, detRowId, UserContext.getGroupPoid());
    //     return com.asg.shipchandling.common.ApiResponse.success("Supplier detail deleted successfully", null);
    // }

    // @Operation(summary = "Get Supplier Details")
    // @GetMapping("/{stockPoid}/supplier-details")
    // @AllowedAction(UserRolesRightsEnum.VIEW)
    // public ResponseEntity<?> getSupplierDetails(
    //         @PathVariable Long stockPoid) {

    //     List<StockMasterDtlDto> supplierDetails = stockMasterService.getSupplierDetails(stockPoid, UserContext.getGroupPoid());
    //     return com.asg.shipchandling.common.ApiResponse.success("Supplier details fetched successfully", supplierDetails);
    // }


    //  @Operation(summary = "Add Warehouse Detail")
    // @PostMapping("/{stockPoid}/warehouse-details")
    // @AllowedAction(UserRolesRightsEnum.CREATE)
    // public ResponseEntity<?> addWarehouseDetail(
    //         @PathVariable Long stockPoid,
    //         @Valid @RequestBody CreateStockMasterWarehouseDtlRequest request) {

    //     StockMasterWarehouseDtlDto dto = stockMasterService.addWarehouseDetail(
    //             stockPoid, request, UserContext.getGroupPoid(), UserContext.getUserId());
    //     return com.asg.shipchandling.common.ApiResponse.success("Warehouse detail added successfully", dto);
    // }


    // @Operation(summary = "Update Warehouse Detail")
    // @PutMapping("/{stockPoid}/warehouse-details/{detRowId}")
    // @AllowedAction(UserRolesRightsEnum.EDIT)
    // public ResponseEntity<?> updateWarehouseDetail(
    //         @PathVariable Long stockPoid,
    //         @PathVariable Long detRowId,
    //         @Valid @RequestBody CreateStockMasterWarehouseDtlRequest request) {

    //     StockMasterWarehouseDtlDto dto = stockMasterService.updateWarehouseDetail(
    //             stockPoid, detRowId, request, UserContext.getGroupPoid(), UserContext.getUserId());
    //     return com.asg.shipchandling.common.ApiResponse.success("Warehouse detail updated successfully", dto);
    // }

    // @Operation(summary = "Delete Warehouse Detail")
    // @DeleteMapping("/{stockPoid}/warehouse-details/{detRowId}")
    // @AllowedAction(UserRolesRightsEnum.DELETE)
    // public ResponseEntity<?> deleteWarehouseDetail(
    //         @PathVariable Long stockPoid,
    //         @PathVariable Long detRowId) {

    //     stockMasterService.deleteWarehouseDetail(stockPoid, detRowId, UserContext.getGroupPoid());
    //     return com.asg.shipchandling.common.ApiResponse.success("Warehouse detail deleted successfully", null);
    // }

    // @Operation(summary = "Get Warehouse Details")
    // @GetMapping("/{stockPoid}/warehouse-details")
    // @AllowedAction(UserRolesRightsEnum.VIEW)
    // public ResponseEntity<?> getWarehouseDetails(
    //         @PathVariable Long stockPoid) {

    //     List<StockMasterWarehouseDtlDto> warehouseDetails = stockMasterService.getWarehouseDetails(stockPoid, UserContext.getGroupPoid());
    //     return com.asg.shipchandling.common.ApiResponse.success("Warehouse details fetched successfully", warehouseDetails);
    // }

    // @GetMapping("/by-barcode/{barcode}")
    // @AllowedAction(UserRolesRightsEnum.VIEW)
    // public ResponseEntity<StockMasterDto> getStockByBarcode(
    //         @PathVariable String barcode) {

    //     StockMasterDto stock = stockMasterService.getStockMasterByBarcode(barcode, UserContext.getGroupPoid());
    //     return ResponseEntity.ok(stock);
    // }

    // @Operation(summary = "Get Stock Details", description = "Retrieves stock details including category, tax, and unit information for a given stock POID.", responses = {
    //                 @ApiResponse(responseCode = "200", description = "Successfully retrieved stock details"),
    //                 @ApiResponse(responseCode = "404", description = "Stock not found"),
    //                 @ApiResponse(responseCode = "401", description = "Unauthorized")
    // }, security = @SecurityRequirement(name = "bearerAuth"))
    // @GetMapping("/{stockPoid}/details")
    // @AllowedAction(UserRolesRightsEnum.VIEW)
    // public ResponseEntity<?> getStockDetails(
    //                 @PathVariable Long stockPoid) {
    //         StockDetailsResponse response = stockMasterService.getStockDetails(stockPoid, UserContext.getCompanyPoid());
    //         return com.asg.shipchandling.common.ApiResponse.success("Stock details fetched successfully", response);
    // }

}
