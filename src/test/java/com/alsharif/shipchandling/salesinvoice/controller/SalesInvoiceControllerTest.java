package com.alsharif.shipchandling.salesinvoice.controller;

import com.alsharif.shipchandling.salesinvoice.dto.*;
import com.alsharif.shipchandling.salesinvoice.dto.request.*;
import com.alsharif.shipchandling.salesinvoice.dto.response.*;
import com.alsharif.shipchandling.salesinvoice.service.SalesInvoiceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SalesInvoiceController.class)
class SalesInvoiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SalesInvoiceService invoiceService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final Long TEST_GROUP_POID = 1L;
    private static final Long TEST_COMPANY_POID = 2L;
    private static final Long TEST_TRANSACTION_POID = 100L;
    private static final String TEST_USER_ID = "testUser";
    private static final Long TEST_DET_ROW_ID = 1L;
    private static final Long TEST_CUSTOMER_POID = 50L;

    @BeforeEach
    void setUp() {
        // Common setup if needed
    }

    // ========== Basic CRUD Operations Tests ==========

    @Test
    void testCreateSalesInvoice_Success() throws Exception {
        // Arrange
        CreateSalesInvoiceRequest request = new CreateSalesInvoiceRequest();
        request.setTransactionDate(Timestamp.from(Instant.now()));
        request.setPartyType("CUSTOMER");
        request.setCustomerPoid(TEST_CUSTOMER_POID);

        SalesInvoiceHdrDto responseDto = new SalesInvoiceHdrDto();
        responseDto.setTransactionPoid(TEST_TRANSACTION_POID);
        responseDto.setDocRef("INV-001");
        responseDto.setPartyType("CUSTOMER");

        when(invoiceService.createSalesInvoice(any(CreateSalesInvoiceRequest.class),
                eq(TEST_GROUP_POID), eq(TEST_COMPANY_POID), eq(TEST_USER_ID)))
                .thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(post("/sales-invoice-sch")
                        .header("X-Group-Poid", TEST_GROUP_POID)
                        .header("X-Company-Poid", TEST_COMPANY_POID)
                        .header("X-User-Id", TEST_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Sales invoice created successfully"))
                .andExpect(jsonPath("$.result.data.transactionPoid").value(TEST_TRANSACTION_POID));

        verify(invoiceService, times(1)).createSalesInvoice(
                any(CreateSalesInvoiceRequest.class), eq(TEST_GROUP_POID), eq(TEST_COMPANY_POID), eq(TEST_USER_ID));
    }

    @Test
    void testCreateSalesInvoice_MissingHeaders() throws Exception {
        // Arrange
        CreateSalesInvoiceRequest request = new CreateSalesInvoiceRequest();
        request.setTransactionDate(Timestamp.from(Instant.now()));
        request.setPartyType("CUSTOMER");

        // Act & Assert - Missing Group POID
        mockMvc.perform(post("/sales-invoice-sch")
                        .header("X-Company-Poid", TEST_COMPANY_POID)
                        .header("X-User-Id", TEST_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetSalesInvoiceByPoid_Success() throws Exception {
        // Arrange
        SalesInvoiceHdrDto responseDto = new SalesInvoiceHdrDto();
        responseDto.setTransactionPoid(TEST_TRANSACTION_POID);
        responseDto.setDocRef("INV-001");
        responseDto.setPartyType("CUSTOMER");

        when(invoiceService.getSalesInvoiceByPoid(
                eq(TEST_TRANSACTION_POID), eq(TEST_GROUP_POID), eq(TEST_COMPANY_POID), eq(false)))
                .thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(get("/sales-invoice-sch/{transactionPoid}", TEST_TRANSACTION_POID)
                        .header("X-Group-Poid", TEST_GROUP_POID)
                        .header("X-Company-Poid", TEST_COMPANY_POID)
                        .param("includeDetails", "false")
                        .param("documentId", "INV")
                        .param("actionRequested", "VIEW"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Sales invoice fetched successfully"))
                .andExpect(jsonPath("$.result.data.transactionPoid").value(TEST_TRANSACTION_POID));

        verify(invoiceService, times(1)).getSalesInvoiceByPoid(
                eq(TEST_TRANSACTION_POID), eq(TEST_GROUP_POID), eq(TEST_COMPANY_POID), eq(false));
    }

    @Test
    void testGetSalesInvoiceByPoid_NotFound() throws Exception {
        // Arrange
        when(invoiceService.getSalesInvoiceByPoid(
                eq(TEST_TRANSACTION_POID), eq(TEST_GROUP_POID), eq(TEST_COMPANY_POID), anyBoolean()))
                .thenThrow(new com.alsharif.shipchandling.exceptions.ResourceNotFoundException("Sales Invoice", "transactionPoid", TEST_TRANSACTION_POID));

        // Act & Assert
        mockMvc.perform(get("/sales-invoice-sch/{transactionPoid}", TEST_TRANSACTION_POID)
                        .header("X-Group-Poid", TEST_GROUP_POID)
                        .header("X-Company-Poid", TEST_COMPANY_POID)
                        .param("documentId", "INV")
                        .param("actionRequested", "VIEW"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateSalesInvoice_Success() throws Exception {
        // Arrange
        UpdateSalesInvoiceRequest request = new UpdateSalesInvoiceRequest();
        request.setTransactionDate(Timestamp.from(Instant.now()));
        request.setPartyType("CUSTOMER");
        request.setCustomerPoid(TEST_CUSTOMER_POID);

        SalesInvoiceHdrDto responseDto = new SalesInvoiceHdrDto();
        responseDto.setTransactionPoid(TEST_TRANSACTION_POID);
        responseDto.setPartyType("CUSTOMER");

        when(invoiceService.updateSalesInvoice(
                eq(TEST_TRANSACTION_POID), any(UpdateSalesInvoiceRequest.class),
                eq(TEST_GROUP_POID), eq(TEST_COMPANY_POID), eq(TEST_USER_ID)))
                .thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(put("/sales-invoice-sch/{transactionPoid}", TEST_TRANSACTION_POID)
                        .header("X-Group-Poid", TEST_GROUP_POID)
                        .header("X-Company-Poid", TEST_COMPANY_POID)
                        .header("X-User-Id", TEST_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Sales invoice updated successfully"));

        verify(invoiceService, times(1)).updateSalesInvoice(
                eq(TEST_TRANSACTION_POID), any(UpdateSalesInvoiceRequest.class),
                eq(TEST_GROUP_POID), eq(TEST_COMPANY_POID), eq(TEST_USER_ID));
    }

    @Test
    void testUpdateSalesInvoice_VerifiedStatus() throws Exception {
        // Arrange
        UpdateSalesInvoiceRequest request = new UpdateSalesInvoiceRequest();
        request.setTransactionDate(Timestamp.from(Instant.now()));

        when(invoiceService.updateSalesInvoice(
                eq(TEST_TRANSACTION_POID), any(UpdateSalesInvoiceRequest.class),
                eq(TEST_GROUP_POID), eq(TEST_COMPANY_POID), eq(TEST_USER_ID)))
                .thenThrow(new com.alsharif.shipchandling.exceptions.CustomException("Cannot update verified invoice. Invoice must be unverified first."));

        // Act & Assert
        mockMvc.perform(put("/sales-invoice-sch/{transactionPoid}", TEST_TRANSACTION_POID)
                        .header("X-Group-Poid", TEST_GROUP_POID)
                        .header("X-Company-Poid", TEST_COMPANY_POID)
                        .header("X-User-Id", TEST_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void testDeleteSalesInvoice_Success() throws Exception {
        // Arrange
        doNothing().when(invoiceService).deleteSalesInvoice(
                eq(TEST_TRANSACTION_POID), eq(TEST_GROUP_POID), eq(TEST_COMPANY_POID));

        // Act & Assert
        mockMvc.perform(delete("/sales-invoice-sch/{transactionPoid}", TEST_TRANSACTION_POID)
                        .header("X-Group-Poid", TEST_GROUP_POID)
                        .header("X-Company-Poid", TEST_COMPANY_POID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Sales invoice deleted successfully"));

        verify(invoiceService, times(1)).deleteSalesInvoice(
                eq(TEST_TRANSACTION_POID), eq(TEST_GROUP_POID), eq(TEST_COMPANY_POID));
    }

    @Test
    void testGetAllSalesInvoices_Success() throws Exception {
        // Arrange
        SalesInvoiceHdrDto dto = new SalesInvoiceHdrDto();
        dto.setTransactionPoid(TEST_TRANSACTION_POID);
        dto.setDocRef("INV-001");

        PaginatedResponse<SalesInvoiceHdrDto> paginatedResponse = new PaginatedResponse<>();
        paginatedResponse.setData(Collections.singletonList(dto));
        paginatedResponse.setPage(0);
        paginatedResponse.setSize(10);
        paginatedResponse.setTotalElements(1L);
        paginatedResponse.setTotalPages(1);
        paginatedResponse.setFirst(true);
        paginatedResponse.setLast(true);

        when(invoiceService.getAllSalesInvoices(
                eq(TEST_GROUP_POID), eq(TEST_COMPANY_POID), isNull(), isNull(),
                isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), eq(0), eq(10)))
                .thenReturn(paginatedResponse);

        // Act & Assert
        mockMvc.perform(get("/sales-invoice-sch")
                        .header("X-Group-Poid", TEST_GROUP_POID)
                        .header("X-Company-Poid", TEST_COMPANY_POID)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Sales invoices fetched successfully"))
                .andExpect(jsonPath("$.result.data.data").isArray())
                .andExpect(jsonPath("$.result.data.totalElements").value(1));

        verify(invoiceService, times(1)).getAllSalesInvoices(
                eq(TEST_GROUP_POID), eq(TEST_COMPANY_POID), isNull(), isNull(),
                isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), eq(0), eq(10));
    }

    @Test
    void testGetAllSalesInvoices_WithFilters() throws Exception {
        // Arrange
        SalesInvoiceHdrDto dto = new SalesInvoiceHdrDto();
        dto.setTransactionPoid(TEST_TRANSACTION_POID);

        PaginatedResponse<SalesInvoiceHdrDto> paginatedResponse = new PaginatedResponse<>();
        paginatedResponse.setData(Collections.singletonList(dto));
        paginatedResponse.setTotalElements(1L);

        OffsetDateTime fromDate = OffsetDateTime.now().minusDays(30);
        OffsetDateTime toDate = OffsetDateTime.now();
        Timestamp fromTs = Timestamp.from(fromDate.toInstant());
        Timestamp toTs = Timestamp.from(toDate.toInstant());

        when(invoiceService.getAllSalesInvoices(
                eq(TEST_GROUP_POID), eq(TEST_COMPANY_POID), eq("IN_PROGRESS"), eq("N"),
                eq(TEST_CUSTOMER_POID), isNull(), isNull(), eq("test"), eq(fromTs), eq(toTs), eq(0), eq(20)))
                .thenReturn(paginatedResponse);

        // Act & Assert
        mockMvc.perform(get("/sales-invoice-sch")
                        .header("X-Group-Poid", TEST_GROUP_POID)
                        .header("X-Company-Poid", TEST_COMPANY_POID)
                        .param("invStatus", "IN_PROGRESS")
                        .param("verified", "N")
                        .param("customerPoid", String.valueOf(TEST_CUSTOMER_POID))
                        .param("search", "test")
                        .param("fromDate", fromDate.toString())
                        .param("toDate", toDate.toString())
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(invoiceService, times(1)).getAllSalesInvoices(
                eq(TEST_GROUP_POID), eq(TEST_COMPANY_POID), eq("IN_PROGRESS"), eq("N"),
                eq(TEST_CUSTOMER_POID), isNull(), isNull(), eq("test"), eq(fromTs), eq(toTs), eq(0), eq(20));
    }

    // ========== Invoice Details (Item Details) Tests ==========

    @Test
    void testAddItemDetail_Success() throws Exception {
        // Arrange
        CreateSalesInvoiceDtlRequest request = new CreateSalesInvoiceDtlRequest();
        request.setStockPoid(100L);
        request.setStockUnitPoid(1L);
        request.setQuantity(10L);
        request.setPrice(BigDecimal.valueOf(100));
        request.setDiscount(50L);

        SalesInvoiceDtlDto responseDto = new SalesInvoiceDtlDto();
        responseDto.setTransactionPoid(TEST_TRANSACTION_POID);
        responseDto.setDetRowId(TEST_DET_ROW_ID);
        responseDto.setStockPoid(100L);

        when(invoiceService.addInvoiceDetail(
                eq(TEST_TRANSACTION_POID), any(CreateSalesInvoiceDtlRequest.class),
                eq(TEST_GROUP_POID), eq(TEST_COMPANY_POID), eq(TEST_USER_ID)))
                .thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(post("/sales-invoice-sch/{transactionPoid}/item-details", TEST_TRANSACTION_POID)
                        .header("X-Group-Poid", TEST_GROUP_POID)
                        .header("X-Company-Poid", TEST_COMPANY_POID)
                        .header("X-User-Id", TEST_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Item detail added successfully"));

        verify(invoiceService, times(1)).addInvoiceDetail(
                eq(TEST_TRANSACTION_POID), any(CreateSalesInvoiceDtlRequest.class),
                eq(TEST_GROUP_POID), eq(TEST_COMPANY_POID), eq(TEST_USER_ID));
    }

    @Test
    void testUpdateItemDetail_Success() throws Exception {
        // Arrange
        UpdateSalesInvoiceDtlRequest request = new UpdateSalesInvoiceDtlRequest();
        request.setStockPoid(100L);
        request.setQuantity(20L);
        request.setPrice(BigDecimal.valueOf(150));

        SalesInvoiceDtlDto responseDto = new SalesInvoiceDtlDto();
        responseDto.setTransactionPoid(TEST_TRANSACTION_POID);
        responseDto.setDetRowId(TEST_DET_ROW_ID);

        when(invoiceService.updateInvoiceDetail(
                eq(TEST_TRANSACTION_POID), eq(TEST_DET_ROW_ID), any(UpdateSalesInvoiceDtlRequest.class),
                eq(TEST_GROUP_POID), eq(TEST_COMPANY_POID), eq(TEST_USER_ID)))
                .thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(put("/sales-invoice-sch/{transactionPoid}/item-details/{detRowId}",
                        TEST_TRANSACTION_POID, TEST_DET_ROW_ID)
                        .header("X-Group-Poid", TEST_GROUP_POID)
                        .header("X-Company-Poid", TEST_COMPANY_POID)
                        .header("X-User-Id", TEST_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Item detail updated successfully"));

        verify(invoiceService, times(1)).updateInvoiceDetail(
                eq(TEST_TRANSACTION_POID), eq(TEST_DET_ROW_ID), any(UpdateSalesInvoiceDtlRequest.class),
                eq(TEST_GROUP_POID), eq(TEST_COMPANY_POID), eq(TEST_USER_ID));
    }

    @Test
    void testDeleteItemDetail_Success() throws Exception {
        // Arrange
        doNothing().when(invoiceService).deleteInvoiceDetail(
                eq(TEST_TRANSACTION_POID), eq(TEST_DET_ROW_ID), eq(TEST_GROUP_POID), eq(TEST_COMPANY_POID));

        // Act & Assert
        mockMvc.perform(delete("/sales-invoice-sch/{transactionPoid}/item-details/{detRowId}",
                        TEST_TRANSACTION_POID, TEST_DET_ROW_ID)
                        .header("X-Group-Poid", TEST_GROUP_POID)
                        .header("X-Company-Poid", TEST_COMPANY_POID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Item detail deleted successfully"));

        verify(invoiceService, times(1)).deleteInvoiceDetail(
                eq(TEST_TRANSACTION_POID), eq(TEST_DET_ROW_ID), eq(TEST_GROUP_POID), eq(TEST_COMPANY_POID));
    }

    @Test
    void testGetItemDetails_Success() throws Exception {
        // Arrange
        SalesInvoiceDtlDto itemDto = new SalesInvoiceDtlDto();
        itemDto.setTransactionPoid(TEST_TRANSACTION_POID);
        itemDto.setDetRowId(TEST_DET_ROW_ID);

        List<SalesInvoiceDtlDto> itemList = Collections.singletonList(itemDto);

        when(invoiceService.getInvoiceDetails(
                eq(TEST_TRANSACTION_POID), eq(TEST_GROUP_POID), eq(TEST_COMPANY_POID)))
                .thenReturn(itemList);

        // Act & Assert
        mockMvc.perform(get("/sales-invoice-sch/{transactionPoid}/item-details", TEST_TRANSACTION_POID)
                        .header("X-Group-Poid", TEST_GROUP_POID)
                        .header("X-Company-Poid", TEST_COMPANY_POID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Item details fetched successfully"))
                .andExpect(jsonPath("$.result.data").isArray());

        verify(invoiceService, times(1)).getInvoiceDetails(
                eq(TEST_TRANSACTION_POID), eq(TEST_GROUP_POID), eq(TEST_COMPANY_POID));
    }

    // ========== Delivery Note Details Tests ==========

    @Test
    void testAddDeliveryNoteDetail_Success() throws Exception {
        // Arrange
        CreateSalesDnDtlRequest request = new CreateSalesDnDtlRequest();
        request.setDnPoidFk(200L);
        request.setQuotationPoidFk(300L);

        SalesDnDtlDto responseDto = new SalesDnDtlDto();
        responseDto.setTransactionPoid(TEST_TRANSACTION_POID);
        responseDto.setDetRowId(TEST_DET_ROW_ID);

        when(invoiceService.addDeliveryNoteDetail(
                eq(TEST_TRANSACTION_POID), any(CreateSalesDnDtlRequest.class),
                eq(TEST_GROUP_POID), eq(TEST_COMPANY_POID), eq(TEST_USER_ID)))
                .thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(post("/sales-invoice-sch/{transactionPoid}/delivery-note-details", TEST_TRANSACTION_POID)
                        .header("X-Group-Poid", TEST_GROUP_POID)
                        .header("X-Company-Poid", TEST_COMPANY_POID)
                        .header("X-User-Id", TEST_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Delivery note detail added successfully"));

        verify(invoiceService, times(1)).addDeliveryNoteDetail(
                eq(TEST_TRANSACTION_POID), any(CreateSalesDnDtlRequest.class),
                eq(TEST_GROUP_POID), eq(TEST_COMPANY_POID), eq(TEST_USER_ID));
    }

    @Test
    void testGetDeliveryNoteDetails_Success() throws Exception {
        // Arrange
        SalesDnDtlDto dnDto = new SalesDnDtlDto();
        dnDto.setTransactionPoid(TEST_TRANSACTION_POID);
        dnDto.setDetRowId(TEST_DET_ROW_ID);

        List<SalesDnDtlDto> dnList = Collections.singletonList(dnDto);

        when(invoiceService.getDeliveryNoteDetails(
                eq(TEST_TRANSACTION_POID), eq(TEST_GROUP_POID), eq(TEST_COMPANY_POID)))
                .thenReturn(dnList);

        // Act & Assert
        mockMvc.perform(get("/sales-invoice-sch/{transactionPoid}/delivery-note-details", TEST_TRANSACTION_POID)
                        .header("X-Group-Poid", TEST_GROUP_POID)
                        .header("X-Company-Poid", TEST_COMPANY_POID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Delivery note details fetched successfully"))
                .andExpect(jsonPath("$.result.data").isArray());

        verify(invoiceService, times(1)).getDeliveryNoteDetails(
                eq(TEST_TRANSACTION_POID), eq(TEST_GROUP_POID), eq(TEST_COMPANY_POID));
    }

    // ========== Business Logic Tests ==========

    @Test
    void testCalculateGp_Success() throws Exception {
        // Arrange
        CalculateGpResponse response = new CalculateGpResponse();
        response.setSuccess(true);
        response.setMessage("GP calculated successfully");
        response.setTotalGpAmt(1000L);
        response.setTotalGpPercent(15L);

        when(invoiceService.calculateGp(
                eq(TEST_TRANSACTION_POID), eq(TEST_GROUP_POID), eq(TEST_COMPANY_POID), eq("")))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/sales-invoice-sch/{transactionPoid}/refresh-gp", TEST_TRANSACTION_POID)
                        .header("X-Group-Poid", TEST_GROUP_POID)
                        .header("X-Company-Poid", TEST_COMPANY_POID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("GP calculated successfully"));

        verify(invoiceService, times(1)).calculateGp(
                eq(TEST_TRANSACTION_POID), eq(TEST_GROUP_POID), eq(TEST_COMPANY_POID), eq(""));
    }

    @Test
    void testCalculateDueDate_Success() throws Exception {
        // Arrange
        Timestamp transactionDate = Timestamp.from(Instant.now());
        Long creditDays = 30L;

        CalculateDueDateResponse response = new CalculateDueDateResponse();
        response.setSuccess(true);
        response.setDueDate(Timestamp.from(Instant.now().plusSeconds(2592000)));

        when(invoiceService.calculateDueDate(
                eq(TEST_TRANSACTION_POID), any(Timestamp.class), eq(creditDays),
                eq(TEST_GROUP_POID), eq(TEST_COMPANY_POID)))
                .thenReturn(response);

        // Act & Assert
        // Format timestamp in a format Spring can parse (yyyy-MM-dd HH:mm:ss)
        java.time.LocalDateTime localDateTime = transactionDate.toLocalDateTime();
        String timestampStr = localDateTime.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        
        mockMvc.perform(get("/sales-invoice-sch/{transactionPoid}/calculate-due-date", TEST_TRANSACTION_POID)
                        .header("X-Group-Poid", TEST_GROUP_POID)
                        .header("X-Company-Poid", TEST_COMPANY_POID)
                        .param("transactionDate", timestampStr)
                        .param("creditDays", String.valueOf(creditDays)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Due date calculated successfully"));

        verify(invoiceService, times(1)).calculateDueDate(
                eq(TEST_TRANSACTION_POID), any(Timestamp.class), eq(creditDays),
                eq(TEST_GROUP_POID), eq(TEST_COMPANY_POID));
    }

    @Test
    void testLoadQuotationItems_Success() throws Exception {
        // Arrange
        LoadQuotationItemsRequest request = new LoadQuotationItemsRequest();
        request.setQtnPoid("QTN-001");
        request.setIncentiveAmt(100L);

        LoadQuotationItemsResponse response = new LoadQuotationItemsResponse();
        response.setSuccess(true);
        response.setMessage("Quotation items loaded successfully");
        response.setItems(Collections.emptyList());

        SalesInvoiceHdrDto invoiceDto = new SalesInvoiceHdrDto();
        invoiceDto.setTransactionPoid(TEST_TRANSACTION_POID);
        response.setInvoice(invoiceDto);

        when(invoiceService.loadQuotationItems(
                eq(TEST_TRANSACTION_POID), any(LoadQuotationItemsRequest.class),
                eq(TEST_GROUP_POID), eq(TEST_COMPANY_POID), eq(TEST_USER_ID)))
                .thenReturn(response);
        when(invoiceService.getSalesInvoiceByPoid(
                eq(TEST_TRANSACTION_POID), eq(TEST_GROUP_POID), eq(TEST_COMPANY_POID), eq(true)))
                .thenReturn(invoiceDto);

        // Act & Assert
        mockMvc.perform(post("/sales-invoice-sch/{transactionPoid}/load-quotation", TEST_TRANSACTION_POID)
                        .header("X-Group-Poid", TEST_GROUP_POID)
                        .header("X-Company-Poid", TEST_COMPANY_POID)
                        .header("X-User-Id", TEST_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Quotation items loaded successfully"));

        verify(invoiceService, times(1)).loadQuotationItems(
                eq(TEST_TRANSACTION_POID), any(LoadQuotationItemsRequest.class),
                eq(TEST_GROUP_POID), eq(TEST_COMPANY_POID), eq(TEST_USER_ID));
    }

    @Test
    void testVerifyInvoice_Success() throws Exception {
        // Arrange
        VerifyInvoiceResponse response = new VerifyInvoiceResponse();
        response.setSuccess(true);
        response.setMessage("Invoice verified successfully");
        response.setVerified("Y");

        when(invoiceService.verifyInvoice(
                eq(TEST_TRANSACTION_POID), eq(TEST_GROUP_POID), eq(TEST_COMPANY_POID), eq(TEST_USER_ID)))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/sales-invoice-sch/{transactionPoid}/verify", TEST_TRANSACTION_POID)
                        .header("X-Group-Poid", TEST_GROUP_POID)
                        .header("X-Company-Poid", TEST_COMPANY_POID)
                        .header("X-User-Id", TEST_USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Invoice verified successfully"));

        verify(invoiceService, times(1)).verifyInvoice(
                eq(TEST_TRANSACTION_POID), eq(TEST_GROUP_POID), eq(TEST_COMPANY_POID), eq(TEST_USER_ID));
    }

    @Test
    void testValidateCustomer_Success() throws Exception {
        // Arrange
        ValidationResponse response = new ValidationResponse();
        response.setSuccess(true);
        response.setMessage("Customer validated successfully");

        when(invoiceService.validateCustomer(
                eq(TEST_CUSTOMER_POID), eq(TEST_GROUP_POID), eq(TEST_COMPANY_POID)))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/sales-invoice-sch/validate-customer")
                        .header("X-Group-Poid", TEST_GROUP_POID)
                        .header("X-Company-Poid", TEST_COMPANY_POID)
                        .param("customerPoid", String.valueOf(TEST_CUSTOMER_POID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Customer validation completed"));

        verify(invoiceService, times(1)).validateCustomer(
                eq(TEST_CUSTOMER_POID), eq(TEST_GROUP_POID), eq(TEST_COMPANY_POID));
    }

    @Test
    void testLoadCreditDetails_Success() throws Exception {
        // Arrange
        CreditDetailsRequest request = new CreditDetailsRequest();
        request.setDocId("DOC-001");

        CreditDetailsResponse response = new CreditDetailsResponse();
        response.setSuccess(true);
        response.setMessage("Credit details loaded successfully");
        response.setCreditDetails(Collections.emptyList());

        when(invoiceService.loadCreditDetails(
                eq(TEST_CUSTOMER_POID), eq(TEST_GROUP_POID), eq(TEST_COMPANY_POID), any(CreditDetailsRequest.class)))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/sales-invoice-sch/load-credit-details")
                        .header("X-Group-Poid", TEST_GROUP_POID)
                        .header("X-Company-Poid", TEST_COMPANY_POID)
                        .param("customerPoid", String.valueOf(TEST_CUSTOMER_POID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Credit details loaded successfully"));

        verify(invoiceService, times(1)).loadCreditDetails(
                eq(TEST_CUSTOMER_POID), eq(TEST_GROUP_POID), eq(TEST_COMPANY_POID), any(CreditDetailsRequest.class));
    }

    @Test
    void testCheckSalesInvoiceDependencies_Success() throws Exception {
        // Arrange
        SalesInvoiceDependenciesDto dto = new SalesInvoiceDependenciesDto();
        dto.setTransactionPoid(TEST_TRANSACTION_POID);
        dto.setCanDelete(true);
        dto.setMessage("Sales invoice can be deleted. No dependencies found.");

        when(invoiceService.checkSalesInvoiceDependencies(
                eq(TEST_TRANSACTION_POID), eq(TEST_GROUP_POID), eq(TEST_COMPANY_POID)))
                .thenReturn(dto);

        // Act & Assert
        mockMvc.perform(get("/sales-invoice-sch/{transactionPoid}/dependencies", TEST_TRANSACTION_POID)
                        .header("X-Group-Poid", TEST_GROUP_POID)
                        .header("X-Company-Poid", TEST_COMPANY_POID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Dependency check completed"));

        verify(invoiceService, times(1)).checkSalesInvoiceDependencies(
                eq(TEST_TRANSACTION_POID), eq(TEST_GROUP_POID), eq(TEST_COMPANY_POID));
    }

    @Test
    void testGetCostBookedDetails_Success() throws Exception {
        // Arrange
        SalesInvCostbkdDtlDto costDto = new SalesInvCostbkdDtlDto();
        costDto.setTransactionPoid(TEST_TRANSACTION_POID);

        List<SalesInvCostbkdDtlDto> costList = Collections.singletonList(costDto);

        when(invoiceService.getCostBookedDetails(
                eq(TEST_TRANSACTION_POID), eq(TEST_GROUP_POID), eq(TEST_COMPANY_POID)))
                .thenReturn(costList);

        // Act & Assert
        mockMvc.perform(get("/sales-invoice-sch/{transactionPoid}/cost-booked-details", TEST_TRANSACTION_POID)
                        .header("X-Group-Poid", TEST_GROUP_POID)
                        .header("X-Company-Poid", TEST_COMPANY_POID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Cost booked details fetched successfully"))
                .andExpect(jsonPath("$.result.data").isArray());

        verify(invoiceService, times(1)).getCostBookedDetails(
                eq(TEST_TRANSACTION_POID), eq(TEST_GROUP_POID), eq(TEST_COMPANY_POID));
    }
}

