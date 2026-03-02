package com.asg.shipchandling.salesinvoice.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.asg.shipchandling.exceptions.CustomException;
import com.asg.shipchandling.salesinvoice.dto.QuotationSummaryDto;
import com.asg.shipchandling.salesinvoice.dto.QuotationItemDto;
import com.asg.shipchandling.salesinvoice.dto.request.CalculateDiscountCommissionRequest;
import com.asg.shipchandling.salesinvoice.dto.request.LoadQuotationItemsRequest;
import com.asg.shipchandling.salesinvoice.dto.response.CalculateDiscountCommissionResponse;
import com.asg.shipchandling.salesinvoice.dto.response.CreditDetailsResponse;
import com.asg.shipchandling.salesinvoice.dto.response.LoadQuotationSummaryResponse;
import com.asg.shipchandling.salesinvoice.dto.response.LoadQuotationItemsResponse;
import com.asg.shipchandling.salesinvoice.dto.response.RefreshGpProcResponse;
import com.asg.shipchandling.salesinvoice.dto.response.UnloadQuotationResponse;
import com.asg.shipchandling.salesinvoice.dto.response.ValidationResponse;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

@Repository
@Slf4j
public class SalesInvoiceStoredProcRepository {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public ValidationResponse callCustomerValidateProc(Long customerPoid, String creditType, String authorizedId) {
        String proc = "{call PROC_VALIDATE_CUSTOMER(?, ?, ?, ?)}";
        return jdbcTemplate.execute((Connection con) -> {
            try (CallableStatement cs = con.prepareCall(proc)) {

                cs.setLong(1, customerPoid);
                cs.registerOutParameter(2, Types.VARCHAR);
                cs.setString(3, creditType);
                cs.setString(4, authorizedId);

                cs.execute();
                String procResult = cs.getString(2);

                if (procResult != null && procResult.toUpperCase().contains("ERROR")) {
                    throw new CustomException("PROC_VALIDATE_CUSTOMER failed: " + procResult);
                }

                ValidationResponse response = new ValidationResponse();
                response.setMessage(procResult);
                response.setSuccess(procResult.contains("SUCCESS"));
                return response;
            } catch (SQLException ex) {
                throw new CustomException("Error calling PROC_VALIDATE_CUSTOMER: " + ex.getMessage());
            }
        });
    }

    public boolean callCustomerEditValidateProc(Long transactionPoid, Long customerPoid) {
        String proc = "{call PROC_AR_SCH_EDIT_VALIDATE(?, ?, ?)}";
        return jdbcTemplate.execute((Connection con) -> {
            try (CallableStatement cs = con.prepareCall(proc)) {

                cs.setLong(1, transactionPoid);
                cs.setLong(2, customerPoid);
                cs.registerOutParameter(3, Types.VARCHAR);

                cs.execute();
                String procResult = cs.getString(3);

                if (procResult != null && procResult.toUpperCase().contains("ERROR")) {
                    throw new CustomException("PROC_AR_SCH_EDIT_VALIDATE failed: " + procResult);
                }
                return procResult.contains("True");
            } catch (SQLException ex) {
                throw new CustomException("Error calling PROC_AR_SCH_EDIT_VALIDATE: " + ex.getMessage());
            }
        });
    }

    public UnloadQuotationResponse callUnloadQuotationProc(Long transactionPoid, Long qtnPoid) {
        String proc = "{call PROC_AR_SCH_UNLOAD_QUOTATION1(?, ?, ?)}";
        return jdbcTemplate.execute((Connection con) -> {
            try (CallableStatement cs = con.prepareCall(proc)) {

                cs.setLong(1, transactionPoid);
                cs.setLong(2, qtnPoid);
                cs.registerOutParameter(3, Types.VARCHAR);

                cs.execute();
                String procResult = cs.getString(3);

                UnloadQuotationResponse response = new UnloadQuotationResponse();
                response.setMessage(procResult);
                response.setSuccess(procResult != null && procResult.contains("SUCCESS"));
                return response;
            } catch (SQLException ex) {
                throw new CustomException("Error calling PROC_AR_SCH_UNLOAD_QUOTATION1: " + ex.getMessage());
            }
        });
    }

    public ValidationResponse callLoadDeliveryNoteProc(Long transactionPoid) {
        String proc = "{call PROC_AR_SCH_SALESINV_DN_LOAD(?, ?)}";
        return jdbcTemplate.execute((Connection con) -> {
            try (CallableStatement cs = con.prepareCall(proc)) {

                cs.setLong(1, transactionPoid);
                cs.registerOutParameter(2, Types.VARCHAR);

                cs.execute();
                String procResult = cs.getString(2);

                if (procResult != null && procResult.toUpperCase().contains("ERROR")) {
                    throw new CustomException("PROC_AR_SCH_SALESINV_DN_LOAD failed: " + procResult);
                }

                ValidationResponse response = new ValidationResponse();
                response.setMessage(procResult);
                response.setSuccess(procResult.contains("No_Data") ? false : true);
                return response;
            } catch (SQLException ex) {
                throw new CustomException("Error calling PROC_AR_SCH_SALESINV_DN_LOAD: " + ex.getMessage());
            }
        });
    }

    public LoadQuotationItemsResponse callLoadQuotationItemsProc(Long transactionPoid,
            LoadQuotationItemsRequest request) {
        String proc = "{call PROC_AR_SCH_QTN_LOAD_BUTTON(?, ?, ?, ? ,? ,?, ?)}";
        return jdbcTemplate.execute((Connection con) -> {
            try (CallableStatement cs = con.prepareCall(proc)) {

                cs.setLong(1, transactionPoid);
                cs.setLong(2, request.getQtnPoid() != null ? request.getQtnPoid() : null);
                cs.setBigDecimal(3, request.getIncentiveAmt());
                cs.setBigDecimal(4, request.getIncentiveAmt2());
                cs.setBigDecimal(5, request.getIncentiveAmt3());
                cs.registerOutParameter(6, Types.VARCHAR);
                cs.registerOutParameter(7, Types.REF_CURSOR);

                cs.execute();

                String procResult = cs.getString(6);
                if (procResult != null && procResult.toUpperCase().contains("ERROR")) {
                    LoadQuotationItemsResponse response = new LoadQuotationItemsResponse();
                    String message = procResult;
                    response.setMessage(message);
                    response.setSuccess(false);
                    response.setItems(new ArrayList<>());
                    return response;
                }
                try (ResultSet rs = (ResultSet) cs.getObject(7)) {
                    List<QuotationItemDto> items = new ArrayList<>();
                    if (rs != null) {
                        while (rs.next()) {
                            QuotationItemDto dto = new QuotationItemDto();

                            BigDecimal discountPercent = rs.getBigDecimal("DISCOUNT_PERCENT");
                            dto.setDiscountPercent(discountPercent);

                            BigDecimal discountAmt = rs.getBigDecimal("DISCOUNT_AMT");
                            dto.setDiscountAmt(discountAmt);

                            BigDecimal incentivePercent = rs.getBigDecimal("INCENTIVE_PERCENT");
                            dto.setIncentivePercent(incentivePercent);

                            BigDecimal incentivePercent2 = rs.getBigDecimal("INCENTIVE_PERCENT2");
                            dto.setIncentivePercent2(incentivePercent2);

                            BigDecimal incentivePercent3 = rs.getBigDecimal("INCENTIVE_PERCENT3");
                            dto.setIncentivePercent3(incentivePercent3);

                            BigDecimal incentiveAmt = rs.getBigDecimal("INCENTIVE_AMT");
                            dto.setIncentiveAmt(incentiveAmt);

                            BigDecimal incentiveAmt2 = rs.getBigDecimal("INCENTIVE_AMT2");
                            dto.setIncentiveAmt2(incentiveAmt2);

                            BigDecimal incentiveAmt3 = rs.getBigDecimal("INCENTIVE_AMT3");
                            dto.setIncentiveAmt3(incentiveAmt3);

                            BigDecimal totalGpAmt = rs.getBigDecimal("TOTAL_GP_AMT");
                            dto.setTotalGpAmt(totalGpAmt);

                            BigDecimal totalGpPercent = rs.getBigDecimal("TOTAL_GP_PERCENT");
                            dto.setTotalGpPercent(totalGpPercent);

                            BigDecimal invAmount = rs.getBigDecimal("INV_AMOUNT");
                            dto.setInvAmount(invAmount);

                            items.add(dto);
                        }
                    }
                    LoadQuotationItemsResponse response = new LoadQuotationItemsResponse();
                    String message = procResult != null ? procResult : "Quotation items loaded successfully";
                    response.setMessage(message);
                    response.setSuccess(message.toUpperCase().contains("SUCCESS"));
                    response.setItems(items);
                    log.info(
                            "loadQuotationItems: PROC_AR_SCH_QTN_LOAD_BUTTON completed successfully for transactionPoid={}",
                            transactionPoid);
                    return response;
                }

            } catch (SQLException ex) {
                throw new CustomException("Error calling PROC_AR_SCH_QTN_LOAD_BUTTON: " + ex.getMessage());
            }
        });
    }

    public ValidationResponse callCalculateGpProc(Long transactionPoid, Long qtnId) {
        String proc = "{call PROC_AR_SCH_GP_CALC(?, ?, ?)}";
        return jdbcTemplate.execute((Connection con) -> {
            try (CallableStatement cs = con.prepareCall(proc)) {

                cs.setLong(1, transactionPoid);
                cs.setLong(2, qtnId);
                cs.registerOutParameter(3, Types.VARCHAR);

                cs.execute();
                String procResult = cs.getString(3);

                if (procResult != null && procResult.toUpperCase().contains("ERROR")) {
                    throw new CustomException("PROC_AR_SCH_GP_CALC failed: " + procResult);
                }

                ValidationResponse response = new ValidationResponse();
                response.setMessage(procResult);
                response.setSuccess(procResult.contains("UPDATED"));
                return response;
            } catch (SQLException ex) {
                throw new CustomException("Error calling PROC_AR_SCH_GP_CALC: " + ex.getMessage());
            }
        });
    }

    public RefreshGpProcResponse callRefreshGpProc(
            Long userPoid,
            Long transactionPoid,
            Long qtnPoid,
            CalculateDiscountCommissionRequest request) {
        String proc = "{call PROC_AR_SCH_DIS_COM_CAL(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}";
        return jdbcTemplate.execute((Connection con) -> {
            try (CallableStatement cs = con.prepareCall(proc)) {
                String type = request != null && request.getType() != null ? request.getType().toUpperCase() : "AMOUNT";
                cs.setLong(1, userPoid);
                cs.setLong(2, transactionPoid);
                cs.setLong(3, qtnPoid);
                cs.setBigDecimal(4, request != null ? request.getInvDiscount() : null);
                cs.setBigDecimal(5, request != null ? request.getIncentiveAmt() : null);
                cs.setBigDecimal(6, request != null ? request.getIncentiveAmt2() : null);
                cs.setBigDecimal(7, request != null ? request.getIncentiveAmt3() : null);
                cs.setString(8, type);
                cs.setBigDecimal(9, request != null ? request.getIncentivePercent() : null);
                cs.setBigDecimal(10, request != null ? request.getIncentivePercent2() : null);
                cs.setBigDecimal(11, request != null ? request.getIncentivePercent3() : null);
                cs.registerOutParameter(12, Types.VARCHAR);
                cs.registerOutParameter(13, Types.REF_CURSOR);

                cs.execute();

                String procResult = cs.getString(12);
                if (procResult != null && procResult.toUpperCase().contains("ERROR")) {
                    throw new CustomException("PROC_AR_SCH_DIS_COM_CAL failed: " + procResult);
                }

                try (ResultSet rs = (ResultSet) cs.getObject(13)) {
                    if (rs != null && rs.next()) {
                        BigDecimal discountPercent = rs.getBigDecimal("DISCOUNT_PERCENT");
                        BigDecimal discountAmt = rs.getBigDecimal("DISCOUNT_AMT");
                        BigDecimal incentivePercent = rs.getBigDecimal("INCENTIVE_PERCENT");
                        BigDecimal incentivePercent2 = rs.getBigDecimal("INCENTIVE_PERCENT2");
                        BigDecimal incentivePercent3 = rs.getBigDecimal("INCENTIVE_PERCENT3");
                        BigDecimal incentiveAmount = rs.getBigDecimal("INCENTIVE_AMT");
                        BigDecimal incentiveAmount2 = rs.getBigDecimal("INCENTIVE_AMT2");
                        BigDecimal incentiveAmount3 = rs.getBigDecimal("INCENTIVE_AMT3");
                        BigDecimal totalGpAmt = rs.getBigDecimal("TOTAL_GP_AMT");
                        BigDecimal totalGpPercent = rs.getBigDecimal("TOTAL_GP_PERCENT");
                        BigDecimal invAmount = rs.getBigDecimal("INV_AMOUNT");
                        
                        RefreshGpProcResponse response = new RefreshGpProcResponse();
                        response.setMessage(procResult != null ? procResult : "Discount/Commission calculated successfully");
                        response.setSuccess(procResult == null || !procResult.toUpperCase().contains("ERROR"));
                        response.setDiscountPercent(discountPercent);
                        response.setDiscountAmt(discountAmt);
                        response.setIncentivePercent(incentivePercent);
                        response.setIncentivePercent2(incentivePercent2);
                        response.setIncentivePercent3(incentivePercent3);
                        response.setIncentiveAmt(incentiveAmount);
                        response.setIncentiveAmt2(incentiveAmount2);
                        response.setIncentiveAmt3(incentiveAmount3);
                        response.setTotalGpAmt(totalGpAmt);
                        response.setTotalGpPercent(totalGpPercent);
                        response.setInvAmount(invAmount);
                        return response;
                    }
                }

                RefreshGpProcResponse response = new RefreshGpProcResponse();
                response.setMessage(procResult != null ? procResult : "Discount/Commission calculated successfully");
                response.setSuccess(procResult == null || !procResult.toUpperCase().contains("ERROR"));
                return response;
            } catch (SQLException ex) {
                throw new CustomException("Error calling PROC_AR_SCH_DIS_COM_CAL: " + ex.getMessage());
            }
        });
    }

    public CreditDetailsResponse callCalculateDueDateProc(Date docDate,
            Long creditDays, String calculationType, Long customerPoid) {
        String proc = "{call PROC_CALC_DUEDAYS(?, ?, ?, ? ,?, ?, ?, ?)}";
        return jdbcTemplate.execute((Connection con) -> {
            try (CallableStatement cs = con.prepareCall(proc)) {

                cs.setDate(1, docDate);
                cs.setNull(2, Types.VARCHAR);
                cs.setLong(3, creditDays);
                cs.setString(4, calculationType);
                cs.setLong(5, customerPoid);
                cs.registerOutParameter(6, Types.DATE);
                cs.registerOutParameter(7, Types.BIGINT);
                cs.registerOutParameter(8, Types.VARCHAR);

                cs.execute();

                Date resultDueDate = cs.getDate(6);
                Long dueDays = cs.getLong(7);
                String procResult = cs.getString(8);

                if (procResult != null && procResult.toUpperCase().contains("ERROR")) {
                    throw new CustomException("PROC_CALC_DUEDAYS failed: " + procResult);
                }

                CreditDetailsResponse response = new CreditDetailsResponse();
                response.setMessage(procResult);
                response.setSuccess(procResult != null && procResult.contains("True"));
                response.setDueDate(resultDueDate != null ? resultDueDate.toLocalDate() : null);
                response.setCreditDays(dueDays);

                return response;
            } catch (SQLException ex) {
                throw new CustomException("Error calling PROC_CALC_DUEDAYS: " + ex.getMessage());
            }
        });

    }

    public CalculateDiscountCommissionResponse callCalculateItemDiscountCommissionProc(
            Long transactionPoid, CalculateDiscountCommissionRequest request, Long qtnPoid,
            Long userId) {
        String proc = "{call PROC_AR_SCH_DIS_COM_CAL(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}";
        return jdbcTemplate.execute((Connection con) -> {
            try (CallableStatement cs = con.prepareCall(proc)) {

                cs.setLong(1, userId);
                cs.setLong(2, transactionPoid);
                cs.setLong(3, qtnPoid);
                cs.setBigDecimal(4, request.getInvDiscount());
                cs.setBigDecimal(5, request.getIncentiveAmt());
                cs.setBigDecimal(6, request.getIncentiveAmt2());
                cs.setBigDecimal(7, request.getIncentiveAmt3());
                cs.setString(8, request.getType() != null ? request.getType() : "Amount");
                cs.setBigDecimal(9, request.getIncentivePercent());
                cs.setBigDecimal(10, request.getIncentivePercent2());
                cs.setBigDecimal(11, request.getIncentivePercent3());
                cs.registerOutParameter(12, Types.VARCHAR);
                cs.registerOutParameter(13, Types.REF_CURSOR);

                cs.execute();

                String procResult = cs.getString(12);

                try (ResultSet rs = (ResultSet) cs.getObject(13)) {
                    if (procResult != null && procResult.toUpperCase().contains("ERROR")) {
                        throw new CustomException("PROC_AR_SCH_DIS_COM_CAL failed: " + procResult);
                    }

                    List<QuotationItemDto> items = new ArrayList<>();
                    if (rs != null) {
                        while (rs.next()) {
                            QuotationItemDto dto = new QuotationItemDto();

                            BigDecimal discountPercent = rs.getBigDecimal("DISCOUNT_PERCENT");
                            dto.setDiscountPercent(discountPercent);

                            BigDecimal discountAmt = rs.getBigDecimal("DISCOUNT_AMT");
                            dto.setDiscountAmt(discountAmt);

                            BigDecimal incentivePercent = rs.getBigDecimal("INCENTIVE_PERCENT");
                            dto.setIncentivePercent(incentivePercent);

                            BigDecimal incentivePercent2 = rs.getBigDecimal("INCENTIVE_PERCENT2");
                            dto.setIncentivePercent2(incentivePercent2);

                            BigDecimal incentivePercent3 = rs.getBigDecimal("INCENTIVE_PERCENT3");
                            dto.setIncentivePercent3(incentivePercent3);

                            BigDecimal incentiveAmt = rs.getBigDecimal("INCENTIVE_AMT");
                            dto.setIncentiveAmt(incentiveAmt);

                            BigDecimal incentiveAmt2 = rs.getBigDecimal("INCENTIVE_AMT2");
                            dto.setIncentiveAmt2(incentiveAmt2);

                            BigDecimal incentiveAmt3 = rs.getBigDecimal("INCENTIVE_AMT3");
                            dto.setIncentiveAmt3(incentiveAmt3);

                            BigDecimal totalGpAmt = rs.getBigDecimal("TOTAL_GP_AMT");
                            dto.setTotalGpAmt(totalGpAmt);

                            BigDecimal totalGpPercent = rs.getBigDecimal("TOTAL_GP_PERCENT");
                            dto.setTotalGpPercent(totalGpPercent);

                            BigDecimal invAmount = rs.getBigDecimal("INV_AMOUNT");
                            dto.setInvAmount(invAmount);

                            items.add(dto);
                        }
                    }
                    CalculateDiscountCommissionResponse response = new CalculateDiscountCommissionResponse();
                    response.setMessage("Discount/Commission calculated successfully");
                    response.setSuccess(true);
                    response.setItems(items);
                    return response;
                }
            } catch (SQLException ex) {
                throw new CustomException("Error calling PROC_AR_SCH_DIS_COM_CAL: " + ex.getMessage());
            }
        });
    }

    public CalculateDiscountCommissionResponse callCalculateHeaderDiscountCommissionProc(
            Long transactionPoid, CalculateDiscountCommissionRequest request, Long detRowId, Long qtnPoid,
            Long userId) {
        String proc = "{call PROC_AR_SCH_DIS_COM_CAL_HDR(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}";
        return jdbcTemplate.execute((Connection con) -> {
            try (CallableStatement cs = con.prepareCall(proc)) {

                cs.setLong(1, userId);
                cs.setLong(2, transactionPoid);
                cs.setLong(3, qtnPoid);
                cs.setBigDecimal(4, request.getInvDiscount());
                cs.setBigDecimal(5, request.getIncentiveAmt());
                cs.setBigDecimal(6, request.getIncentiveAmt2());
                cs.setBigDecimal(7, request.getIncentiveAmt3());
                cs.setString(8, request.getType() != null ? request.getType() : "Amount");
                cs.setBigDecimal(9, request.getIncentivePercent());
                cs.setBigDecimal(10, request.getIncentivePercent2());
                cs.setBigDecimal(11, request.getIncentivePercent3());
                cs.registerOutParameter(12, Types.VARCHAR);
                cs.registerOutParameter(13, Types.REF_CURSOR);

                cs.execute();
                String procResult = cs.getString(2);

                if (procResult != null && procResult.toUpperCase().contains("ERROR")) {
                    throw new CustomException("PROC_AR_SCH_DIS_COM_CAL_HDR failed: " + procResult);
                }

                CalculateDiscountCommissionResponse response = new CalculateDiscountCommissionResponse();
                response.setMessage(procResult);
                response.setSuccess(procResult.contains("SUCCESS"));
                return response;
            } catch (SQLException ex) {
                throw new CustomException("Error calling PROC_AR_SCH_DIS_COM_CAL_HDR: " + ex.getMessage());
            }
        });
    }

    public ValidationResponse callLoadCostBookingsProc(Long transactionPoid, Long qtnId) {
        String proc = "{call PROC_AR_SCH_SALES_INV_PJ_LOAD1(?, ?, ?)}";
        return jdbcTemplate.execute((Connection con) -> {
            try (CallableStatement cs = con.prepareCall(proc)) {

                cs.setLong(1, transactionPoid);
                cs.setLong(2, qtnId);
                cs.registerOutParameter(3, Types.VARCHAR);

                cs.execute();
                String procResult = cs.getString(3);

                ValidationResponse response = new ValidationResponse();
                response.setMessage(procResult);
                response.setSuccess(procResult != null && procResult.contains("SUCCESS"));
                return response;
            } catch (SQLException ex) {
                throw new CustomException("Error calling PROC_AR_SCH_SALES_INV_PJ_LOAD1: " + ex.getMessage());
            }
        });
    }

    public CreditDetailsResponse callLoadCreditDetailsProc(Long groupPoid, Long companyPoid,
            String docId, Date docDate, String partyType, Long partyPoid) {
        String proc = "{call PROC_LOAD_CREDIT_DETAILS(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}";
        return jdbcTemplate.execute((Connection con) -> {
            try (CallableStatement cs = con.prepareCall(proc)) {

                cs.setLong(1, groupPoid);
                cs.setLong(2, companyPoid);
                cs.setNull(3, Types.NUMERIC);
                cs.setString(4, docId);
                cs.setNull(5, Types.NUMERIC);
                cs.setDate(6, docDate);
                cs.setString(7, partyType);
                cs.setLong(8, partyPoid);
                cs.registerOutParameter(9, Types.VARCHAR);
                cs.registerOutParameter(10, Types.REF_CURSOR);

                cs.execute();
                String procResult = cs.getString(9);

                 try (ResultSet rs = (ResultSet) cs.getObject(10)) {
                    if (procResult != null && procResult.toUpperCase().contains("ERROR")) {
                        throw new CustomException("PROC_LOAD_CREDIT_DETAILS failed: " + procResult);
                    }

                    CreditDetailsResponse response = new CreditDetailsResponse();
                    if (rs != null && rs.next()) {
                        Long creditDays = rs.getLong("CREDIT_PERIOD");
                        Date dueDate = rs.getDate("DUE_DATE");
                        
                        response.setCreditDays(creditDays);
                        response.setDueDate(dueDate != null ? dueDate.toLocalDate() : null);
                    }
                    
                    response.setMessage("Credit Details loaded successfully");
                    response.setSuccess(true);
                   
                    return response;
                }
            } catch (SQLException ex) {
                throw new CustomException("Error calling PROC_LOAD_CREDIT_DETAILS: " + ex.getMessage());
            }
        });
    }

    public LoadQuotationSummaryResponse callLoadQuotationSummaryProc(Long transactionId, Long qtnPoId) {
        String proc = "{call PROC_AR_SCH_QTN_LOAD_CUR1(?, ?, ?, ?)}";
        return jdbcTemplate.execute((Connection con) -> {
            try (CallableStatement cs = con.prepareCall(proc)) {

                cs.setLong(1, transactionId);
                cs.setLong(2, qtnPoId);
                cs.registerOutParameter(3, Types.VARCHAR);
                cs.registerOutParameter(4, Types.REF_CURSOR);

                cs.execute();
                String procResult = cs.getString(3);
                try (ResultSet rs = (ResultSet) cs.getObject(4)) {
                    if (procResult != null && procResult.toUpperCase().contains("ERROR")) {
                        throw new CustomException("PROC_AR_SCH_QTN_LOAD_CUR1 failed: " + procResult);
                    }

                    QuotationSummaryDto summary = null;
                    if (rs != null && rs.next()) {
                        summary = new QuotationSummaryDto();
                        summary.setStatus(rs.getString("STATUS"));
                        summary.setDataLoadType(rs.getString("DATA_LOAD_TYPE"));
                        summary.setPaymentMode(rs.getString("PAYMENT_MODE"));
                        summary.setVesselName(rs.getString("VESSEL_NAME"));
                        summary.setPortName(rs.getString("PORT_NAME"));
                        summary.setCurrencyCode(rs.getString("CURRENCY_CODE"));
                        summary.setCurrencyRate(rs.getBigDecimal("CURRENCY_RATE"));
                        summary.setInvDiscount(rs.getBigDecimal("INV_DISCOUNT"));
                        summary.setDiscountAmt(rs.getBigDecimal("DISCOUNT_AMT"));
                        summary.setDiscountPercent(rs.getBigDecimal("DISCOUNT_PERCENT"));
                        summary.setDetails(rs.getString("DETAILS"));
                        summary.setInvAmount(rs.getBigDecimal("INV_AMOUNT"));
                        summary.setTotalGpAmt(rs.getBigDecimal("TOTAL_GP_AMT"));
                        summary.setTotalGpPercent(rs.getBigDecimal("TOTAL_GP_PERCENT"));
                        summary.setDescriptionPrintYn(rs.getString("DESCRIPTION_PRINT_YN"));
                        summary.setDeliveryToAddress(rs.getString("DELIVERY_TO_ADDRESS"));
                    }
                    LoadQuotationSummaryResponse response = new LoadQuotationSummaryResponse();
                    String message = procResult != null ? procResult : "Quotation summary loaded successfully";
                    response.setMessage(message);
                    response.setSuccess(message.toUpperCase().contains("SUCCESS"));
                    response.setQuotationSummary(summary);

                    return response;
                }
            } catch (SQLException ex) {
                throw new CustomException("Error calling PROC_AR_SCH_QTN_LOAD_CUR1: " + ex.getMessage());
            }
        });
    }

    public void callAuthorizationProc(Long transactionPoid, String authorizedId, String userId) {
        String proc = "{call PROC_SCH_INVOICE_AUTHORIZATION(?, ?, ?)}";
        jdbcTemplate.execute((Connection con) -> {
            try (CallableStatement cs = con.prepareCall(proc)) {

                cs.setLong(1, transactionPoid);
                cs.setString(2, authorizedId);
                cs.setString(3, userId);

                cs.execute();
                log.info("callAuthorizationProc: PROC_SCH_INVOICE_AUTHORIZATION completed successfully for transactionPoid={}",
                        transactionPoid);
                return null;
            } catch (SQLException ex) {
                throw new CustomException("Error calling PROC_SCH_INVOICE_AUTHORIZATION: " + ex.getMessage());
            }
        });
    }
}