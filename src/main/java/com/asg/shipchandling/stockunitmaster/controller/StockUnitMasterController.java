package com.asg.shipchandling.stockunitmaster.controller;

import com.asg.shipchandling.stockunitmaster.dto.FilterRequestDto;
import com.asg.shipchandling.stockunitmaster.dto.StockUnitListResponse;
import com.asg.shipchandling.stockunitmaster.dto.StockUnitMasterDto;
import com.asg.shipchandling.stockunitmaster.dto.UnitDependenciesDto;
import com.asg.shipchandling.stockunitmaster.dto.ValidationResponse;
import com.asg.shipchandling.stockunitmaster.service.StockUnitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import static com.asg.shipchandling.common.ApiResponse.*;

import java.util.List;

@RestController
@RequestMapping("stockunitmaster")
public class StockUnitMasterController {

        @Autowired
        private StockUnitService stockUnitService;

        @Operation(summary = "Get stock unit by ID", description = "Retrieves stock unit details based on the provided stockUnit POID", responses = {
                        @ApiResponse(responseCode = "200", description = "Successfully retrieved the stock unit details", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StockUnitMasterDto.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid input parameters", content = @Content(mediaType = "application/json")),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required", content = @Content(mediaType = "application/json")),
                        @ApiResponse(responseCode = "404", description = "Stock unit not found", content = @Content(mediaType = "application/json"))
        }, security = @SecurityRequirement(name = "bearerAuth"))
        @GetMapping("/{stockUnitPoid}")
        public ResponseEntity<?> getStockUnitByPoid(
                        @Parameter(description = "StockUnitPoid reference identifier", required = true) @PathVariable Long stockUnitPoid,
                        @Parameter(description = "Document identifier", required = true, example = "800-320") @RequestParam String documentId,
                        @Parameter(description = "Action requested", required = true) @RequestParam String actionRequested) {
                StockUnitMasterDto stockUnitMasterDto = stockUnitService.getStockUnitByPoid(stockUnitPoid);
                return success("Task fetched successfully", stockUnitMasterDto);

        }

        @Operation(summary = "Create a new stock unit", description = "Creates a new stock unit with the provided details", responses = {
                        @ApiResponse(responseCode = "201", description = "Successfully created the stock unit", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StockUnitMasterDto.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid input, object invalid", content = @Content(mediaType = "application/json")),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required", content = @Content(mediaType = "application/json")),
                        @ApiResponse(responseCode = "409", description = "Country with the same code already exists", content = @Content(mediaType = "application/json"))
        }, security = @SecurityRequirement(name = "bearerAuth"))
        @PostMapping("/create")
        public ResponseEntity<?> createStockUnit(

                        @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Country object that needs to be created", required = true, content = @Content(mediaType = "application/json", schema = @Schema(implementation = StockUnitMasterDto.class))) @Parameter(description = "Stock unit details to be created", required = true) @Valid @RequestBody StockUnitMasterDto stockUnitMasterDto) {

                StockUnitMasterDto response = stockUnitService.createStockUnit(stockUnitMasterDto);

                return success("Stock unit created successfully", response);
        }

        @Operation(summary = "Update Stock unit details", description = "Updates the details of an existing Stock unit identified by its ID", responses = {
                        @ApiResponse(responseCode = "200", description = "Stock unit updated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StockUnitMasterDto.class), examples = @ExampleObject(value = "{\"status\": 200, \"message\": \"Stock unit updated successfully\", \"data\": {...}}"))),
                        @ApiResponse(responseCode = "400", description = "Invalid input or validation error", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"status\": 400, \"message\": \"Validation error: [field] is required\"}"))),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required", content = @Content(mediaType = "application/json")),
                        @ApiResponse(responseCode = "404", description = "Country not found", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"status\": 404, \"message\": \"Stock unit not found with stockUnitPoid: 123\"}")))
        })
        @PutMapping("/{stockUnitPoid}")
        public ResponseEntity<?> updateStockUnit(
                        @Parameter(description = "ID of the stock unit to update", required = true) @PathVariable Long stockUnitPoid,

                        @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Stockunit object with updated details", required = true, content = @Content(mediaType = "application/json", schema = @Schema(implementation = StockUnitMasterDto.class), examples = @ExampleObject(name = "StockunitUpdateExample", value = """
                                        {
                                            "groupPoid": 1,
                                            "stockunitName": "Updated Country Name",
                                            "stockunitCode": "UCN",
                                            "stockunitName2": "Updated Country Name 2",
                                            "active": "Y",
                                            "stockunitTicketRate": 10.5
                                        }
                                        """))) @Valid @RequestBody StockUnitMasterDto stockUnitMasterDto,
                        @Parameter(description = "Document identifier", required = true, example = "800-320") @RequestParam String documentId,
                        @Parameter(description = "Action requested", required = true) @RequestParam String actionRequested) {
                stockUnitMasterDto.setStockUnitPoid(stockUnitPoid);

                StockUnitMasterDto updatedCountry = stockUnitService.updateStockUnit(stockUnitPoid, stockUnitMasterDto);
                return success("Stock unit updated successfully", updatedCountry);
        }

        @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true, description = """
                        - ### Filters:
                          Use either:
                          1. A single `GLOBALSEARCH` filter, OR
                          2. Any combination of specific fields (STOCK_UNIT_CODE, STOCK_UNIT_NAME, CLASSIFIED, ACTIVE, GROUP_POID, DELETED, etc.).
                          3. operator field will either have "AND" or "OR", if not given will be considered as "OR",
                             not required for GLOBALSEARCH, for non GLOBALSEARCH need to give only 1 time
                          4. isDeleted when 'Y' or null, will search and return non deleted records, 'Y' will check and return deleted records
                          5. sort will be default on primary key ascending if specified in db field otherwise you can override it giving the field name and the direction.

                        - ### Authorization Parameters (handled by interceptor)
                            - **documentId:** Unique identifier for the document
                            - **actionRequested:** Action being performed (`VIEW`)
                        """, content = @Content(schema = @Schema(implementation = FilterRequestDto.class), examples = {
                        @ExampleObject(name = "Stock Unit Filters", value = """
                                        {
                                        "operator": "OR",
                                        "isDeleted": "N",
                                        "filters": [
                                           { "searchField": "STOCK_UNIT_CODE", "searchValue": "rs" },
                                           { "searchField": "STOCK_UNIT_NAME", "searchValue": "r" }
                                        ]
                                        }
                                        """)
        }))
        @PostMapping("/list")
        public ResponseEntity<?> getStockUnitList(
                        @Parameter(description = "Document identifier", required = true, example = "200-001") @RequestParam String documentId,
                        @Parameter(description = "Action requested", required = true) @RequestParam String actionRequested,
                        @Valid @RequestBody FilterRequestDto filterRequest,
                        @ParameterObject Pageable pageable) {

                StockUnitListResponse response = stockUnitService.listStockUnitsWithFilters(filterRequest, pageable);

                return success("Stock units fetched successfully", response);
        }

        @Operation(summary = "Soft delete a stock unit", description = "Marks a stock unit as deleted without permanently removing its data", responses = {
                        @ApiResponse(responseCode = "200", description = "Successfully soft deleted the stock unit", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StockUnitMasterDto.class))),
                        @ApiResponse(responseCode = "404", description = "Stock unit not found", content = @Content(mediaType = "application/json")),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required", content = @Content(mediaType = "application/json"))
        }, security = @SecurityRequirement(name = "bearerAuth"))
        @DeleteMapping("/{stockUnitPoid}")
        public ResponseEntity<?> softDeleteCountry(
                        @Parameter(description = "StockUnitPoid reference identifier", required = true) @PathVariable Long stockUnitPoid,
                        @Parameter(description = "Document identifier", required = true, example = "800-320") @RequestParam String documentId,
                        @Parameter(description = "Action requested", required = true) @RequestParam String actionRequested) {

                stockUnitService.softDeleteStockUnit(stockUnitPoid);
                // Only return a simple message now:
                return success("Stock unit has been soft deleted successfully");
        }

        @Operation(summary = "Validate stock unit code uniqueness", description = "Validates if a stock unit code is unique. Used for real-time validation in UI.", responses = {
                        @ApiResponse(responseCode = "200", description = "Validation result", content = @Content(mediaType = "application/json")),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required", content = @Content(mediaType = "application/json"))
        }, security = @SecurityRequirement(name = "bearerAuth"))
        @GetMapping("/validate-code")
        public ResponseEntity<?> validateStockUnitCode(
                        @RequestParam String stockUnitCode,
                        @RequestHeader("groupPoid") Long groupPoid,
                        @RequestParam(required = false) Long excludeStockUnitPoid) {

                boolean isUnique = stockUnitService.validateStockUnitCode(stockUnitCode, groupPoid,
                                excludeStockUnitPoid);
                if (isUnique) {
                        return success("Stock unit code is available",
                                        new ValidationResponse(isUnique, "Stock unit code is available"));
                } else {
                        return success("Stock unit code already exists",
                                        new ValidationResponse(isUnique, "Stock unit code already exists"));
                }
        }

        @Operation(summary = "Validate stock unit name uniqueness", description = "Validates if a stock unit name is unique. Used for real-time validation in UI.", responses = {
                        @ApiResponse(responseCode = "200", description = "Validation result", content = @Content(mediaType = "application/json")),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required", content = @Content(mediaType = "application/json"))
        }, security = @SecurityRequirement(name = "bearerAuth"))
        @GetMapping("/validate-name")
        public ResponseEntity<?> validateStockUnitName(
                        @Parameter(description = "Stock unit name to validate", required = true) @RequestParam String stockUnitName,
                        @Parameter(description = "Group POID from request context", required = true) @RequestHeader("groupPoid") Long groupPoid,
                        @Parameter(description = "Stock unit POID to exclude (for update scenarios)", required = false) @RequestParam(required = false) Long excludeStockUnitPoid) {

                boolean isUnique = stockUnitService.validateStockUnitName(stockUnitName, groupPoid,
                                excludeStockUnitPoid);

                if (isUnique) {
                        return success("Stock unit name is available",
                                        new ValidationResponse(isUnique, "Stock unit name is available"));
                } else {
                        return success("Stock unit name already exists",
                                        new ValidationResponse(isUnique, "Stock unit name already exists"));
                }
        }

        @Operation(summary = "Check stock unit dependencies", description = "Checks if a stock unit can be deleted by checking for dependencies (stock items, etc.).", responses = {
                        @ApiResponse(responseCode = "200", description = "Dependency check result", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UnitDependenciesDto.class))),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required", content = @Content(mediaType = "application/json")),
                        @ApiResponse(responseCode = "404", description = "Stock unit not found", content = @Content(mediaType = "application/json"))
        }, security = @SecurityRequirement(name = "bearerAuth"))
        @GetMapping("/{stockUnitPoid}/dependencies")
        public ResponseEntity<?> checkUnitDependencies(
                        @Parameter(description = "Stock unit POID", required = true) @PathVariable Long stockUnitPoid,
                        @Parameter(description = "Group POID from request context", required = true) @RequestHeader("groupPoid") Long groupPoid) {

                UnitDependenciesDto dependencies = stockUnitService.checkUnitDependencies(stockUnitPoid, groupPoid);
                return success("Dependency check completed", dependencies);
        }

        @Operation(summary = "Get active stock units only", description = "Returns only active stock units. Commonly used for dropdowns and LOVs where only active units should be shown.", responses = {
                        @ApiResponse(responseCode = "200", description = "Successfully retrieved active stock units", content = @Content(mediaType = "application/json", schema = @Schema(implementation = StockUnitMasterDto.class))),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required", content = @Content(mediaType = "application/json"))
        }, security = @SecurityRequirement(name = "bearerAuth"))

        @GetMapping("/active")
        public ResponseEntity<?> getActiveStockUnits(
                        @RequestHeader("groupPoid") Long groupPoid,
                        @RequestParam(required = false) String classified,
                        @RequestParam(required = false) String search) {

                List<StockUnitMasterDto> units = stockUnitService.getActiveStockUnits(groupPoid, classified, search);
                return success("Active stock units fetched successfully", units);
        }

}
