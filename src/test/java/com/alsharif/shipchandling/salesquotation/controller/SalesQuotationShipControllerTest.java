package com.alsharif.shipchandling.salesquotation.controller;

import com.alsharif.shipchandling.salesquotation.dto.*;
import com.alsharif.shipchandling.salesquotation.service.SalesQuotationShipService;
import com.alsharif.shipchandling.salesquotation.service.SalesQuotationShipService.ChargeTaxResponse;
import com.alsharif.shipchandling.salesquotation.service.SalesQuotationShipService.CustomerContactResponse;
import com.alsharif.shipchandling.salesquotation.service.SalesQuotationShipService.LineAccessResponse;
import com.alsharif.shipchandling.salesquotation.service.SalesQuotationShipService.SalesmanDefaultResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SalesQuotationShipController.class)
class SalesQuotationShipControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SalesQuotationShipService service;

    @Autowired
    private ObjectMapper objectMapper;

    private static final BigDecimal TEST_TRANSACTION_POID = BigDecimal.valueOf(100L);
    private static final BigDecimal TEST_COMPANY_POID = BigDecimal.valueOf(2L);
    private static final BigDecimal TEST_USER_POID = BigDecimal.valueOf(1L);
    private static final String TEST_USER_ID = "testUser";
    private static final BigDecimal TEST_DET_ROW_ID = BigDecimal.valueOf(1L);

    @BeforeEach
    void setUp() {
        // Common setup if needed
    }

    // ========== CRUD Operations Tests ==========

    @Test
    void testCreate_Success() throws Exception {
        // Arrange
        SalesQuotationShipCommand command = new SalesQuotationShipCommand();
        command.setCompanyPoid(TEST_COMPANY_POID);
        command.setUserId(TEST_USER_ID);
        command.setTransactionDate(LocalDate.now());
        command.setDescription("Test Quotation");

        SalesQuotationShipDetailDto responseDto = new SalesQuotationShipDetailDto();
        responseDto.setTransactionPoid(TEST_TRANSACTION_POID);
        responseDto.setDocRef("SQ-001");
        responseDto.setDescription("Test Quotation");

        when(service.createQuotation(any(SalesQuotationShipCommand.class))).thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(post("/sales-quotations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionPoid").value(TEST_TRANSACTION_POID))
                .andExpect(jsonPath("$.docRef").value("SQ-001"));

        verify(service, times(1)).createQuotation(any(SalesQuotationShipCommand.class));
    }

    @Test
    void testUpdate_Success() throws Exception {
        // Arrange
        SalesQuotationShipCommand command = new SalesQuotationShipCommand();
        command.setCompanyPoid(TEST_COMPANY_POID);
        command.setUserId(TEST_USER_ID);
        command.setDescription("Updated Quotation");

        SalesQuotationShipDetailDto responseDto = new SalesQuotationShipDetailDto();
        responseDto.setTransactionPoid(TEST_TRANSACTION_POID);
        responseDto.setDescription("Updated Quotation");

        when(service.updateQuotation(eq(TEST_TRANSACTION_POID), any(SalesQuotationShipCommand.class)))
                .thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(put("/sales-quotations/{transactionPoid}", TEST_TRANSACTION_POID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionPoid").value(TEST_TRANSACTION_POID));

        verify(service, times(1)).updateQuotation(eq(TEST_TRANSACTION_POID), any(SalesQuotationShipCommand.class));
    }

    @Test
    void testDelete_Success() throws Exception {
        // Arrange
        SalesQuotationShipDeleteResponse response = new SalesQuotationShipDeleteResponse();
        response.setCanDelete(true);
        response.setMessage("Quotation deleted successfully");

        when(service.deleteQuotation(eq(TEST_TRANSACTION_POID), eq(TEST_COMPANY_POID), eq(TEST_USER_ID)))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(delete("/sales-quotations/{transactionPoid}", TEST_TRANSACTION_POID)
                        .param("companyId", TEST_COMPANY_POID.toString())
                        .header("X-User-Id", TEST_USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.canDelete").value(true));

        verify(service, times(1)).deleteQuotation(eq(TEST_TRANSACTION_POID), eq(TEST_COMPANY_POID), eq(TEST_USER_ID));
    }

    @Test
    void testSearch_Success() throws Exception {
        // Arrange
        SalesQuotationShipSummaryDto summaryDto = new SalesQuotationShipSummaryDto();
        summaryDto.setTransactionPoid(TEST_TRANSACTION_POID);
        summaryDto.setDocRef("SQ-001");

        SalesQuotationShipListResponse listResponse = new SalesQuotationShipListResponse(
                Collections.singletonList(summaryDto), 1L, 1, 0, 20);

        when(service.search(any(SalesQuotationShipFilter.class), eq(TEST_USER_POID)))
                .thenReturn(listResponse);

        // Act & Assert
        mockMvc.perform(get("/sales-quotations")
                        .param("companyId", TEST_COMPANY_POID.toString())
                        .header("X-User-Id", TEST_USER_POID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(service, times(1)).search(any(SalesQuotationShipFilter.class), eq(TEST_USER_POID));
    }

    @Test
    void testGetById_Success() throws Exception {
        // Arrange
        SalesQuotationShipDetailDto detailDto = new SalesQuotationShipDetailDto();
        detailDto.setTransactionPoid(TEST_TRANSACTION_POID);
        detailDto.setDocRef("SQ-001");

        when(service.getDetail(eq(TEST_TRANSACTION_POID), eq(TEST_COMPANY_POID), eq(false)))
                .thenReturn(detailDto);

        // Act & Assert
        mockMvc.perform(get("/sales-quotations/{transactionPoid}", TEST_TRANSACTION_POID)
                        .param("companyId", TEST_COMPANY_POID.toString())
                        .param("includeDetails", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionPoid").value(TEST_TRANSACTION_POID));

        verify(service, times(1)).getDetail(eq(TEST_TRANSACTION_POID), eq(TEST_COMPANY_POID), eq(false));
    }

    // ========== Customer Operations Tests ==========

    @Test
    void testGetCustomerData_Success() throws Exception {
        // Arrange
        SalesQuotationShipCustomerDataRequest request = new SalesQuotationShipCustomerDataRequest(
                null, TEST_COMPANY_POID, BigDecimal.valueOf(50L), null);

        SalesQuotationShipCustomerDataResponse response = new SalesQuotationShipCustomerDataResponse(
                Collections.emptyList());

        when(service.loadCustomerData(any(SalesQuotationShipCustomerDataRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/sales-quotations/customer-data")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rows").isArray());

        verify(service, times(1)).loadCustomerData(any(SalesQuotationShipCustomerDataRequest.class));
    }

    @Test
    void testValidateCustomer_Success() throws Exception {
        // Arrange
        SalesQuotationShipValidationRequest request = new SalesQuotationShipValidationRequest(
                BigDecimal.valueOf(50L), TEST_TRANSACTION_POID);

        SalesQuotationShipCustomerValidationResponse response = new SalesQuotationShipCustomerValidationResponse(
                "ACTIVE", "VALID");

        when(service.validateCustomer(any(SalesQuotationShipValidationRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/sales-quotations/validate-customer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.validationStatus").value("VALID"));

        verify(service, times(1)).validateCustomer(any(SalesQuotationShipValidationRequest.class));
    }

    // ========== Charge Detail Operations Tests ==========

    @Test
    void testAddChargeDetail_Success() throws Exception {
        // Arrange
        SalesQuotationShipChargeRequest request = new SalesQuotationShipChargeRequest();
        request.setChargePoid(BigDecimal.valueOf(10L));
        request.setSellingCharge(BigDecimal.valueOf(100));

        SalesQuotationShipItemDto responseDto = new SalesQuotationShipItemDto();
        responseDto.setTransactionPoid(TEST_TRANSACTION_POID);
        responseDto.setDetailRowId(TEST_DET_ROW_ID);

        when(service.addChargeDetail(eq(TEST_TRANSACTION_POID), eq(TEST_COMPANY_POID), eq(TEST_USER_ID), any(SalesQuotationShipChargeRequest.class)))
                .thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(post("/sales-quotations/{transactionPoid}/charge-details", TEST_TRANSACTION_POID)
                        .param("companyId", TEST_COMPANY_POID.toString())
                        .header("X-User-Id", TEST_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionPoid").value(TEST_TRANSACTION_POID));

        verify(service, times(1)).addChargeDetail(eq(TEST_TRANSACTION_POID), eq(TEST_COMPANY_POID), eq(TEST_USER_ID), any(SalesQuotationShipChargeRequest.class));
    }

    @Test
    void testUpdateChargeDetail_Success() throws Exception {
        // Arrange
        SalesQuotationShipChargeRequest request = new SalesQuotationShipChargeRequest();
        request.setChargePoid(BigDecimal.valueOf(10L));
        request.setSellingCharge(BigDecimal.valueOf(150));

        SalesQuotationShipItemDto responseDto = new SalesQuotationShipItemDto();
        responseDto.setTransactionPoid(TEST_TRANSACTION_POID);
        responseDto.setDetailRowId(TEST_DET_ROW_ID);

        when(service.updateChargeDetail(eq(TEST_TRANSACTION_POID), eq(TEST_DET_ROW_ID), eq(TEST_COMPANY_POID), eq(TEST_USER_ID), any(SalesQuotationShipChargeRequest.class)))
                .thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(put("/sales-quotations/{transactionPoid}/charge-details/{detRowId}", TEST_TRANSACTION_POID, TEST_DET_ROW_ID)
                        .param("companyId", TEST_COMPANY_POID.toString())
                        .header("X-User-Id", TEST_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.detailRowId").value(TEST_DET_ROW_ID));

        verify(service, times(1)).updateChargeDetail(eq(TEST_TRANSACTION_POID), eq(TEST_DET_ROW_ID), eq(TEST_COMPANY_POID), eq(TEST_USER_ID), any(SalesQuotationShipChargeRequest.class));
    }

    @Test
    void testDeleteChargeDetail_Success() throws Exception {
        // Arrange
        doNothing().when(service).deleteChargeDetail(eq(TEST_TRANSACTION_POID), eq(TEST_DET_ROW_ID), eq(TEST_COMPANY_POID));

        // Act & Assert
        mockMvc.perform(delete("/sales-quotations/{transactionPoid}/charge-details/{detRowId}", TEST_TRANSACTION_POID, TEST_DET_ROW_ID)
                        .param("companyId", TEST_COMPANY_POID.toString()))
                .andExpect(status().isNoContent());

        verify(service, times(1)).deleteChargeDetail(eq(TEST_TRANSACTION_POID), eq(TEST_DET_ROW_ID), eq(TEST_COMPANY_POID));
    }

    @Test
    void testGetChargeDetails_Success() throws Exception {
        // Arrange
        SalesQuotationShipItemDto itemDto = new SalesQuotationShipItemDto();
        itemDto.setTransactionPoid(TEST_TRANSACTION_POID);
        itemDto.setDetailRowId(TEST_DET_ROW_ID);

        List<SalesQuotationShipItemDto> chargeList = Collections.singletonList(itemDto);

        when(service.getChargeDetails(eq(TEST_TRANSACTION_POID), eq(TEST_COMPANY_POID))).thenReturn(chargeList);

        // Act & Assert
        mockMvc.perform(get("/sales-quotations/{transactionPoid}/charge-details", TEST_TRANSACTION_POID)
                        .param("companyId", TEST_COMPANY_POID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].transactionPoid").value(TEST_TRANSACTION_POID));

        verify(service, times(1)).getChargeDetails(eq(TEST_TRANSACTION_POID), eq(TEST_COMPANY_POID));
    }

    // ========== Equipment Detail Operations Tests ==========

    @Test
    void testAddEquipmentDetail_Success() throws Exception {
        // Arrange
        SalesQuotationShipEquipmentRequest request = new SalesQuotationShipEquipmentRequest();
        request.setEquipmentPoid(BigDecimal.valueOf(20L));
        request.setQuantity(BigDecimal.valueOf(5));

        SalesQuotationShipEquipmentDto responseDto = new SalesQuotationShipEquipmentDto();
        responseDto.setTransactionPoid(TEST_TRANSACTION_POID);
        responseDto.setDetailRowId(TEST_DET_ROW_ID);

        when(service.addEquipmentDetail(eq(TEST_TRANSACTION_POID), eq(TEST_COMPANY_POID), eq(TEST_USER_ID), any(SalesQuotationShipEquipmentRequest.class)))
                .thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(post("/sales-quotations/{transactionPoid}/equipment-details", TEST_TRANSACTION_POID)
                        .param("companyId", TEST_COMPANY_POID.toString())
                        .header("X-User-Id", TEST_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionPoid").value(TEST_TRANSACTION_POID));

        verify(service, times(1)).addEquipmentDetail(eq(TEST_TRANSACTION_POID), eq(TEST_COMPANY_POID), eq(TEST_USER_ID), any(SalesQuotationShipEquipmentRequest.class));
    }

    @Test
    void testUpdateEquipmentDetail_Success() throws Exception {
        // Arrange
        SalesQuotationShipEquipmentRequest request = new SalesQuotationShipEquipmentRequest();
        request.setEquipmentPoid(BigDecimal.valueOf(20L));
        request.setQuantity(BigDecimal.valueOf(10));

        SalesQuotationShipEquipmentDto responseDto = new SalesQuotationShipEquipmentDto();
        responseDto.setTransactionPoid(TEST_TRANSACTION_POID);
        responseDto.setDetailRowId(TEST_DET_ROW_ID);

        when(service.updateEquipmentDetail(eq(TEST_TRANSACTION_POID), eq(TEST_DET_ROW_ID), eq(TEST_COMPANY_POID), eq(TEST_USER_ID), any(SalesQuotationShipEquipmentRequest.class)))
                .thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(put("/sales-quotations/{transactionPoid}/equipment-details/{detRowId}", TEST_TRANSACTION_POID, TEST_DET_ROW_ID)
                        .param("companyId", TEST_COMPANY_POID.toString())
                        .header("X-User-Id", TEST_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.detailRowId").value(TEST_DET_ROW_ID));

        verify(service, times(1)).updateEquipmentDetail(eq(TEST_TRANSACTION_POID), eq(TEST_DET_ROW_ID), eq(TEST_COMPANY_POID), eq(TEST_USER_ID), any(SalesQuotationShipEquipmentRequest.class));
    }

    @Test
    void testGetEquipmentDetails_Success() throws Exception {
        // Arrange
        SalesQuotationShipEquipmentDto equipmentDto = new SalesQuotationShipEquipmentDto();
        equipmentDto.setTransactionPoid(TEST_TRANSACTION_POID);
        equipmentDto.setDetailRowId(TEST_DET_ROW_ID);

        List<SalesQuotationShipEquipmentDto> equipmentList = Collections.singletonList(equipmentDto);

        when(service.getEquipmentDetails(eq(TEST_TRANSACTION_POID), eq(TEST_COMPANY_POID))).thenReturn(equipmentList);

        // Act & Assert
        mockMvc.perform(get("/sales-quotations/{transactionPoid}/equipment-details", TEST_TRANSACTION_POID)
                        .param("companyId", TEST_COMPANY_POID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].transactionPoid").value(TEST_TRANSACTION_POID));

        verify(service, times(1)).getEquipmentDetails(eq(TEST_TRANSACTION_POID), eq(TEST_COMPANY_POID));
    }

    // ========== Business Logic Operations Tests ==========

    @Test
    void testGetDefaultSalesman_Success() throws Exception {
        // Arrange
        SalesmanDefaultResponse response = new SalesmanDefaultResponse("10");
        when(service.getDefaultSalesman(eq(TEST_USER_ID))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/sales-quotations/default-salesman")
                        .header("X-User-Id", TEST_USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.salesmanPoid").value("10"));

        verify(service, times(1)).getDefaultSalesman(eq(TEST_USER_ID));
    }

    @Test
    void testGetUserLines_Success() throws Exception {
        // Arrange
        LineAccessResponse response = new LineAccessResponse("1,2,3");
        when(service.getAccessibleLines(eq(TEST_USER_ID))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/sales-quotations/user-lines")
                        .header("X-User-Id", TEST_USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lineList").value("1,2,3"));

        verify(service, times(1)).getAccessibleLines(eq(TEST_USER_ID));
    }

    @Test
    void testGetQuotationTotals_Success() throws Exception {
        // Arrange
        SalesQuotationShipService.QuotationTotalsResponse response = 
                new SalesQuotationShipService.QuotationTotalsResponse(
                        BigDecimal.valueOf(1000), BigDecimal.valueOf(1200), BigDecimal.valueOf(200));
        when(service.getQuotationTotals(eq(TEST_TRANSACTION_POID), eq(TEST_COMPANY_POID))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/sales-quotations/{transactionPoid}/totals", TEST_TRANSACTION_POID)
                        .param("companyId", TEST_COMPANY_POID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.buyingTotal").value(1000))
                .andExpect(jsonPath("$.sellingTotal").value(1200));

        verify(service, times(1)).getQuotationTotals(eq(TEST_TRANSACTION_POID), eq(TEST_COMPANY_POID));
    }

    @Test
    void testCheckQuotationDependencies_Success() throws Exception {
        // Arrange
        SalesQuotationShipService.QuotationDependenciesResponse response = 
                new SalesQuotationShipService.QuotationDependenciesResponse(true, "No dependencies", 0, "Quotation can be deleted");
        when(service.checkQuotationDependencies(eq(TEST_TRANSACTION_POID), eq(TEST_COMPANY_POID))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/sales-quotations/{transactionPoid}/dependencies", TEST_TRANSACTION_POID)
                        .param("companyId", TEST_COMPANY_POID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.canDelete").value(true));

        verify(service, times(1)).checkQuotationDependencies(eq(TEST_TRANSACTION_POID), eq(TEST_COMPANY_POID));
    }

    @Test
    void testGetCustomerContact_Success() throws Exception {
        // Arrange
        CustomerContactResponse response = new CustomerContactResponse("John Doe", "john@example.com");
        when(service.getCustomerContactDetails(eq(TEST_USER_POID), eq(BigDecimal.valueOf(50L)))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/sales-quotations/customers/{customerId}/contact", 50L)
                        .param("userId", TEST_USER_POID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contactPerson").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"));

        verify(service, times(1)).getCustomerContactDetails(eq(TEST_USER_POID), eq(BigDecimal.valueOf(50L)));
    }

    @Test
    void testGetChargeTax_Success() throws Exception {
        // Arrange
        ChargeTaxResponse response = new ChargeTaxResponse(BigDecimal.valueOf(10), BigDecimal.valueOf(5L));
        when(service.getChargeTaxDetails(eq(TEST_COMPANY_POID), eq(BigDecimal.valueOf(50L)), eq(BigDecimal.valueOf(10L))))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/sales-quotations/charges/{chargeId}/tax", 10L)
                        .param("companyId", TEST_COMPANY_POID.toString())
                        .param("customerId", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taxPercentage").value(10));

        verify(service, times(1)).getChargeTaxDetails(eq(TEST_COMPANY_POID), eq(BigDecimal.valueOf(50L)), eq(BigDecimal.valueOf(10L)));
    }

    @Test
    void testAddLocalCharges_Success() throws Exception {
        // Arrange
        SalesQuotationShipService.AddLocalChargesResponse response = 
                new SalesQuotationShipService.AddLocalChargesResponse(true, "Charges added", 3);
        SalesQuotationShipController.AddLocalChargesRequest request = new SalesQuotationShipController.AddLocalChargesRequest(true);
        
        when(service.addLocalCharges(eq(TEST_TRANSACTION_POID), eq(TEST_COMPANY_POID), eq(TEST_USER_ID), eq(true))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/sales-quotations/{transactionPoid}/add-local-charges", TEST_TRANSACTION_POID)
                        .param("companyId", TEST_COMPANY_POID.toString())
                        .header("X-User-Id", TEST_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.chargesAdded").value(3));

        verify(service, times(1)).addLocalCharges(eq(TEST_TRANSACTION_POID), eq(TEST_COMPANY_POID), eq(TEST_USER_ID), eq(true));
    }

    // ========== Additional Endpoint Tests ==========

    @Test
    void testRefreshDetail_Success() throws Exception {
        // Arrange
        SalesQuotationShipRefreshDetailRequest request = new SalesQuotationShipRefreshDetailRequest(
                null, TEST_COMPANY_POID, TEST_TRANSACTION_POID, null);

        when(service.refreshDetailCharges(any(SalesQuotationShipRefreshDetailRequest.class))).thenReturn("SUCCESS");

        // Act & Assert
        mockMvc.perform(post("/sales-quotations/refresh-detail")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("SUCCESS"));

        verify(service, times(1)).refreshDetailCharges(any(SalesQuotationShipRefreshDetailRequest.class));
    }

    @Test
    void testDefaultDetail_Success() throws Exception {
        // Arrange
        SalesQuotationShipDefaultDetailRequest request = new SalesQuotationShipDefaultDetailRequest(
                BigDecimal.valueOf(50L), null, TEST_TRANSACTION_POID, null, null);

        SalesQuotationShipDefaultDetailResponse response = new SalesQuotationShipDefaultDetailResponse(
                BigDecimal.valueOf(1), BigDecimal.valueOf(1), BigDecimal.valueOf(1));

        when(service.setDefaultDetailValues(any(SalesQuotationShipDefaultDetailRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/sales-quotations/default-detail")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.outValue1").value(1));

        verify(service, times(1)).setDefaultDetailValues(any(SalesQuotationShipDefaultDetailRequest.class));
    }
}

