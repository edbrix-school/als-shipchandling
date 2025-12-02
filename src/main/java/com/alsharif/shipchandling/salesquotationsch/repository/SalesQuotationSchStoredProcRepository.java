package com.alsharif.shipchandling.salesquotationsch.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.alsharif.shipchandling.exceptions.CustomException;
import com.alsharif.shipchandling.salesquotationsch.dto.SalesQuotationSchCustomerDetailsDto;
import com.alsharif.shipchandling.salesquotationsch.dto.request.*;
import com.alsharif.shipchandling.salesquotationsch.dto.response.AddressDetailsResponse;
import com.alsharif.shipchandling.salesquotationsch.dto.response.CustomerDetailsResponse;
import com.alsharif.shipchandling.salesquotationsch.dto.response.StoredProcedureResponse;
import com.alsharif.shipchandling.salesquotationsch.dto.response.ValidationResponse;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

@Repository
@Slf4j
public class SalesQuotationSchStoredProcRepository {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public CustomerDetailsResponse callrefreshPreviousQuotationDataProc(Long groupPoid, Long customerPoid,
            Long companyPoid, Long transactionPoid) {
        String proc = "{call PROC_SALES_SCQTN_GET_CUST_DATA(?, ?, ?, ?, ?)}";
        return jdbcTemplate.execute((Connection con) -> {
            try (CallableStatement cs = con.prepareCall(proc)) {
                cs.setLong(1, groupPoid);
                cs.setLong(2, customerPoid);
                cs.setLong(3, companyPoid);
                cs.setLong(4, transactionPoid);
                cs.registerOutParameter(5, Types.REF_CURSOR);

                cs.execute();

                try (ResultSet rs = (ResultSet) cs.getObject(5)) {
                    List<SalesQuotationSchCustomerDetailsDto> items = new ArrayList<>();
                    if (rs != null) {
                        while (rs.next()) {
                            SalesQuotationSchCustomerDetailsDto dto = new SalesQuotationSchCustomerDetailsDto();

                            String currencyCode = rs.getString("CURRENCY_CODE");
                            dto.setCurrencyCode(currencyCode);

                            Long currencyRate = rs.getLong("CURRENCY_RATE");
                            dto.setCurrencyRate(currencyRate);

                            String paymentMode = rs.getString("PAYMENT_MODE");
                            dto.setPaymentMode(paymentMode);

                            items.add(dto);
                        }
                    }
                    CustomerDetailsResponse response = new CustomerDetailsResponse();
                    response.setMessage("Quotation refreshed successfully");
                    response.setCustomerDetails(items);
                    response.setSuccess(true);

                    return response;
                }

            } catch (SQLException ex) {
                throw new CustomException("Error calling PROC_SALES_SCQTN_GET_CUST_DATA: " + ex.getMessage());
            }
        });
    }

    /**
     * PROC_SALES_SCQTN_IMPORT_ITEMS
     * Import items from Excel file to SALES_QUOTATION_ITEM_DTL
     * Note: File upload handling will be done in the service/controller layer
     */
    public StoredProcedureResponse callImportItemsProc(ImportItemsRequest request) {
        String proc = "{call PROC_SALES_SCQTN_IMPORT_ITEMS(?, ?, ?, ?, ?)}";
        return jdbcTemplate.execute((Connection con) -> {
            try (CallableStatement cs = con.prepareCall(proc)) {
                cs.setLong(1, request.getGroupPoid());
                cs.setLong(2, request.getCompanyPoid());
                cs.setLong(3, request.getTransactionPoid());
                cs.setString(4, request.getLoginUser());
                cs.registerOutParameter(5, Types.VARCHAR);

                cs.execute();

                String result = cs.getString(5);
                StoredProcedureResponse response = new StoredProcedureResponse();
                if (result != null && !result.trim().isEmpty() && result.toUpperCase().contains("ERROR")) {
                    response.setSuccess(false);
                    response.setErrorMessage(result);
                    response.setMessage("Import failed");
                } else {
                    response.setSuccess(true);
                    response.setMessage(result != null ? result : "Items imported successfully");
                }
                return response;
            } catch (SQLException ex) {
                throw new CustomException("Error calling PROC_SALES_SCQTN_IMPORT_ITEMS: " + ex.getMessage());
            }
        });
    }

    /**
     * PROC_SALES_SCQTN_ITEMS_CLEAR
     * Clear stock detail table if quotation status is not in 'processing'
     */
    public StoredProcedureResponse callClearItemsProc(ClearItemsRequest request) {
        String proc = "{call PROC_SALES_SCQTN_ITEMS_CLEAR(?, ?, ?, ?)}";
        return jdbcTemplate.execute((Connection con) -> {
            try (CallableStatement cs = con.prepareCall(proc)) {
                cs.setLong(1, request.getGroupPoid());
                cs.setLong(2, request.getCompanyPoid());
                cs.setLong(3, request.getTransactionPoid());
                cs.registerOutParameter(4, Types.VARCHAR);

                cs.execute();

                String result = cs.getString(4);
                StoredProcedureResponse response = new StoredProcedureResponse();
                if (result != null && !result.trim().isEmpty() && result.toUpperCase().contains("ERROR")) {
                    response.setSuccess(false);
                    response.setErrorMessage(result);
                    response.setMessage("Clear items failed");
                } else {
                    response.setSuccess(true);
                    response.setMessage(result != null ? result : "Items cleared successfully");
                }
                return response;
            } catch (SQLException ex) {
                throw new CustomException("Error calling PROC_SALES_SCQTN_ITEMS_CLEAR: " + ex.getMessage());
            }
        });
    }

    /**
     * PROC_SALES_SCQTN_REFRESH_DTL
     * Refresh cost and price from stock master to Sales quotation detail table and
     * RFQ table
     */
    public StoredProcedureResponse callRefreshDetailProc(RefreshDetailRequest request) {
        String proc = "{call PROC_SALES_SCQTN_REFRESH_DTL(?, ?, ?, ?, ?)}";
        return jdbcTemplate.execute((Connection con) -> {
            try (CallableStatement cs = con.prepareCall(proc)) {
                cs.setLong(1, request.getGroupPoid());
                cs.setLong(2, request.getCompanyPoid());
                cs.setLong(3, request.getTransactionPoid());
                cs.setString(4, request.getQuotedRate());
                cs.registerOutParameter(5, Types.VARCHAR);

                cs.execute();

                String result = cs.getString(5);
                StoredProcedureResponse response = new StoredProcedureResponse();
                if (result != null && !result.trim().isEmpty() && result.toUpperCase().contains("ERROR")) {
                    response.setSuccess(false);
                    response.setErrorMessage(result);
                    response.setMessage("Refresh detail failed");
                } else {
                    response.setSuccess(true);
                    response.setMessage(result != null ? result : "Details refreshed successfully");
                }
                return response;
            } catch (SQLException ex) {
                throw new CustomException("Error calling PROC_SALES_SCQTN_REFRESH_DTL: " + ex.getMessage());
            }
        });
    }

    /**
     * PROC_SALES_SCQTN_RFQ_CREATE
     * Create Request For Quotation (RFQ) from the sales quotation
     */
    public StoredProcedureResponse callCreateRfqProc(CreateRfqRequest request) {
        String proc = "{call PROC_SALES_SCQTN_RFQ_CREATE(?, ?, ?, ?, ?)}";
        return jdbcTemplate.execute((Connection con) -> {
            try (CallableStatement cs = con.prepareCall(proc)) {
                cs.setLong(1, request.getGroupPoid());
                cs.setLong(2, request.getCompanyPoid());
                cs.setLong(3, request.getTransactionPoid());
                cs.setString(4, request.getLoginUser());
                cs.registerOutParameter(5, Types.VARCHAR);

                cs.execute();

                String result = cs.getString(5);
                StoredProcedureResponse response = new StoredProcedureResponse();
                if (result != null && !result.trim().isEmpty() && result.toUpperCase().contains("ERROR")) {
                    response.setSuccess(false);
                    response.setErrorMessage(result);
                    response.setMessage("RFQ creation failed");
                } else {
                    response.setSuccess(true);
                    response.setMessage(result != null ? result : "RFQ created successfully");
                }
                return response;
            } catch (SQLException ex) {
                throw new CustomException("Error calling PROC_SALES_SCQTN_RFQ_CREATE: " + ex.getMessage());
            }
        });
    }

    /**
     * PROC_SALES_SCQTN_DN_CREATE
     * Create Delivery Note (DN) from quotation
     */
    public StoredProcedureResponse callCreateDeliveryNoteProc(CreateDeliveryNoteRequest request) {
        String proc = "{call PROC_SALES_SCQTN_DN_CREATE(?, ?, ?, ?, ?)}";
        return jdbcTemplate.execute((Connection con) -> {
            try (CallableStatement cs = con.prepareCall(proc)) {
                cs.setLong(1, request.getGroupPoid());
                cs.setLong(2, request.getCompanyPoid());
                cs.setLong(3, request.getTransactionPoid());
                cs.setString(4, request.getLoginUser());
                cs.registerOutParameter(5, Types.VARCHAR);

                cs.execute();

                String result = cs.getString(5);
                StoredProcedureResponse response = new StoredProcedureResponse();
                if (result != null && !result.trim().isEmpty() && result.toUpperCase().contains("ERROR")) {
                    response.setSuccess(false);
                    response.setErrorMessage(result);
                    response.setMessage("Delivery note creation failed");
                } else {
                    response.setSuccess(true);
                    response.setMessage(result != null ? result : "Delivery note created successfully");
                }
                return response;
            } catch (SQLException ex) {
                throw new CustomException("Error calling PROC_SALES_SCQTN_DN_CREATE: " + ex.getMessage());
            }
        });
    }

    /**
     * PROC_SALES_SCQTN_SELECT_ALL
     * Mark all items in the detail table to create delivery notes
     */
    public StoredProcedureResponse callSelectAllProc(SelectAllRequest request) {
        String proc = "{call PROC_SALES_SCQTN_SELECT_ALL(?, ?, ?, ?, ?)}";
        return jdbcTemplate.execute((Connection con) -> {
            try (CallableStatement cs = con.prepareCall(proc)) {
                cs.setLong(1, request.getGroupPoid());
                cs.setLong(2, request.getCompanyPoid());
                cs.setLong(3, request.getTransactionPoid());
                cs.setString(4, request.getSelectStatus());
                cs.registerOutParameter(5, Types.VARCHAR);

                cs.execute();

                String result = cs.getString(5);
                StoredProcedureResponse response = new StoredProcedureResponse();
                if (result != null && !result.trim().isEmpty() && result.toUpperCase().contains("ERROR")) {
                    response.setSuccess(false);
                    response.setErrorMessage(result);
                    response.setMessage("Select all failed");
                } else {
                    response.setSuccess(true);
                    response.setMessage(result != null ? result : "All items selected successfully");
                }
                return response;
            } catch (SQLException ex) {
                throw new CustomException("Error calling PROC_SALES_SCQTN_SELECT_ALL: " + ex.getMessage());
            }
        });
    }

    /**
     * PROC_SCH_QTN_VALIDATE_CUSTOMER
     * Validate customer or principal before save
     */
    public ValidationResponse callValidateCustomerProc(ValidateCustomerRequest request) {
        String proc = "{call PROC_SCH_QTN_VALIDATE_CUSTOMER(?, ?)}";
        return jdbcTemplate.execute((Connection con) -> {
            try (CallableStatement cs = con.prepareCall(proc)) {
                cs.setLong(1, request.getAddressPoid());
                cs.registerOutParameter(2, Types.VARCHAR);

                cs.execute();

                String result = cs.getString(2);
                ValidationResponse response = new ValidationResponse();
                if (result != null && !result.trim().isEmpty() && result.toUpperCase().contains("ERROR")) {
                    response.setIsValid(false);
                    response.setMessage(result);
                } else {
                    response.setIsValid(true);
                    response.setMessage(result != null ? result : "Customer validated successfully");
                }
                return response;
            } catch (SQLException ex) {
                throw new CustomException("Error calling PROC_SCH_QTN_VALIDATE_CUSTOMER: " + ex.getMessage());
            }
        });
    }

    /**
     * PROC_SALES_SCQTN_QTY_UPDATE
     * Update new details in Delivery Note and RFQ based on user input after DN and
     * RFQ were created
     */
    public StoredProcedureResponse callUpdateQuantityProc(UpdateQuantityRequest request) {
        String proc = "{call PROC_SALES_SCQTN_QTY_UPDATE(?, ?, ?, ?, ?, ?)}";
        return jdbcTemplate.execute((Connection con) -> {
            try (CallableStatement cs = con.prepareCall(proc)) {
                cs.setLong(1, request.getGroupPoid());
                cs.setLong(2, request.getCompanyPoid());
                cs.setLong(3, request.getLoginUserPoid());
                cs.setString(4, request.getLoginUser());
                cs.setLong(5, request.getTransactionPoid());
                cs.registerOutParameter(6, Types.VARCHAR);

                cs.execute();

                String result = cs.getString(6);
                StoredProcedureResponse response = new StoredProcedureResponse();
                if (result != null && !result.trim().isEmpty() && result.toUpperCase().contains("ERROR")) {
                    response.setSuccess(false);
                    response.setErrorMessage(result);
                    response.setMessage("Quantity update failed");
                } else {
                    response.setSuccess(true);
                    response.setMessage(result != null ? result : "Quantity updated successfully");
                }
                return response;
            } catch (SQLException ex) {
                throw new CustomException("Error calling PROC_SALES_SCQTN_QTY_UPDATE: " + ex.getMessage());
            }
        });
    }

    /**
     * PROC_SALES_SCQTN_CBOX_VALIDATE
     * Validate if delivery note has already been created when user unticks a
     * checkbox
     */
    public ValidationResponse callValidateCheckboxProc(ValidateCheckboxRequest request) {
        String proc = "{call PROC_SALES_SCQTN_CBOX_VALIDATE(?, ?, ?, ?, ?)}";
        return jdbcTemplate.execute((Connection con) -> {
            try (CallableStatement cs = con.prepareCall(proc)) {
                cs.setLong(1, request.getTransactionPoid());
                cs.setLong(2, request.getDetRowId());
                cs.setLong(3, request.getStockPoid());
                cs.setString(4, request.getDeliverySelect());
                cs.registerOutParameter(5, Types.VARCHAR);

                cs.execute();

                String result = cs.getString(5);
                ValidationResponse response = new ValidationResponse();
                if (result != null && !result.trim().isEmpty() && result.toUpperCase().contains("ERROR")) {
                    response.setIsValid(false);
                    response.setMessage(result);
                } else {
                    response.setIsValid(true);
                    response.setMessage(result != null ? result : "Checkbox validation passed");
                }
                return response;
            } catch (SQLException ex) {
                throw new CustomException("Error calling PROC_SALES_SCQTN_CBOX_VALIDATE: " + ex.getMessage());
            }
        });
    }

    /**
     * PROC_SALES_SCQTN_DO_CALC
     * Calculate item details price, qty, and other calculations if user selected
     * 'Suppress Calculation' button
     */
    public StoredProcedureResponse callCalculateProc(CalculateRequest request) {
        String proc = "{call PROC_SALES_SCQTN_DO_CALC(?, ?, ?, ?, ?)}";
        return jdbcTemplate.execute((Connection con) -> {
            try (CallableStatement cs = con.prepareCall(proc)) {
                cs.setLong(1, request.getGroupPoid());
                cs.setLong(2, request.getCompanyPoid());
                cs.setString(3, request.getLoginUser());
                cs.setLong(4, request.getTransactionPoid());
                cs.registerOutParameter(5, Types.VARCHAR);

                cs.execute();

                String result = cs.getString(5);
                StoredProcedureResponse response = new StoredProcedureResponse();
                if (result != null && !result.trim().isEmpty() && result.toUpperCase().contains("ERROR")) {
                    response.setSuccess(false);
                    response.setErrorMessage(result);
                    response.setMessage("Calculation failed");
                } else {
                    response.setSuccess(true);
                    response.setMessage(result != null ? result : "Calculation completed successfully");
                }
                return response;
            } catch (SQLException ex) {
                throw new CustomException("Error calling PROC_SALES_SCQTN_DO_CALC: " + ex.getMessage());
            }
        });
    }

    public CustomerDetailsResponse callGetCustomerAddressProc(Long userPoid, Long customerPoid, String addressType) {
        String proc = "{call PROC_GET_QTN_CUST_ADDRESS_V2(?, ?, ?, ?)}";
        return jdbcTemplate.execute((Connection con) -> {
            try (CallableStatement cs = con.prepareCall(proc)) {
                cs.setLong(1, userPoid);
                cs.setLong(2, customerPoid);
                cs.setString(3, addressType);
                cs.registerOutParameter(4, Types.REF_CURSOR);

                cs.execute();

                try (ResultSet rs = (ResultSet) cs.getObject(4)) {
                    List<AddressDetailsResponse> addressDetails = new ArrayList<>();
                    if (rs != null) {
                        while (rs.next()) {
                            AddressDetailsResponse dto = new AddressDetailsResponse();

                            dto.setAddressPoid(customerPoid);

                            String contactPerson = rs.getString("CONTACT_PERSON");
                            dto.setContactPerson(contactPerson);

                            String email1 = rs.getString("EMAIL1");
                            dto.setEmail1(email1);

                            String mobile = rs.getString("MOBILE");
                            dto.setMobile(mobile);

                            addressDetails.add(dto);
                        }
                    }
                    CustomerDetailsResponse response = new CustomerDetailsResponse();
                    response.setMessage("Address details retrieved successfully");
                    response.setAddressDetails(addressDetails);
                    response.setSuccess(true);

                    return response;
                }

            } catch (SQLException ex) {
                throw new CustomException("Error calling PROC_GET_QTN_CUST_ADDRESS_V2: " + ex.getMessage());
            }
        });
    }
}