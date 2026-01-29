package com.asg.shipchandling.salesquotationsch.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.asg.shipchandling.exceptions.CustomException;
import com.asg.shipchandling.salesquotationsch.dto.TempNewAddressRow;
import com.asg.shipchandling.salesquotationsch.dto.SalesQuotationSchCustomerDetailsDto;
import com.asg.shipchandling.salesquotationsch.dto.request.*;
import com.asg.shipchandling.salesquotationsch.dto.response.CustomerDetailsResponse;
import com.asg.shipchandling.salesquotationsch.dto.response.StoredProcedureResponse;
import com.asg.shipchandling.salesquotationsch.dto.response.ValidationResponse;
import com.asg.shipchandling.salesquotationsch.dto.response.AddressDetailsResponse;
import com.asg.shipchandling.salesquotationsch.dto.response.TempAddressProcedureResponse;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

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

                            BigDecimal currencyRate = rs.getBigDecimal("CURRENCY_RATE");
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
     * PROC_NEW_ADDRESS_LOADLIST
     * Legacy temp-address loader used on "reopen": load all temp addresses for a document.
     */
    public List<TempNewAddressRow> callNewTempAddressLoadListProc(
            Long loginGroupPoid,
            Long loginCompanyPoid,
            Long loginUserPoid,
            String docId,
            Long docKeyPoid
    ) {
        String proc = "{call PROC_NEW_ADDRESS_LOADLIST(?, ?, ?, ?, ?, ?)}";

        return jdbcTemplate.execute((Connection con) -> {
            try (CallableStatement cs = con.prepareCall(proc)) {
                cs.setLong(1, loginGroupPoid);
                cs.setLong(2, loginCompanyPoid);
                cs.setLong(3, loginUserPoid);
                cs.setString(4, docId);
                cs.setLong(5, docKeyPoid);
                cs.registerOutParameter(6, Types.REF_CURSOR);

                cs.execute();

                List<TempNewAddressRow> rows = new ArrayList<>();
                try (ResultSet rs = (ResultSet) cs.getObject(6)) {
                    if (rs != null) {
                        while (rs.next()) {
                            TempNewAddressRow row = new TempNewAddressRow();
                            row.setNewAddressPoid(rs.getObject("NEW_ADDRESS_POID") != null ? rs.getLong("NEW_ADDRESS_POID") : null);
                            row.setDocFieldName(rs.getString("DOC_FIELD_NAME"));
                            row.setAddressName(rs.getString("ADDRESS_NAME"));
                            row.setOffTel1(rs.getString("OFF_TEL1"));
                            row.setOffTel2(rs.getString("OFF_TEL2"));
                            row.setContactPerson(rs.getString("CONTACT_PERSON"));
                            row.setDesignation(rs.getString("DESIGNATION"));
                            row.setMobile(rs.getString("MOBILE"));
                            row.setFax(rs.getString("FAX"));
                            row.setEmail1(rs.getString("EMAIL1"));
                            row.setEmail2(rs.getString("EMAIL2"));
                            row.setWebsite(rs.getString("WEBSITE"));
                            row.setPoBox(rs.getString("PO_BOX"));
                            row.setOffNo(rs.getString("OFF_NO"));
                            row.setBldg(rs.getString("BLDG"));
                            row.setRoad(rs.getString("ROAD"));
                            row.setAreaCity(rs.getString("AREA_CITY"));
                            row.setState(rs.getString("STATE"));
                            row.setCountryPoid(rs.getObject("COUNTRY_POID") != null ? rs.getLong("COUNTRY_POID") : null);
                            row.setLandMark(rs.getString("LAND_MARK"));
                            rows.add(row);
                        }
                    }
                }

                return rows;
            } catch (SQLException ex) {
                throw new CustomException("Error calling PROC_NEW_ADDRESS_LOADLIST: " + ex.getMessage());
            }
        });
    }

    /**
     * PROC_NEW_ADDRESS_CREATE_UPDATE
     * Create/Update a temporary "new address" record for a document (GLOBAL_NEW_ADDRESS_DETAILS)
     * Legacy keys for Sales Quotation (SCH): DocId=350-101, DocFieldName=CustomerPoid
     */
    public TempAddressProcedureResponse callNewTempAddressCreateUpdateProc(
            Long loginGroupPoid,
            Long loginUserPoid,
            String docId,
            Long docKeyPoid,
            String docFieldName,
            String addressName,
            Long newAddressPoid,
            String offTel1,
            String offTel2,
            String contactPerson,
            String designation,
            String mobile,
            String fax,
            String email1,
            String email2,
            String website,
            String poBox,
            String offNo,
            String bldg,
            String road,
            String areaCity,
            String state,
            Long countryPoid,
            String landMark,
            String action
    ) {
        String proc = "{call PROC_NEW_ADDRESS_CREATE_UPDATE(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}";

        return jdbcTemplate.execute((Connection con) -> {
            try (CallableStatement cs = con.prepareCall(proc)) {
                cs.setLong(1, loginGroupPoid);
                cs.setLong(2, loginUserPoid);
                cs.setString(3, docId);
                cs.setLong(4, docKeyPoid);
                cs.setString(5, docFieldName);

                cs.setString(6, addressName);
                if (newAddressPoid == null) {
                    cs.setNull(7, Types.NUMERIC);
                } else {
                    cs.setLong(7, newAddressPoid);
                }
                cs.setString(8, offTel1);
                cs.setString(9, offTel2);

                cs.setString(10, contactPerson);
                cs.setString(11, designation);
                cs.setString(12, mobile);
                cs.setString(13, fax);

                cs.setString(14, email1);
                cs.setString(15, email2);
                cs.setString(16, website);
                cs.setString(17, poBox);

                cs.setString(18, offNo);
                cs.setString(19, bldg);
                cs.setString(20, road);
                cs.setString(21, areaCity);

                cs.setString(22, state);
                if (countryPoid == null) {
                    cs.setNull(23, Types.NUMERIC);
                } else {
                    cs.setLong(23, countryPoid);
                }
                cs.setString(24, landMark);
                cs.setString(25, action);

                cs.registerOutParameter(26, Types.VARCHAR); // P_ACTION_RESULT
                cs.registerOutParameter(27, Types.NUMERIC); // P_RESULT_NEW_ADDRESS_POID

                cs.execute();

                String result = cs.getString(26);
                BigDecimal resultNewAddressPoid = (BigDecimal) cs.getObject(27);

                TempAddressProcedureResponse response = new TempAddressProcedureResponse();
                if (result != null && !result.trim().isEmpty() && result.toUpperCase().contains("ERROR")) {
                    response.setSuccess(false);
                    response.setErrorMessage(result);
                    response.setMessage("Temp address save failed");
                    response.setNewAddressPoid(null);
                } else {
                    response.setSuccess(true);
                    response.setMessage(result != null ? result : "Temp address saved successfully");
                    response.setErrorMessage(null);
                    response.setNewAddressPoid(resultNewAddressPoid != null ? resultNewAddressPoid.longValue() : null);
                }

                return response;
            } catch (SQLException ex) {
                throw new CustomException("Error calling PROC_NEW_ADDRESS_CREATE_UPDATE: " + ex.getMessage());
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

                            dto.setAddressPoid(BigDecimal.valueOf(customerPoid));

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