package com.asg.shipchandling.salesinvoice.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.asg.shipchandling.exceptions.CustomException;
import com.asg.shipchandling.salesinvoice.dto.CreditDetailsDto;
import com.asg.shipchandling.salesinvoice.dto.QuotationSummaryDto;
import com.asg.shipchandling.salesinvoice.dto.QuotationItemDto;
import com.asg.shipchandling.salesinvoice.dto.request.CalculateDiscountCommissionRequest;
import com.asg.shipchandling.salesinvoice.dto.request.LoadQuotationItemsRequest;
import com.asg.shipchandling.salesinvoice.dto.response.CalculateDiscountCommissionResponse;
import com.asg.shipchandling.salesinvoice.dto.response.CalculateDueDateResponse;
import com.asg.shipchandling.salesinvoice.dto.response.CreditDetailsResponse;
import com.asg.shipchandling.salesinvoice.dto.response.LoadQuotationSummaryResponse;
import com.asg.shipchandling.salesinvoice.dto.response.LoadQuotationItemsResponse;
import com.asg.shipchandling.salesinvoice.dto.response.RefreshGpProcResponse;
import com.asg.shipchandling.salesinvoice.dto.response.UnloadQuotationResponse;
import com.asg.shipchandling.salesinvoice.dto.response.ValidationResponse;

import java.sql.CallableStatement;
import java.sql.Connection;
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
                cs.setLong(3, request.getIncentiveAmt() != null ? request.getIncentiveAmt() : null);
                cs.setLong(4, request.getIncentiveAmt2() != null ? request.getIncentiveAmt2() : null);
                cs.setLong(5, request.getIncentiveAmt3() != null ? request.getIncentiveAmt3() : null);
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

                            Object discountPercent = rs.getObject("DISCOUNT_PERCENT");
                            dto.setDiscountPercent(
                                    discountPercent != null ? ((Number) discountPercent).longValue() : null);

                            Long discountAmt = rs.getLong("DISCOUNT_AMT");
                            dto.setDiscountAmt(discountAmt);

                            Object incentivePercent = rs.getObject("INCENTIVE_PERCENT");
                            dto.setIncentivePercent(
                                    incentivePercent != null ? ((Number) incentivePercent).longValue() : null);

                            Long incentivePercent2 = rs.getLong("INCENTIVE_PERCENT2");
                            dto.setIncentivePercent2(incentivePercent2);

                            Long incentivePercent3 = rs.getLong("INCENTIVE_PERCENT3");
                            dto.setIncentivePercent3(incentivePercent3);

                            Long incentiveAmt = rs.getLong("INCENTIVE_AMT");
                            dto.setIncentiveAmt(incentiveAmt);

                            Long incentiveAmt2 = rs.getLong("INCENTIVE_AMT2");
                            dto.setIncentiveAmt2(incentiveAmt2);

                            Long incentiveAmt3 = rs.getLong("INCENTIVE_AMT3");
                            dto.setIncentiveAmt3(incentiveAmt3);

                            Long totalGpAmt = rs.getLong("TOTAL_GP_AMT");
                            dto.setTotalGpAmt(totalGpAmt);

                            Object totalGpPercentObj = rs.getObject("TOTAL_GP_PERCENT");
                            dto.setTotalGpPercent(
                                    totalGpPercentObj != null ? ((Number) totalGpPercentObj).longValue() : null);

                            Long invAmount = rs.getLong("INV_AMOUNT");
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
                cs.setObject(1, userPoid);
                cs.setObject(2, transactionPoid);
                cs.setObject(3, qtnPoid);
                cs.setObject(4, request != null ? request.getInvDiscount() : null);
                cs.setObject(5, request != null ? request.getIncentiveAmt() : null);
                cs.setObject(6, request != null ? request.getIncentiveAmt2() : null);
                cs.setObject(7, request != null ? request.getIncentiveAmt3() : null);
                cs.setObject(8, type);
                cs.setObject(9, request != null ? request.getIncentivePercent() : null);
                cs.setObject(10, request != null ? request.getIncentivePercent2() : null);
                cs.setObject(11, request != null ? request.getIncentivePercent3() : null);
                cs.registerOutParameter(12, Types.VARCHAR);
                cs.registerOutParameter(13, Types.REF_CURSOR);

                cs.execute();

                String procResult = cs.getString(12);
                if (procResult != null && procResult.toUpperCase().contains("ERROR")) {
                    throw new CustomException("PROC_AR_SCH_DIS_COM_CAL failed: " + procResult);
                }

                try (ResultSet rs = (ResultSet) cs.getObject(13)) {
                    if (rs != null && rs.next()) {
                        Object discountPercent = rs.getObject("DISCOUNT_PERCENT");
                        Long discountPercentValue =
                                discountPercent != null ? ((Number) discountPercent).longValue() : null;

                        Long discountAmt = rs.getLong("DISCOUNT_AMT");

                        Object incentivePercentValue = rs.getObject("INCENTIVE_PERCENT");
                        Long incentivePercentFromDb =
                                incentivePercentValue != null ? ((Number) incentivePercentValue).longValue()
                                        : null;

                        Long incentivePercent2Value = rs.getLong("INCENTIVE_PERCENT2");

                        Long incentivePercent3Value = rs.getLong("INCENTIVE_PERCENT3");

                        Long incentiveAmount = rs.getLong("INCENTIVE_AMT");

                        Long incentiveAmount2 = rs.getLong("INCENTIVE_AMT2");

                        Long incentiveAmount3 = rs.getLong("INCENTIVE_AMT3");

                        Long totalGpAmt = rs.getLong("TOTAL_GP_AMT");

                        Object totalGpPercentObj = rs.getObject("TOTAL_GP_PERCENT");
                        Long totalGpPercent =
                                totalGpPercentObj != null ? ((Number) totalGpPercentObj).longValue() : null;

                        Long invAmount = rs.getLong("INV_AMOUNT");
                        
                        RefreshGpProcResponse response = new RefreshGpProcResponse();
                        response.setMessage(procResult != null ? procResult : "Discount/Commission calculated successfully");
                        response.setSuccess(procResult == null || !procResult.toUpperCase().contains("ERROR"));
                        response.setDiscountPercent(discountPercentValue);
                        response.setDiscountAmt(discountAmt);
                        response.setIncentivePercent(incentivePercentFromDb);
                        response.setIncentivePercent2(incentivePercent2Value);
                        response.setIncentivePercent3(incentivePercent3Value);
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

    public CalculateDueDateResponse callCalculateDueDateProc(Long groupPoid, Long companyPoid,
            java.sql.Timestamp transactionDate,
            java.sql.Timestamp dueDate, Long creditDays, String calculationType, Long customerPoid) {
        String proc = "{call PROC_CALC_DUEDAYS(?, ?, ?, ? ,?, ?, ?, ?)}";
        return jdbcTemplate.execute((Connection con) -> {
            try (CallableStatement cs = con.prepareCall(proc)) {

                cs.setTimestamp(1, transactionDate);
                cs.setTimestamp(2, dueDate);
                cs.setLong(3, creditDays);
                cs.setString(4, calculationType);
                cs.setLong(5, customerPoid);
                cs.registerOutParameter(6, Types.DATE);
                cs.registerOutParameter(7, Types.BIGINT);
                cs.registerOutParameter(8, Types.VARCHAR);

                cs.execute();

                java.sql.Timestamp resultDueDate = cs.getTimestamp(6);
                Long dueDays = cs.getLong(7);
                String procResult = cs.getString(8);

                if (procResult != null && procResult.toUpperCase().contains("ERROR")) {
                    throw new CustomException("PROC_AR_SCH_GP_CALC failed: " + procResult);
                }

                CalculateDueDateResponse response = new CalculateDueDateResponse();
                response.setMessage(procResult);
                response.setSuccess(procResult.contains("True"));
                response.setDueDate(resultDueDate);
                response.setDueDays(dueDays);

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
                cs.setLong(4, request.getInvDiscount() != null ? request.getInvDiscount() : null);
                cs.setLong(5, request.getIncentiveAmt() != null ? request.getIncentiveAmt() : null);
                cs.setLong(6, request.getIncentiveAmt2() != null ? request.getIncentiveAmt2() : null);
                cs.setLong(7, request.getIncentiveAmt3() != null ? request.getIncentiveAmt3() : null);
                cs.setString(8, request.getType() != null ? request.getType() : "Amount");
                cs.setLong(9, request.getIncentivePercent() != null ? request.getIncentivePercent() : null);
                cs.setLong(10, request.getIncentivePercent2() != null ? request.getIncentivePercent2() : null);
                cs.setLong(11, request.getIncentivePercent3() != null ? request.getIncentivePercent3() : null);
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

                            Object discountPercent = rs.getObject("DISCOUNT_PERCENT");
                            dto.setDiscountPercent(
                                    discountPercent != null ? ((Number) discountPercent).longValue() : null);

                            Long discountAmt = rs.getLong("DISCOUNT_AMT");
                            dto.setDiscountAmt(discountAmt);

                            Object incentivePercent = rs.getObject("INCENTIVE_PERCENT");
                            dto.setIncentivePercent(
                                    incentivePercent != null ? ((Number) incentivePercent).longValue() : null);

                            Long incentivePercent2 = rs.getLong("INCENTIVE_PERCENT2");
                            dto.setIncentivePercent2(incentivePercent2);

                            Long incentivePercent3 = rs.getLong("INCENTIVE_PERCENT3");
                            dto.setIncentivePercent3(incentivePercent3);

                            Long incentiveAmt = rs.getLong("INCENTIVE_AMT");
                            dto.setIncentiveAmt(incentiveAmt);

                            Long incentiveAmt2 = rs.getLong("INCENTIVE_AMT2");
                            dto.setIncentiveAmt2(incentiveAmt2);

                            Long incentiveAmt3 = rs.getLong("INCENTIVE_AMT3");
                            dto.setIncentiveAmt3(incentiveAmt3);

                            Long totalGpAmt = rs.getLong("TOTAL_GP_AMT");
                            dto.setTotalGpAmt(totalGpAmt);

                            Object totalGpPercentObj = rs.getObject("TOTAL_GP_PERCENT");
                            dto.setTotalGpPercent(
                                    totalGpPercentObj != null ? ((Number) totalGpPercentObj).longValue() : null);

                            Long invAmount = rs.getLong("INV_AMOUNT");
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
                cs.setLong(4, request.getInvDiscount() != null ? request.getInvDiscount() : null);
                cs.setLong(5, request.getIncentiveAmt() != null ? request.getIncentiveAmt() : null);
                cs.setLong(6, request.getIncentiveAmt2() != null ? request.getIncentiveAmt2() : null);
                cs.setLong(7, request.getIncentiveAmt3() != null ? request.getIncentiveAmt3() : null);
                cs.setString(8, request.getType() != null ? request.getType() : "Amount");
                cs.setLong(9, request.getIncentivePercent() != null ? request.getIncentivePercent() : null);
                cs.setLong(10, request.getIncentivePercent2() != null ? request.getIncentivePercent2() : null);
                cs.setLong(11, request.getIncentivePercent3() != null ? request.getIncentivePercent3() : null);
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

    public CreditDetailsResponse callLoadCreditDetailsProc(Long groupPoid, Long companyPoid, Long customerPoid,
            String docId, Long docKeyPoid, java.sql.Timestamp docDate, String partyType, Long partyPoid) {
        String proc = "{call PROC_LOAD_CREDIT_DETAILS(?, ?)}";
        return jdbcTemplate.execute((Connection con) -> {
            try (CallableStatement cs = con.prepareCall(proc)) {

                cs.setLong(1, groupPoid);
                cs.setLong(2, companyPoid);
                cs.setLong(3, customerPoid);
                cs.setString(4, docId);
                cs.setLong(5, docKeyPoid);
                cs.setTimestamp(6, docDate);
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

                    List<CreditDetailsDto> items = new ArrayList<>();
                    if (rs != null) {
                        while (rs.next()) {
                            CreditDetailsDto dto = new CreditDetailsDto();

                            Long creditPeriod = rs.getLong("CREDIT_PERIOD");
                            dto.setCreditPeriod(creditPeriod);

                            java.sql.Timestamp dueDate = rs.getTimestamp("DISCOUNT_AMT");
                            dto.setDueDate(dueDate);

                            items.add(dto);
                        }
                    }
                    CreditDetailsResponse response = new CreditDetailsResponse();
                    response.setMessage("Quotation items loaded successfully");
                    response.setCreditDetails(items);
                   
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