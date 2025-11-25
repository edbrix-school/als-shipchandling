package com.alsharif.shipchandling.requestforquotation.controller;

import com.alsharif.shipchandling.requestforquotation.dto.request.*;
import com.alsharif.shipchandling.requestforquotation.dto.response.*;
import com.alsharif.shipchandling.requestforquotation.service.ApRequestForQtnService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ApRequestForQuotationController.class)
class ApRequestForQuotationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ApRequestForQtnService rfqService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final Long TEST_GROUP_POID = 1L;
    private static final Long TEST_COMPANY_POID = 2L;
    private static final Long TEST_TRANSACTION_POID = 100L;
    private static final String TEST_USER_ID = "testUser";
    private static final Long TEST_DET_ROW_ID = 1L;

    @BeforeEach
    void setUp() {
        // Common setup if needed
    }

    // ========== Header/CRUD Operations Tests ==========

    @Test
    void testCreateRequestForQuotation_Success() throws Exception {
        // Arrange
        CreateApRequestForQtnRequest request = new CreateApRequestForQtnRequest();
        request.setDescription("Test RFQ");
        request.setTransactionDate(Timestamp.from(Instant.now()));

        ApRequestForQtnHdrDto responseDto = new ApRequestForQtnHdrDto();
        responseDto.setTransactionPoid(TEST_TRANSACTION_POID);
        responseDto.setDocRef("RFQ-001");
        responseDto.setDescription("Test RFQ");

        when(rfqService.createRequestForQuotation(any(CreateApRequestForQtnRequest.class),
                eq(TEST_GROUP_POID), eq(TEST_COMPANY_POID), eq(TEST_USER_ID)))
                .thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(post("/api/ap/request-for-quotations")
                        .header("X-Group-Poid", TEST_GROUP_POID)
                        .header("X-Company-Poid", TEST_COMPANY_POID)
                        .header("X-User-Id", TEST_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("RFQ created successfully"))
                .andExpect(jsonPath("$.result.data.transactionPoid").value(TEST_TRANSACTION_POID));

        verify(rfqService, times(1)).createRequestForQuotation(
                any(CreateApRequestForQtnRequest.class), eq(TEST_GROUP_POID), eq(TEST_COMPANY_POID), eq(TEST_USER_ID));
    }

    @Test
    void testCreateRequestForQuotation_MissingHeaders() throws Exception {
        // Arrange
        CreateApRequestForQtnRequest request = new CreateApRequestForQtnRequest();
        request.setDescription("Test RFQ");

        // Act & Assert - Missing Group POID
        mockMvc.perform(post("/api/ap/request-for-quotations")
                        .header("X-Company-Poid", TEST_COMPANY_POID)
                        .header("X-User-Id", TEST_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetRequestForQuotationByPoid_Success() throws Exception {
        // Arrange
        ApRequestForQtnHdrDto responseDto = new ApRequestForQtnHdrDto();
        responseDto.setTransactionPoid(TEST_TRANSACTION_POID);
        responseDto.setDocRef("RFQ-001");
        responseDto.setDescription("Test RFQ");

        when(rfqService.getRequestForQuotationByPoid(
                eq(TEST_TRANSACTION_POID), eq(TEST_GROUP_POID), eq(TEST_COMPANY_POID), eq(false)))
                .thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(get("/api/ap/request-for-quotations/{transactionPoid}", TEST_TRANSACTION_POID)
                        .header("X-Group-Poid", TEST_GROUP_POID)
                        .header("X-Company-Poid", TEST_COMPANY_POID)
                        .param("includeDetails", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("RFQ fetched successfully"))
                .andExpect(jsonPath("$.result.data.transactionPoid").value(TEST_TRANSACTION_POID));

        verify(rfqService, times(1)).getRequestForQuotationByPoid(
                eq(TEST_TRANSACTION_POID), eq(TEST_GROUP_POID), eq(TEST_COMPANY_POID), eq(false));
    }

    @Test
    void testGetRequestForQuotationByPoid_NotFound() throws Exception {
        // Arrange
        when(rfqService.getRequestForQuotationByPoid(
                eq(TEST_TRANSACTION_POID), eq(TEST_GROUP_POID), eq(TEST_COMPANY_POID), anyBoolean()))
                .thenThrow(new com.alsharif.shipchandling.exceptions.ResourceNotFoundException("RFQ", "transactionPoid", TEST_TRANSACTION_POID));

        // Act & Assert
        mockMvc.perform(get("/api/ap/request-for-quotations/{transactionPoid}", TEST_TRANSACTION_POID)
                        .header("X-Group-Poid", TEST_GROUP_POID)
                        .header("X-Company-Poid", TEST_COMPANY_POID))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateRequestForQuotation_Success() throws Exception {
        // Arrange
        UpdateApRequestForQtnRequest request = new UpdateApRequestForQtnRequest();
        request.setDescription("Updated RFQ");
        request.setTransactionDate(Timestamp.from(Instant.now()));

        ApRequestForQtnHdrDto responseDto = new ApRequestForQtnHdrDto();
        responseDto.setTransactionPoid(TEST_TRANSACTION_POID);
        responseDto.setDescription("Updated RFQ");

        when(rfqService.updateRequestForQuotation(
                eq(TEST_TRANSACTION_POID), any(UpdateApRequestForQtnRequest.class),
                eq(TEST_GROUP_POID), eq(TEST_COMPANY_POID), eq(TEST_USER_ID)))
                .thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(put("/api/ap/request-for-quotations/{transactionPoid}", TEST_TRANSACTION_POID)
                        .header("X-Group-Poid", TEST_GROUP_POID)
                        .header("X-Company-Poid", TEST_COMPANY_POID)
                        .header("X-User-Id", TEST_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("RFQ updated successfully"));

        verify(rfqService, times(1)).updateRequestForQuotation(
                eq(TEST_TRANSACTION_POID), any(UpdateApRequestForQtnRequest.class),
                eq(TEST_GROUP_POID), eq(TEST_COMPANY_POID), eq(TEST_USER_ID));
    }

    @Test
    void testUpdateRequestForQuotation_ClosedStatus() throws Exception {
        // Arrange
        UpdateApRequestForQtnRequest request = new UpdateApRequestForQtnRequest();
        request.setTransactionDate(Timestamp.from(Instant.now()));

        when(rfqService.updateRequestForQuotation(
                eq(TEST_TRANSACTION_POID), any(UpdateApRequestForQtnRequest.class),
                eq(TEST_GROUP_POID), eq(TEST_COMPANY_POID), eq(TEST_USER_ID)))
                .thenThrow(new com.alsharif.shipchandling.exceptions.CustomException("Current document is in closed status"));

        // Act & Assert
        mockMvc.perform(put("/api/ap/request-for-quotations/{transactionPoid}", TEST_TRANSACTION_POID)
                        .header("X-Group-Poid", TEST_GROUP_POID)
                        .header("X-Company-Poid", TEST_COMPANY_POID)
                        .header("X-User-Id", TEST_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testGetAllRequestForQuotations_WithFilters() throws Exception {
        // Arrange
        ApRequestForQtnHdrDto dto = new ApRequestForQtnHdrDto();
        dto.setTransactionPoid(TEST_TRANSACTION_POID);
        dto.setDocRef("RFQ-001");

        List<ApRequestForQtnHdrDto> dtoList = Collections.singletonList(dto);
        Page<ApRequestForQtnHdrDto> page = new PageImpl<>(dtoList, PageRequest.of(0, 20), 1);

        when(rfqService.getAllRequestForQuotations(
                eq(TEST_GROUP_POID), eq(TEST_COMPANY_POID), eq("IN PROGRESS"),
                eq(10L), eq(20L), eq("test"), any(), any(), eq(0), eq(20)))
                .thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/ap/request-for-quotations")
                        .header("X-Group-Poid", TEST_GROUP_POID)
                        .header("X-Company-Poid", TEST_COMPANY_POID)
                        .param("status", "IN PROGRESS")
                        .param("divisionPoid", "10")
                        .param("salesQtnPoid", "20")
                        .param("search", "test")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.result.data.content").isArray())
                .andExpect(jsonPath("$.result.data.totalElements").value(1));

        verify(rfqService, times(1)).getAllRequestForQuotations(
                eq(TEST_GROUP_POID), eq(TEST_COMPANY_POID), eq("IN PROGRESS"),
                eq(10L), eq(20L), eq("test"), any(), any(), eq(0), eq(20));
    }

    // ========== Item Detail Operations Tests ==========

    @Test
    void testUpdateItemDetail_Success() throws Exception {
        // Arrange
        CreateApRequestForQtnItemDtlRequest request = new CreateApRequestForQtnItemDtlRequest();
        request.setStockPoid(100L);
        request.setStockUnitPoid(1L);
        request.setQty(BigDecimal.valueOf(10));
        request.setPrice(BigDecimal.valueOf(100));

        ApRequestForQtnItemDtlDto responseDto = new ApRequestForQtnItemDtlDto();
        responseDto.setTransactionPoid(TEST_TRANSACTION_POID);
        responseDto.setDetRowId(TEST_DET_ROW_ID);
        responseDto.setStockPoid(100L);

        when(rfqService.updateItemDetail(
                eq(TEST_TRANSACTION_POID), eq(TEST_DET_ROW_ID), any(CreateApRequestForQtnItemDtlRequest.class),
                eq(TEST_GROUP_POID), eq(TEST_COMPANY_POID), eq(TEST_USER_ID)))
                .thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(put("/api/ap/request-for-quotations/{transactionPoid}/item-details/{detRowId}",
                        TEST_TRANSACTION_POID, TEST_DET_ROW_ID)
                        .header("X-Group-Poid", TEST_GROUP_POID)
                        .header("X-Company-Poid", TEST_COMPANY_POID)
                        .header("X-User-Id", TEST_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Item detail updated successfully"));

        verify(rfqService, times(1)).updateItemDetail(
                eq(TEST_TRANSACTION_POID), eq(TEST_DET_ROW_ID), any(CreateApRequestForQtnItemDtlRequest.class),
                eq(TEST_GROUP_POID), eq(TEST_COMPANY_POID), eq(TEST_USER_ID));
    }

    @Test
    void testGetItemDetails_Success() throws Exception {
        // Arrange
        ApRequestForQtnItemDtlDto itemDto = new ApRequestForQtnItemDtlDto();
        itemDto.setTransactionPoid(TEST_TRANSACTION_POID);
        itemDto.setDetRowId(TEST_DET_ROW_ID);
        itemDto.setStockPoid(100L);

        List<ApRequestForQtnItemDtlDto> itemList = Collections.singletonList(itemDto);

        when(rfqService.getItemDetails(
                eq(TEST_TRANSACTION_POID), eq(TEST_GROUP_POID), eq(TEST_COMPANY_POID)))
                .thenReturn(itemList);

        // Act & Assert
        mockMvc.perform(get("/api/ap/request-for-quotations/{transactionPoid}/item-details", TEST_TRANSACTION_POID)
                        .header("X-Group-Poid", TEST_GROUP_POID)
                        .header("X-Company-Poid", TEST_COMPANY_POID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Item details fetched successfully"))
                .andExpect(jsonPath("$.result.data").isArray());

        verify(rfqService, times(1)).getItemDetails(
                eq(TEST_TRANSACTION_POID), eq(TEST_GROUP_POID), eq(TEST_COMPANY_POID));
    }

    // ========== Supplier Detail Operations Tests ==========

    @Test
    void testAddSupplierDetail_Success() throws Exception {
        // Arrange
        CreateApRequestForQtnSupDtlRequest request = new CreateApRequestForQtnSupDtlRequest();
        request.setSupplierPoid(50L);
        request.setRemarks("Test supplier");

        ApRequestForQtnSupDtlDto responseDto = new ApRequestForQtnSupDtlDto();
        responseDto.setTransactionPoid(TEST_TRANSACTION_POID);
        responseDto.setDetRowId(TEST_DET_ROW_ID);
        responseDto.setSupplierPoid(50L);

        when(rfqService.addSupplierDetail(
                eq(TEST_TRANSACTION_POID), any(CreateApRequestForQtnSupDtlRequest.class),
                eq(TEST_GROUP_POID), eq(TEST_COMPANY_POID), eq(TEST_USER_ID)))
                .thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(post("/api/ap/request-for-quotations/{transactionPoid}/supplier-details", TEST_TRANSACTION_POID)
                        .header("X-Group-Poid", TEST_GROUP_POID)
                        .header("X-Company-Poid", TEST_COMPANY_POID)
                        .header("X-User-Id", TEST_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Supplier detail added successfully"));

        verify(rfqService, times(1)).addSupplierDetail(
                eq(TEST_TRANSACTION_POID), any(CreateApRequestForQtnSupDtlRequest.class),
                eq(TEST_GROUP_POID), eq(TEST_COMPANY_POID), eq(TEST_USER_ID));
    }

    @Test
    void testUpdateSupplierDetail_Success() throws Exception {
        // Arrange
        CreateApRequestForQtnSupDtlRequest request = new CreateApRequestForQtnSupDtlRequest();
        request.setSupplierPoid(60L);
        request.setRemarks("Updated supplier");

        ApRequestForQtnSupDtlDto responseDto = new ApRequestForQtnSupDtlDto();
        responseDto.setTransactionPoid(TEST_TRANSACTION_POID);
        responseDto.setDetRowId(TEST_DET_ROW_ID);
        responseDto.setSupplierPoid(60L);

        when(rfqService.updateSupplierDetail(
                eq(TEST_TRANSACTION_POID), eq(TEST_DET_ROW_ID), any(CreateApRequestForQtnSupDtlRequest.class),
                eq(TEST_GROUP_POID), eq(TEST_COMPANY_POID), eq(TEST_USER_ID)))
                .thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(put("/api/ap/request-for-quotations/{transactionPoid}/supplier-details/{detRowId}",
                        TEST_TRANSACTION_POID, TEST_DET_ROW_ID)
                        .header("X-Group-Poid", TEST_GROUP_POID)
                        .header("X-Company-Poid", TEST_COMPANY_POID)
                        .header("X-User-Id", TEST_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Supplier detail updated successfully"));

        verify(rfqService, times(1)).updateSupplierDetail(
                eq(TEST_TRANSACTION_POID), eq(TEST_DET_ROW_ID), any(CreateApRequestForQtnSupDtlRequest.class),
                eq(TEST_GROUP_POID), eq(TEST_COMPANY_POID), eq(TEST_USER_ID));
    }

    @Test
    void testDeleteSupplierDetail_Success() throws Exception {
        // Arrange
        doNothing().when(rfqService).deleteSupplierDetail(
                eq(TEST_TRANSACTION_POID), eq(TEST_DET_ROW_ID), eq(TEST_GROUP_POID), eq(TEST_COMPANY_POID));

        // Act & Assert
        mockMvc.perform(delete("/api/ap/request-for-quotations/{transactionPoid}/supplier-details/{detRowId}",
                        TEST_TRANSACTION_POID, TEST_DET_ROW_ID)
                        .header("X-Group-Poid", TEST_GROUP_POID)
                        .header("X-Company-Poid", TEST_COMPANY_POID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Supplier detail deleted successfully"));

        verify(rfqService, times(1)).deleteSupplierDetail(
                eq(TEST_TRANSACTION_POID), eq(TEST_DET_ROW_ID), eq(TEST_GROUP_POID), eq(TEST_COMPANY_POID));
    }

    @Test
    void testGetSupplierDetails_Success() throws Exception {
        // Arrange
        ApRequestForQtnSupDtlDto supDto = new ApRequestForQtnSupDtlDto();
        supDto.setTransactionPoid(TEST_TRANSACTION_POID);
        supDto.setDetRowId(TEST_DET_ROW_ID);
        supDto.setSupplierPoid(50L);

        List<ApRequestForQtnSupDtlDto> supList = Collections.singletonList(supDto);

        when(rfqService.getSupplierDetails(
                eq(TEST_TRANSACTION_POID), eq(TEST_GROUP_POID), eq(TEST_COMPANY_POID)))
                .thenReturn(supList);

        // Act & Assert
        mockMvc.perform(get("/api/ap/request-for-quotations/{transactionPoid}/supplier-details", TEST_TRANSACTION_POID)
                        .header("X-Group-Poid", TEST_GROUP_POID)
                        .header("X-Company-Poid", TEST_COMPANY_POID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Supplier details fetched successfully"))
                .andExpect(jsonPath("$.result.data").isArray());

        verify(rfqService, times(1)).getSupplierDetails(
                eq(TEST_TRANSACTION_POID), eq(TEST_GROUP_POID), eq(TEST_COMPANY_POID));
    }

    // ========== Business Logic Operations Tests ==========

    @Test
    void testAddRelatedSuppliers_Success() throws Exception {
        // Arrange
        AddSuppliersResponse response = new AddSuppliersResponse();
        response.setSuccess(true);
        response.setMessage("Suppliers added successfully");
        response.setSuppliersAdded(2);
        response.setAddedSupplierPoidList(List.of(50L, 51L));

        when(rfqService.addRelatedSuppliers(
                eq(TEST_TRANSACTION_POID), eq(TEST_GROUP_POID), eq(TEST_COMPANY_POID), eq(TEST_USER_ID)))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/ap/request-for-quotations/{transactionPoid}/add-suppliers", TEST_TRANSACTION_POID)
                        .header("X-Group-Poid", TEST_GROUP_POID)
                        .header("X-Company-Poid", TEST_COMPANY_POID)
                        .header("X-User-Id", TEST_USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Suppliers added successfully"));

        verify(rfqService, times(1)).addRelatedSuppliers(
                eq(TEST_TRANSACTION_POID), eq(TEST_GROUP_POID), eq(TEST_COMPANY_POID), eq(TEST_USER_ID));
    }

    @Test
    void testSendMailToSuppliers_Success() throws Exception {
        // Arrange
        SendMailResponse response = new SendMailResponse();
        response.setSuccess(true);
        response.setMessage("Mail sent successfully");
        response.setEmailsSent(3);

        when(rfqService.sendMailToSuppliers(
                eq(TEST_TRANSACTION_POID), eq(TEST_GROUP_POID), eq(TEST_COMPANY_POID), eq(TEST_USER_ID)))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/ap/request-for-quotations/{transactionPoid}/send-mail-to-suppliers", TEST_TRANSACTION_POID)
                        .header("X-Group-Poid", TEST_GROUP_POID)
                        .header("X-Company-Poid", TEST_COMPANY_POID)
                        .header("X-User-Id", TEST_USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Mail sent successfully"));

        verify(rfqService, times(1)).sendMailToSuppliers(
                eq(TEST_TRANSACTION_POID), eq(TEST_GROUP_POID), eq(TEST_COMPANY_POID), eq(TEST_USER_ID));
    }

    @Test
    void testCreatePurchaseOrder_Success() throws Exception {
        // Arrange
        CreatePurchaseOrderRequest request = new CreatePurchaseOrderRequest();
        request.setSupplierPoid(50L);

        CreatePurchaseOrderResponse response = new CreatePurchaseOrderResponse();
        response.setSuccess(true);
        response.setMessage("Purchase Order created successfully");

        when(rfqService.createPurchaseOrder(
                eq(TEST_TRANSACTION_POID), eq(50L), eq(TEST_GROUP_POID), eq(TEST_COMPANY_POID), eq(TEST_USER_ID)))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/ap/request-for-quotations/{transactionPoid}/create-purchase-order", TEST_TRANSACTION_POID)
                        .header("X-Group-Poid", TEST_GROUP_POID)
                        .header("X-Company-Poid", TEST_COMPANY_POID)
                        .header("X-User-Id", TEST_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Purchase Order created successfully"));

        verify(rfqService, times(1)).createPurchaseOrder(
                eq(TEST_TRANSACTION_POID), eq(50L), eq(TEST_GROUP_POID), eq(TEST_COMPANY_POID), eq(TEST_USER_ID));
    }

    @Test
    void testUpdateCost_Success() throws Exception {
        // Arrange
        UpdateCostRequest request = new UpdateCostRequest();
        request.setConfirm(true);

        UpdateCostResponse response = new UpdateCostResponse();
        response.setSuccess(true);
        response.setMessage("Cost updated successfully");
        response.setDocumentsUpdated(5);

        when(rfqService.updateCost(
                eq(TEST_TRANSACTION_POID), eq(true), eq(TEST_GROUP_POID), eq(TEST_COMPANY_POID), eq(TEST_USER_ID)))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/ap/request-for-quotations/{transactionPoid}/update-cost", TEST_TRANSACTION_POID)
                        .header("X-Group-Poid", TEST_GROUP_POID)
                        .header("X-Company-Poid", TEST_COMPANY_POID)
                        .header("X-User-Id", TEST_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Cost updated successfully"));

        verify(rfqService, times(1)).updateCost(
                eq(TEST_TRANSACTION_POID), eq(true), eq(TEST_GROUP_POID), eq(TEST_COMPANY_POID), eq(TEST_USER_ID));
    }

    @Test
    void testGetLastPrice_Success() throws Exception {
        // Arrange
        LastPriceResponse response = new LastPriceResponse();
        response.setLastPrice(BigDecimal.valueOf(95.50));

        when(rfqService.getLastPrice(
                eq(100L), eq(1L), eq(50L), eq(TEST_GROUP_POID), eq(TEST_COMPANY_POID), eq(TEST_USER_ID)))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/ap/request-for-quotations/{transactionPoid}/item-details/{detRowId}/last-price",
                        TEST_TRANSACTION_POID, TEST_DET_ROW_ID)
                        .header("X-Group-Poid", TEST_GROUP_POID)
                        .header("X-Company-Poid", TEST_COMPANY_POID)
                        .header("X-User-Id", TEST_USER_ID)
                        .param("stockPoid", "100")
                        .param("stockUnitPoid", "1")
                        .param("supplierPoid", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Last price fetched successfully"));

        verify(rfqService, times(1)).getLastPrice(
                eq(100L), eq(1L), eq(50L), eq(TEST_GROUP_POID), eq(TEST_COMPANY_POID), eq(TEST_USER_ID));
    }

    @Test
    void testGetDefaultStockUnit_Success() throws Exception {
        // Arrange
        DefaultUnitResponse response = new DefaultUnitResponse();
        response.setStockUnitPoid(1L);

        when(rfqService.getDefaultStockUnit(eq(100L))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/ap/request-for-quotations/{transactionPoid}/item-details/{detRowId}/default-unit",
                        TEST_TRANSACTION_POID, TEST_DET_ROW_ID)
                        .param("stockPoid", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Default unit fetched successfully"));

        verify(rfqService, times(1)).getDefaultStockUnit(eq(100L));
    }

    @Test
    void testGetItemsWithoutSuppliers_Success() throws Exception {
        // Arrange
        ItemsWithoutSuppliersResponse response = new ItemsWithoutSuppliersResponse();
        response.setItemsWithoutSuppliers(new ArrayList<>());
        response.setCount(0);

        when(rfqService.getItemsWithoutSuppliers(
                eq(TEST_TRANSACTION_POID), eq(TEST_GROUP_POID), eq(TEST_COMPANY_POID), eq(TEST_USER_ID)))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/ap/request-for-quotations/{transactionPoid}/items-without-suppliers", TEST_TRANSACTION_POID)
                        .header("X-Group-Poid", TEST_GROUP_POID)
                        .header("X-Company-Poid", TEST_COMPANY_POID)
                        .header("X-User-Id", TEST_USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Items without suppliers fetched successfully"));

        verify(rfqService, times(1)).getItemsWithoutSuppliers(
                eq(TEST_TRANSACTION_POID), eq(TEST_GROUP_POID), eq(TEST_COMPANY_POID), eq(TEST_USER_ID));
    }
}

