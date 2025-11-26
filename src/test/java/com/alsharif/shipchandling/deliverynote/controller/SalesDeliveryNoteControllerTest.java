package com.alsharif.shipchandling.deliverynote.controller;

import com.alsharif.shipchandling.deliverynote.dto.*;
import com.alsharif.shipchandling.deliverynote.service.SalesDeliveryNoteService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SalesDeliveryNoteController.class)
class SalesDeliveryNoteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SalesDeliveryNoteService service;

    @Autowired
    private ObjectMapper objectMapper;

    private static final Long TEST_TRANSACTION_POID = 100L;
    private static final Long TEST_COMPANY_POID = 2L;
    private static final Long TEST_GROUP_POID = 1L;
    private static final String TEST_USER_ID = "testUser";
    private static final Long TEST_DET_ROW_ID = 1L;
    private static final Long TEST_CUSTOMER_POID = 50L;

    @BeforeEach
    void setUp() {
        // Common setup if needed
    }

    // ========== CRUD Operations Tests ==========

    @Test
    void testCreateDeliveryNote_Success() throws Exception {
        // Arrange
        CreateSalesDeliveryNoteRequest request = createTestRequest();
        SalesDeliveryNoteHdrDto responseDto = createTestDeliveryNoteDto();

        when(service.createDeliveryNote(any(CreateSalesDeliveryNoteRequest.class), eq(TEST_GROUP_POID), eq(TEST_COMPANY_POID), eq(TEST_USER_ID)))
                .thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(post("/deliverynote")
                        .header("X-Company-Poid", TEST_COMPANY_POID)
                        .header("X-Group-Poid", TEST_GROUP_POID)
                        .header("X-User-Id", TEST_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.transactionPoid").value(TEST_TRANSACTION_POID));

        verify(service, times(1)).createDeliveryNote(any(CreateSalesDeliveryNoteRequest.class), eq(TEST_GROUP_POID), eq(TEST_COMPANY_POID), eq(TEST_USER_ID));
    }

    @Test
    void testGetDeliveryNoteByPoid_Success() throws Exception {
        // Arrange
        SalesDeliveryNoteHdrDto responseDto = createTestDeliveryNoteDto();

        when(service.getDeliveryNoteByPoid(eq(TEST_GROUP_POID), eq(TEST_TRANSACTION_POID), eq(TEST_COMPANY_POID), anyBoolean()))
                .thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(get("/deliverynote/{transactionPoid}", TEST_TRANSACTION_POID)
                        .header("X-Group-Poid", TEST_GROUP_POID)
                        .header("X-Company-Poid", TEST_COMPANY_POID)
                        .param("documentId", "DN")
                        .param("actionRequested", "VIEW")
                        .param("includeDetails", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.transactionPoid").value(TEST_TRANSACTION_POID));

        verify(service, times(1)).getDeliveryNoteByPoid(eq(TEST_GROUP_POID), eq(TEST_TRANSACTION_POID), eq(TEST_COMPANY_POID), eq(true));
    }

    @Test
    void testUpdateDeliveryNote_Success() throws Exception {
        // Arrange
        CreateSalesDeliveryNoteRequest request = createTestRequest();
        SalesDeliveryNoteHdrDto responseDto = createTestDeliveryNoteDto();

        when(service.updateDeliveryNote(eq(TEST_GROUP_POID), eq(TEST_TRANSACTION_POID), any(CreateSalesDeliveryNoteRequest.class), eq(TEST_COMPANY_POID), eq(TEST_USER_ID)))
                .thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(put("/deliverynote/{transactionPoid}", TEST_TRANSACTION_POID)
                        .header("X-Company-Poid", TEST_COMPANY_POID)
                        .header("X-Group-Poid", TEST_GROUP_POID)
                        .header("X-User-Id", TEST_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.transactionPoid").value(TEST_TRANSACTION_POID));

        verify(service, times(1)).updateDeliveryNote(eq(TEST_GROUP_POID), eq(TEST_TRANSACTION_POID), any(CreateSalesDeliveryNoteRequest.class), eq(TEST_COMPANY_POID), eq(TEST_USER_ID));
    }

    @Test
    void testDeleteDeliveryNote_Success() throws Exception {
        // Arrange
        doNothing().when(service).deleteDeliveryNote(eq(TEST_GROUP_POID), eq(TEST_TRANSACTION_POID), eq(TEST_COMPANY_POID));

        // Act & Assert
        mockMvc.perform(delete("/deliverynote/{transactionPoid}", TEST_TRANSACTION_POID)
                        .header("X-Group-Poid", TEST_GROUP_POID)
                        .header("X-Company-Poid", TEST_COMPANY_POID))
                .andExpect(status().isOk());

        verify(service, times(1)).deleteDeliveryNote(eq(TEST_GROUP_POID), eq(TEST_TRANSACTION_POID), eq(TEST_COMPANY_POID));
    }

    @Test
    void testGetAllDeliveryNotes_Success() throws Exception {
        // Arrange
        PaginatedResponse<SalesDeliveryNoteHdrDto> response = new PaginatedResponse<>();
        response.setData(Collections.singletonList(createTestDeliveryNoteDto()));
        response.setTotalElements(1L);
        response.setPage(0);
        response.setSize(10);

        when(service.getAllDeliveryNotes(eq(TEST_GROUP_POID), eq(TEST_COMPANY_POID), any(), any(), any(), any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/deliverynote")
                        .header("X-Group-Poid", TEST_GROUP_POID)
                        .header("X-Company-Poid", TEST_COMPANY_POID)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1));

        verify(service, times(1)).getAllDeliveryNotes(eq(TEST_GROUP_POID), eq(TEST_COMPANY_POID), any(), any(), any(), any(), any(), any(), any(), eq(0), eq(10));
    }

    @Test
    void testGetAllDeliveryNotes_WithFilters() throws Exception {
        // Arrange
        PaginatedResponse<SalesDeliveryNoteHdrDto> response = new PaginatedResponse<>();
        response.setData(Collections.singletonList(createTestDeliveryNoteDto()));
        response.setTotalElements(1L);

        when(service.getAllDeliveryNotes(eq(TEST_GROUP_POID), eq(TEST_COMPANY_POID), eq("PROCESSING"), eq(TEST_CUSTOMER_POID), any(), any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/deliverynote")
                        .header("X-Group-Poid", TEST_GROUP_POID)
                        .header("X-Company-Poid", TEST_COMPANY_POID)
                        .param("deliveryStatus", "PROCESSING")
                        .param("customerPoid", String.valueOf(TEST_CUSTOMER_POID)))
                .andExpect(status().isOk());

        verify(service, times(1)).getAllDeliveryNotes(eq(TEST_GROUP_POID), eq(TEST_COMPANY_POID), eq("PROCESSING"), eq(TEST_CUSTOMER_POID), any(), any(), any(), any(), any(), anyInt(), anyInt());
    }

    // ========== Validation Tests ==========

    @Test
    void testValidateDocRef_Success() throws Exception {
        // Arrange
        ValidationResponse response = new ValidationResponse();
        response.setIsUnique(true);
        response.setMessage("Document reference is available");

        when(service.validateDocRef(eq("DN-001"), any()))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/deliverynote/validate-doc-ref")
                        .header("X-Group-Poid", TEST_GROUP_POID)
                        .param("docRef", "DN-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isUnique").value(true));

        verify(service, times(1)).validateDocRef(eq("DN-001"), any());
    }

    @Test
    void testValidateDocRef_WithTransactionPoid() throws Exception {
        // Arrange
        ValidationResponse response = new ValidationResponse();
        response.setIsUnique(true);

        when(service.validateDocRef(eq("DN-001"), eq(TEST_TRANSACTION_POID)))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/deliverynote/validate-doc-ref")
                        .header("X-Group-Poid", TEST_GROUP_POID)
                        .param("docRef", "DN-001")
                        .param("transactionPoid", String.valueOf(TEST_TRANSACTION_POID)))
                .andExpect(status().isOk());

        verify(service, times(1)).validateDocRef(eq("DN-001"), eq(TEST_TRANSACTION_POID));
    }

    // ========== Item Details Tests ==========

    @Test
    void testAddItemDetail_Success() throws Exception {
        // Arrange
        CreateSalesDeliveryNoteItemDtlRequest request = new CreateSalesDeliveryNoteItemDtlRequest();
        request.setStockPoid(10L);
        request.setQuantity(10L);
        request.setPrice(100L);
        request.setAmount(1000L);

        SalesDeliveryNoteItemDtlDto responseDto = createTestItemDetailDto();

        when(service.addItemDetail(eq(TEST_TRANSACTION_POID), any(CreateSalesDeliveryNoteItemDtlRequest.class), eq(TEST_COMPANY_POID), eq(TEST_USER_ID)))
                .thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(post("/deliverynote/{transactionPoid}/item-details", TEST_TRANSACTION_POID)
                        .header("X-Group-Poid", TEST_GROUP_POID)
                        .header("X-Company-Poid", TEST_COMPANY_POID)
                        .header("X-User-Id", TEST_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.transactionPoid").value(TEST_TRANSACTION_POID));

        verify(service, times(1)).addItemDetail(eq(TEST_TRANSACTION_POID), any(CreateSalesDeliveryNoteItemDtlRequest.class), eq(TEST_COMPANY_POID), eq(TEST_USER_ID));
    }

    @Test
    void testUpdateItemDetail_Success() throws Exception {
        // Arrange
        CreateSalesDeliveryNoteItemDtlRequest request = new CreateSalesDeliveryNoteItemDtlRequest();
        request.setPrice(150L);
        request.setAmount(1500L);

        SalesDeliveryNoteItemDtlDto responseDto = createTestItemDetailDto();

        when(service.updateItemDetail(eq(TEST_TRANSACTION_POID), eq(TEST_DET_ROW_ID), any(CreateSalesDeliveryNoteItemDtlRequest.class), eq(TEST_COMPANY_POID), eq(TEST_USER_ID)))
                .thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(put("/deliverynote/{transactionPoid}/item-details/{detRowId}", TEST_TRANSACTION_POID, TEST_DET_ROW_ID)
                        .header("X-Group-Poid", TEST_GROUP_POID)
                        .header("X-Company-Poid", TEST_COMPANY_POID)
                        .header("X-User-Id", TEST_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(service, times(1)).updateItemDetail(eq(TEST_TRANSACTION_POID), eq(TEST_DET_ROW_ID), any(CreateSalesDeliveryNoteItemDtlRequest.class), eq(TEST_COMPANY_POID), eq(TEST_USER_ID));
    }

    @Test
    void testDeleteItemDetail_Success() throws Exception {
        // Arrange
        doNothing().when(service).deleteItemDetail(eq(TEST_TRANSACTION_POID), eq(TEST_DET_ROW_ID), eq(TEST_COMPANY_POID));

        // Act & Assert
        mockMvc.perform(delete("/deliverynote/{transactionPoid}/item-details/{detRowId}", TEST_TRANSACTION_POID, TEST_DET_ROW_ID)
                        .header("X-Group-Poid", TEST_GROUP_POID)
                        .header("X-Company-Poid", TEST_COMPANY_POID))
                .andExpect(status().isOk());

        verify(service, times(1)).deleteItemDetail(eq(TEST_TRANSACTION_POID), eq(TEST_DET_ROW_ID), eq(TEST_COMPANY_POID));
    }

    @Test
    void testGetItemDetails_Success() throws Exception {
        // Arrange
        List<SalesDeliveryNoteItemDtlDto> itemDetails = Collections.singletonList(createTestItemDetailDto());

        when(service.getItemDetails(eq(TEST_TRANSACTION_POID), eq(TEST_COMPANY_POID)))
                .thenReturn(itemDetails);

        // Act & Assert
        mockMvc.perform(get("/deliverynote/{transactionPoid}/item-details", TEST_TRANSACTION_POID)
                        .header("X-Group-Poid", TEST_GROUP_POID)
                        .header("X-Company-Poid", TEST_COMPANY_POID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].transactionPoid").value(TEST_TRANSACTION_POID));

        verify(service, times(1)).getItemDetails(eq(TEST_TRANSACTION_POID), eq(TEST_COMPANY_POID));
    }

    // ========== Business Logic Tests ==========

    @Test
    void testValidateCustomerChange_Success() throws Exception {
        // Arrange
        ValidateCustomerChangeResponse response = new ValidateCustomerChangeResponse();
        response.setCanChange(true);
        response.setMessage("Customer can be changed");

        when(service.validateCustomerChange(eq(TEST_TRANSACTION_POID), eq(TEST_COMPANY_POID)))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/deliverynote/{transactionPoid}/validate-customer-change", TEST_TRANSACTION_POID)
                        .header("X-Group-Poid", TEST_GROUP_POID)
                        .header("X-Company-Poid", TEST_COMPANY_POID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.canChange").value(true));

        verify(service, times(1)).validateCustomerChange(eq(TEST_TRANSACTION_POID), eq(TEST_COMPANY_POID));
    }

    @Test
    void testLoadQuotationItems_Success() throws Exception {
        // Arrange
        LoadQuotationItemsResponse response = new LoadQuotationItemsResponse();
        response.setMessage("Quotation items loaded successfully");
        response.setItems(Collections.emptyList());

        when(service.loadQuotationItems(eq(TEST_GROUP_POID), eq(TEST_TRANSACTION_POID), eq(TEST_COMPANY_POID), eq(TEST_USER_ID)))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/deliverynote/{transactionPoid}/load-quotation-items", TEST_TRANSACTION_POID)
                        .header("X-Group-Poid", TEST_GROUP_POID)
                        .header("X-Company-Poid", TEST_COMPANY_POID)
                        .header("X-User-Id", TEST_USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.message").value("Quotation items loaded successfully"));

        verify(service, times(1)).loadQuotationItems(eq(TEST_GROUP_POID), eq(TEST_TRANSACTION_POID), eq(TEST_COMPANY_POID), eq(TEST_USER_ID));
    }

    @Test
    void testCheckDeliveryNoteDependencies_Success() throws Exception {
        // Arrange
        SalesDeliveryNoteDependenciesDto response = new SalesDeliveryNoteDependenciesDto();
        response.setCanDelete(true);
        response.setMessage("No dependencies found. Delivery note can be deleted.");
        response.setLinkedToQuotation(false);
        response.setSalesInvoiceCount(0);

        when(service.checkDeliveryNoteDependencies(eq(TEST_TRANSACTION_POID), eq(TEST_COMPANY_POID)))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/deliverynote/{transactionPoid}/dependencies", TEST_TRANSACTION_POID)
                        .header("X-Group-Poid", TEST_GROUP_POID)
                        .header("X-Company-Poid", TEST_COMPANY_POID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.canDelete").value(true));

        verify(service, times(1)).checkDeliveryNoteDependencies(eq(TEST_TRANSACTION_POID), eq(TEST_COMPANY_POID));
    }

    // ========== Helper Methods ==========

    private CreateSalesDeliveryNoteRequest createTestRequest() {
        CreateSalesDeliveryNoteRequest request = new CreateSalesDeliveryNoteRequest();
        request.setTransactionDate(Timestamp.valueOf(LocalDateTime.now()));
        request.setCustomerPoid(TEST_CUSTOMER_POID);
        request.setCurrencyCode("USD");
        request.setDeliveryStatus("PROCESSING");
        return request;
    }

    private SalesDeliveryNoteHdrDto createTestDeliveryNoteDto() {
        SalesDeliveryNoteHdrDto dto = new SalesDeliveryNoteHdrDto();
        dto.setTransactionPoid(TEST_TRANSACTION_POID);
        dto.setCompanyPoid(TEST_COMPANY_POID);
        dto.setCustomerPoid(TEST_CUSTOMER_POID);
        dto.setDocRef("DN-001");
        dto.setTransactionDate(Timestamp.valueOf(LocalDateTime.now()));
        dto.setDeliveryStatus("PROCESSING");
        return dto;
    }

    private SalesDeliveryNoteItemDtlDto createTestItemDetailDto() {
        SalesDeliveryNoteItemDtlDto dto = new SalesDeliveryNoteItemDtlDto();
        dto.setTransactionPoid(TEST_TRANSACTION_POID);
        dto.setDetRowId(TEST_DET_ROW_ID);
        dto.setStockPoid(10L);
        dto.setQuantity(10L);
        dto.setPrice(100L);
        dto.setAmount(1000L);
        return dto;
    }
}

