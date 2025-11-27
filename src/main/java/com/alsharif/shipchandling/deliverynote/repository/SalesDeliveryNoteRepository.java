package com.alsharif.shipchandling.deliverynote.repository;

import java.sql.*;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.alsharif.shipchandling.exceptions.CustomException;

@Repository
public class SalesDeliveryNoteRepository {

    private static final Logger log = LoggerFactory.getLogger(SalesDeliveryNoteRepository.class);

    @Autowired
    private DataSource dataSource;

    public boolean callSalesSCDNCustomerValidateProc(Long customerPoid,
            Long transactionPoid) {
        String proc = "{call PROC_SALES_SCDN_CUST_VALIDATE(?, ?, ?)}";
        try (Connection conn = dataSource.getConnection();
                CallableStatement cs = conn.prepareCall(proc)) {

            cs.setLong(1, transactionPoid);
            cs.setLong(2, customerPoid);
            cs.registerOutParameter(3, Types.VARCHAR);

            cs.execute();
            String procResult = cs.getString(3);

            if (procResult != null && procResult.toUpperCase().contains("ERROR")) {
                throw new CustomException("PROC_SALES_SCDN_CUST_VALIDATE failed: " + procResult);
            }
            return procResult.equalsIgnoreCase("true");
        } catch (SQLException ex) {
            throw new CustomException("Error calling PROC_SALES_SCDN_CUST_VALIDATE: " + ex.getMessage());
        }
    }

    public String callUpdateDeletedDetailsProc(Long groupPoid, Long companyPoid, String userId,
            Long transactionPoid) {
        String proc = "{call PROC_DN_UPDATE_DELETED_DTLSQH(?, ?, ?, ?, ?)}";
        try (Connection conn = dataSource.getConnection();
                CallableStatement cs = conn.prepareCall(proc)) {

            cs.setLong(1, groupPoid);
            cs.setLong(2, companyPoid);
            cs.setString(3, userId);
            cs.setLong(4, transactionPoid);
            cs.registerOutParameter(5, Types.VARCHAR);
            
            cs.execute();

            String procResult = cs.getString(5);
            if (procResult != null && procResult.toUpperCase().contains("ERROR")) {
                throw new CustomException("PROC_DN_UPDATE_DELETED_DTLSQH failed: " + procResult);
            }
            return procResult;
        } catch (SQLException ex) {
            throw new CustomException("Error calling PROC_DN_UPDATE_DELETED_DTLSQH: " + ex.getMessage());
        }
    }
}
