package com.asg.shipchandling.stockcategory.controller;

import com.asg.shipchandling.stockcategory.dto.*;
import com.asg.shipchandling.stockcategory.dto.request.CreateStockCategoryRequest;
import com.asg.shipchandling.stockcategory.dto.request.UpdateStockCategoryRequest;
import com.asg.shipchandling.stockcategory.dto.response.ValidationResponse;
import com.asg.shipchandling.stockcategory.service.StockCategoryService;
import com.asg.common.lib.security.util.UserContext;
import com.asg.common.lib.annotation.AllowedAction;
import com.asg.common.lib.enums.UserRolesRightsEnum;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static com.asg.shipchandling.common.ApiResponse.*;

@RestController
@RequestMapping("/v1/stock-categories")
@RequiredArgsConstructor
@Slf4j
public class StockCategoryController {


    private final StockCategoryService stockCategoryService;

    @Operation(
            summary = "Create stock category",
            description = "Creates a new stock category. GL values (Stock, Sales, Cost of Sales) are populated by database trigger after insert.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully created stock category",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = StockCategoryMasterDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input parameters or validation error",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping
    public ResponseEntity<?> createStockCategory(
            @Parameter(description = "Stock category creation request", required = true)
            @Valid @RequestBody CreateStockCategoryRequest request) {

        log.info("createStockCategory started for categoryName={} groupPoid={}", request.getCategoryName(), UserContext.getGroupPoid());
        StockCategoryMasterDto dto = stockCategoryService.createStockCategory(request, UserContext.getGroupPoid(), UserContext.getUserId());
        log.info("createStockCategory completed for categoryPoid={} categoryCode={}",
                dto != null ? dto.getCategoryPoid() : null,
                dto != null ? dto.getCategoryCode() : null);
        return success("Stock category created successfully", dto);
    }

    @Operation(
            summary = "Get stock category by ID",
            description = "Retrieves stock category details based on the provided category POID",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully retrieved the stock category details",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = StockCategoryMasterDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input parameters",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Stock category not found",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/{categoryPoid}")
    public ResponseEntity<?> getStockCategoryByPoid(
            @Parameter(description = "Category POID reference identifier", required = true)
            @PathVariable Long categoryPoid) {

        log.info("getStockCategoryByPoid started for categoryPoid={} groupPoid={} documentId={} actionRequested={}",
                categoryPoid, UserContext.getGroupPoid(), UserContext.getDocumentId(), UserContext.getActionRequested());
        StockCategoryMasterDto dto = stockCategoryService.getStockCategoryByPoid(categoryPoid, UserContext.getGroupPoid());
        log.info("getStockCategoryByPoid completed for categoryPoid={} categoryCode={}",
                categoryPoid, dto != null ? dto.getCategoryCode() : null);
        return success("Stock category fetched successfully", dto);
    }

    @Operation(
            summary = "Update stock category",
            description = "Updates an existing stock category. GL values are read-only and populated by database trigger.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully updated stock category",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = StockCategoryMasterDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input parameters or validation error",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Stock category not found",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PutMapping("/{categoryPoid}")
    public ResponseEntity<?> updateStockCategory(
            @Parameter(description = "Category POID reference identifier", required = true)
            @PathVariable Long categoryPoid,
            @Parameter(description = "Stock category update request", required = true)
            @Valid @RequestBody UpdateStockCategoryRequest request) {

        log.info("updateStockCategory started for categoryPoid={} groupPoid={} requestedCategoryCode={}",
                categoryPoid, UserContext.getGroupPoid(), request.getCategoryCode());
        StockCategoryMasterDto dto = stockCategoryService.updateStockCategory(
                categoryPoid, request, UserContext.getGroupPoid(), UserContext.getUserId());
        log.info("updateStockCategory completed for categoryPoid={} newCategoryCode={}",
                categoryPoid, dto != null ? dto.getCategoryCode() : null);
        return success("Stock category updated successfully", dto);
    }

    @Operation(
            summary = "Delete stock category",
            description = "Soft deletes a stock category. Checks for dependencies (child categories, stock items) before deletion.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully deleted stock category",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Cannot delete category with dependencies",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Stock category not found",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @DeleteMapping("/{categoryPoid}")
    public ResponseEntity<?> deleteStockCategory(
            @Parameter(description = "Category POID reference identifier", required = true)
            @PathVariable Long categoryPoid) {

        log.info("deleteStockCategory started for categoryPoid={} groupPoid={}", categoryPoid, UserContext.getGroupPoid());
        stockCategoryService.deleteStockCategory(categoryPoid, UserContext.getGroupPoid());
        log.info("deleteStockCategory completed for categoryPoid={}", categoryPoid);
        return success("Stock category deleted successfully", null);
    }

//     @Operation(
//             summary = "Get stock category tree",
//             description = "Returns parent categories in tree structure format. Only returns categories where PARENT_CATEGORY_POID IS NULL.",
//             responses = {
//                     @ApiResponse(
//                             responseCode = "200",
//                             description = "Successfully retrieved stock category tree",
//                             content = @Content(
//                                     mediaType = "application/json",
//                                     schema = @Schema(implementation = StockCategoryTreeDto.class)
//                             )
//                     ),
//                     @ApiResponse(
//                             responseCode = "401",
//                             description = "Unauthorized - Authentication required",
//                             content = @Content(mediaType = "application/json")
//                     )
//             },
//             security = @SecurityRequirement(name = "bearerAuth")
//     )
//     @GetMapping("/tree")
//     public ResponseEntity<?> getStockCategoryTree(
//             @Parameter(description = "Filter by category type (GROUP/SUB_GROUP)", required = false)
//             @RequestParam(required = false) String categoryType,
//             @Parameter(description = "Filter by active status (Y/N)", required = false)
//             @RequestParam(required = false) String active) {

//         log.info("getStockCategoryTree started for groupPoid={} categoryType={} active={}", UserContext.getGroupPoid(), categoryType, active);
//         List<StockCategoryTreeDto> tree = stockCategoryService.getStockCategoryTree(UserContext.getGroupPoid(), categoryType, active);
//         log.info("getStockCategoryTree completed for groupPoid={} nodeCount={}",
//                 UserContext.getGroupPoid(), tree != null ? tree.size() : 0);
//         return success("Stock category tree fetched successfully", tree);
//     }

//     @Operation(
//             summary = "Get all stock categories (flat list)",
//             description = "Returns all stock categories in flat list format. Used for LOVs and dropdowns.",
//             responses = {
//                     @ApiResponse(
//                             responseCode = "200",
//                             description = "Successfully retrieved stock categories",
//                             content = @Content(
//                                     mediaType = "application/json",
//                                     schema = @Schema(implementation = StockCategoryMasterDto.class)
//                             )
//                     ),
//                     @ApiResponse(
//                             responseCode = "401",
//                             description = "Unauthorized - Authentication required",
//                             content = @Content(mediaType = "application/json")
//                     )
//             },
//             security = @SecurityRequirement(name = "bearerAuth")
//     )
//     @GetMapping
//     public ResponseEntity<?> getAllStockCategories(
//             @Parameter(description = "Filter by category type (GROUP/SUB_GROUP)", required = false)
//             @RequestParam(required = false) String categoryType,
//             @Parameter(description = "Filter by active status (Y/N)", required = false)
//             @RequestParam(required = false) String active) {

//         log.info("getAllStockCategories started for groupPoid={} categoryType={} active={}", UserContext.getGroupPoid(), categoryType, active);
//         List<StockCategoryMasterDto> categories = stockCategoryService.getAllStockCategories(
//                 UserContext.getGroupPoid(), categoryType, active);
//         log.info("getAllStockCategories completed for groupPoid={} count={}",
//                 UserContext.getGroupPoid(), categories != null ? categories.size() : 0);
//         return success("Stock categories fetched successfully", categories);
//     }

//     @Operation(
//             summary = "Get child categories",
//             description = "Returns all child/sub-categories for a given parent category. Used for lazy loading of tree structure.",
//             responses = {
//                     @ApiResponse(
//                             responseCode = "200",
//                             description = "Successfully retrieved child categories",
//                             content = @Content(
//                                     mediaType = "application/json",
//                                     schema = @Schema(implementation = StockCategoryMasterDto.class)
//                             )
//                     ),
//                     @ApiResponse(
//                             responseCode = "401",
//                             description = "Unauthorized - Authentication required",
//                             content = @Content(mediaType = "application/json")
//                     ),
//                     @ApiResponse(
//                             responseCode = "404",
//                             description = "Parent category not found",
//                             content = @Content(mediaType = "application/json")
//                     )
//             },
//             security = @SecurityRequirement(name = "bearerAuth")
//     )
//     @GetMapping("/{parentCategoryPoid}/children")
//     public ResponseEntity<?> getChildCategories(
//             @Parameter(description = "Parent category POID", required = true)
//             @PathVariable Long parentCategoryPoid) {

//         log.info("getChildCategories started for parentCategoryPoid={} groupPoid={}", parentCategoryPoid, UserContext.getGroupPoid());
//         // Validate parent exists
//         stockCategoryService.getStockCategoryByPoid(parentCategoryPoid, UserContext.getGroupPoid());

//         List<StockCategoryMasterDto> children = stockCategoryService.getChildCategories(
//                 parentCategoryPoid, UserContext.getGroupPoid());
//         log.info("getChildCategories completed for parentCategoryPoid={} childCount={}",
//                 parentCategoryPoid, children != null ? children.size() : 0);
//         return success("Child categories fetched successfully", children);
//     }

//     @Operation(
//             summary = "Validate category code uniqueness",
//             description = "Validates if a category code is unique. Used for real-time validation in UI.",
//             responses = {
//                     @ApiResponse(
//                             responseCode = "200",
//                             description = "Validation result",
//                             content = @Content(mediaType = "application/json")
//                     ),
//                     @ApiResponse(
//                             responseCode = "401",
//                             description = "Unauthorized - Authentication required",
//                             content = @Content(mediaType = "application/json")
//                     )
//             },
//             security = @SecurityRequirement(name = "bearerAuth")
//     )
    @GetMapping("/validate-code")
    public ResponseEntity<?> validateCategoryCode(
            @Parameter(description = "Category code to validate", required = true)
            @RequestParam String categoryCode,
            @Parameter(description = "Category POID to exclude (for update scenarios)", required = false)
            @RequestParam(required = false) Long excludeCategoryPoid) {

        log.info("validateCategoryCode started for categoryCode={} groupPoid={} excludeCategoryPoid={}",
                categoryCode, UserContext.getGroupPoid(), excludeCategoryPoid);
        boolean isUnique = stockCategoryService.validateCategoryCode(categoryCode, UserContext.getGroupPoid(), excludeCategoryPoid);

        if (isUnique) {
            log.info("validateCategoryCode completed - categoryCode={} is available", categoryCode);
            return success("Category code is available", new ValidationResponse(isUnique, "Category code is available"));
        } else {
            log.info("validateCategoryCode completed - categoryCode={} already exists", categoryCode);
            return success("Category code already exists", new ValidationResponse(isUnique, "Category code already exists"));
        }
    }

    @Operation(
            summary = "Validate category name uniqueness",
            description = "Validates if a category name is unique. Used for real-time validation in UI.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Validation result",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/validate-name")
    public ResponseEntity<?> validateCategoryName(
            @Parameter(description = "Category name to validate", required = true)
            @RequestParam String categoryName,
            @Parameter(description = "Category POID to exclude (for update scenarios)", required = false)
            @RequestParam(required = false) Long excludeCategoryPoid) {

        log.info("validateCategoryName started for categoryName={} groupPoid={} excludeCategoryPoid={}",
                categoryName, UserContext.getGroupPoid(), excludeCategoryPoid);
        boolean isUnique = stockCategoryService.validateCategoryName(categoryName, UserContext.getGroupPoid(), excludeCategoryPoid);

        if (isUnique) {
            log.info("validateCategoryName completed - categoryName={} is available", categoryName);
            return success("Category name is available", new ValidationResponse(isUnique, "Category name is available"));
        } else {
            log.info("validateCategoryName completed - categoryName={} already exists", categoryName);
            return success("Category name already exists", new ValidationResponse(isUnique, "Category name already exists"));
        }
    }

//     @Operation(
//             summary = "Get parent category GL values",
//             description = "Returns GL account values (Stock, Sales, Cost of Sales) from a parent category. Used to inherit GL values when creating SUB_GROUP categories.",
//             responses = {
//                     @ApiResponse(
//                             responseCode = "200",
//                             description = "Successfully retrieved parent category GL values",
//                             content = @Content(
//                                     mediaType = "application/json",
//                                     schema = @Schema(implementation = StockCategoryGlValuesDto.class)
//                             )
//                     ),
//                     @ApiResponse(
//                             responseCode = "401",
//                             description = "Unauthorized - Authentication required",
//                             content = @Content(mediaType = "application/json")
//                     ),
//                     @ApiResponse(
//                             responseCode = "404",
//                             description = "Parent category not found",
//                             content = @Content(mediaType = "application/json")
//                     )
//             },
//             security = @SecurityRequirement(name = "bearerAuth")
//     )
//     @GetMapping("/{parentCategoryPoid}/gl-values")
//     public ResponseEntity<?> getParentCategoryGlValues(
//             @Parameter(description = "Parent category POID", required = true)
//             @PathVariable Long parentCategoryPoid) {

//         log.info("getParentCategoryGlValues started for parentCategoryPoid={} groupPoid={}", parentCategoryPoid, UserContext.getGroupPoid());
//         StockCategoryGlValuesDto glValues = stockCategoryService.getParentCategoryGlValues(
//                 parentCategoryPoid, UserContext.getGroupPoid());
//         log.info("getParentCategoryGlValues completed for parentCategoryPoid={} stockGlPoid={}",
//                 parentCategoryPoid, glValues != null ? glValues.getStockGlPoid() : null);
//         return success("Parent category GL values fetched successfully", glValues);
//     }

//     @Operation(
//             summary = "Check category dependencies",
//             description = "Checks if a category can be deleted by checking for dependencies (child categories, stock items, etc.).",
//             responses = {
//                     @ApiResponse(
//                             responseCode = "200",
//                             description = "Dependency check result",
//                             content = @Content(
//                                     mediaType = "application/json",
//                                     schema = @Schema(implementation = CategoryDependenciesDto.class)
//                             )
//                     ),
//                     @ApiResponse(
//                             responseCode = "401",
//                             description = "Unauthorized - Authentication required",
//                             content = @Content(mediaType = "application/json")
//                     ),
//                     @ApiResponse(
//                             responseCode = "404",
//                             description = "Category not found",
//                             content = @Content(mediaType = "application/json")
//                     )
//             },
//             security = @SecurityRequirement(name = "bearerAuth")
//     )
//     @GetMapping("/{categoryPoid}/dependencies")
//     public ResponseEntity<?> checkCategoryDependencies(
//             @Parameter(description = "Category POID", required = true)
//             @PathVariable Long categoryPoid) {

//         log.info("checkCategoryDependencies started for categoryPoid={} groupPoid={}", categoryPoid, UserContext.getGroupPoid());
//         CategoryDependenciesDto dependencies = stockCategoryService.checkCategoryDependencies(
//                 categoryPoid, UserContext.getGroupPoid());
//         log.info("checkCategoryDependencies completed for categoryPoid={} canDelete={} childCount={} stockItemCount={}",
//                 categoryPoid,
//                 dependencies != null ? dependencies.getCanDelete() : null,
//                 dependencies != null ? dependencies.getChildCategoryCount() : null,
//                 dependencies != null ? dependencies.getStockItemCount() : null);
//         return success("Dependency check completed", dependencies);
//     }

//     @Operation(
//             summary = "Get category hierarchy",
//             description = "Returns the full hierarchy path for a category (parent → grandparent → etc.). Used for breadcrumb navigation.",
//             responses = {
//                     @ApiResponse(
//                             responseCode = "200",
//                             description = "Successfully retrieved category hierarchy",
//                             content = @Content(
//                                     mediaType = "application/json",
//                                     schema = @Schema(implementation = StockCategoryHierarchyDto.class)
//                             )
//                     ),
//                     @ApiResponse(
//                             responseCode = "401",
//                             description = "Unauthorized - Authentication required",
//                             content = @Content(mediaType = "application/json")
//                     ),
//                     @ApiResponse(
//                             responseCode = "404",
//                             description = "Category not found",
//                             content = @Content(mediaType = "application/json")
//                     )
//             },
//             security = @SecurityRequirement(name = "bearerAuth")
//     )
//     @GetMapping("/{categoryPoid}/hierarchy")
//     public ResponseEntity<?> getCategoryHierarchy(
//             @Parameter(description = "Category POID", required = true)
//             @PathVariable Long categoryPoid) {

//         log.info("getCategoryHierarchy started for categoryPoid={} groupPoid={}", categoryPoid, UserContext.getGroupPoid());
//         List<StockCategoryHierarchyDto> hierarchy = stockCategoryService.getCategoryHierarchy(
//                 categoryPoid, UserContext.getGroupPoid());
//         log.info("getCategoryHierarchy completed for categoryPoid={} levelsReturned={}",
//                 categoryPoid, hierarchy != null ? hierarchy.size() : 0);
//         return success("Category hierarchy fetched successfully", hierarchy);
//     }

    @Operation(
            summary = "Get stock categories list",
            description = "Retrieves stock categories with support for tree structure, hierarchical view, and paginated list. Supports filtering and sorting. When parentPoid is provided, returns child categories in hierarchical format similar to stock master API.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully retrieved stock masters",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input parameters",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized - Authentication required",
                            content = @Content(mediaType = "application/json")
                    )
            },
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/list")
    @AllowedAction(UserRolesRightsEnum.VIEW)
    public ResponseEntity<?> getStockCategoriesList(
            @Parameter(description = "Filter parameters", required = false)
            @RequestParam Map<String, String> filters,
            @Parameter(description = "Parent POID for hierarchical view", required = false)
            @RequestParam(required = false) Long parentPoid,
            @Parameter(description = "Return tree structure", required = false)
            @RequestParam(defaultValue = "false") boolean tree,
            @Parameter(description = "Filter value for search", required = false)
            @RequestParam(required = false) String filterValue,
            @Parameter(description = "Include deleted items", required = false)
            @RequestParam(defaultValue = "false") boolean includeDeleted,
            @Parameter(description = "Page number (0-indexed)", required = false)
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", required = false)
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort by field", required = false)
            @RequestParam(defaultValue = "seqno") String sortBy,
            @Parameter(description = "Sort order (ASC/DESC)", required = false)
            @RequestParam(defaultValue = "ASC") String sortOrder) {

        Long groupPoid = UserContext.getGroupPoid();
        String documentId = UserContext.getDocumentId();

        boolean hasFilter = filterValue != null && !filterValue.trim().isEmpty();

        // Tree structure (similar to Stock Master tree): return plain array as data
        if (tree && (documentId != null || hasFilter)) {
            List<Map<String, Object>> treeStructure = stockCategoryService.getStockCategoriesHierarchical(
                    groupPoid, parentPoid, filterValue, includeDeleted, true);
            return success("Stock Category tree structure retrieved successfully", treeStructure);
        }

        // Hierarchical list (flat, pagination-like): wrap in { content, totalElements }
        if (parentPoid != null || documentId != null) {
            List<Map<String, Object>> hierarchicalList = stockCategoryService.getStockCategoriesHierarchical(
                    groupPoid, parentPoid, filterValue, includeDeleted, tree);

            Map<String, Object> data = Map.of(
                    "content", hierarchicalList,
                    "totalElements", hierarchicalList.size());

            return success("Stock Category list retrieved successfully", data);
        }

        // When only filterValue is provided (no parentPoid / documentId) in non-tree mode, return plain array as data
        if (hasFilter) {
            List<Map<String, Object>> hierarchicalList = stockCategoryService.getStockCategoriesHierarchical(
                    groupPoid, null, filterValue, includeDeleted, tree);
            return success("Stock Category list retrieved successfully", hierarchicalList);
        }

        // Default behavior: return flat list or tree based on tree parameter
        if (tree) {
            List<StockCategoryTreeDto> treeStructure = stockCategoryService.getStockCategoryTree(groupPoid, null, null);
            return success("Stock category tree fetched successfully", treeStructure);
        } else {
            List<StockCategoryMasterDto> categories = stockCategoryService.getAllStockCategories(groupPoid, null, null);
            return success("Stock categories fetched successfully", categories);
        }
    }
}
