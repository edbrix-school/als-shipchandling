package com.asg.shipchandling.commonlov.repository;

import com.asg.shipchandling.commonlov.dto.LovItem;
import com.asg.shipchandling.commonlov.dto.LovResponse;
import lombok.extern.slf4j.Slf4j;
import oracle.jdbc.OracleTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
@Slf4j
public class LovRepository {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    public LovResponse getLovList(String lovName, Long docKeyPoid, String filterValue, String filterField) {
        try {
            final String sql = "BEGIN PROC_LOV_GETLIST(?,?,?,?,?,?,?); END;";
            log.info("Executing PROC_LOV_GETLIST for lovName={} docKeyPoid={} filterValue={} filterField={}", lovName, docKeyPoid, filterValue, filterField);
            return jdbcTemplate.execute((Connection con) -> {
                try (CallableStatement cs = con.prepareCall(sql)) {
                    cs.setLong(1, 1);
                    cs.setLong(2, 1);
                    cs.setLong(3, 1);
                    cs.setString(4, lovName);
                    cs.setString(5, filterField != null ? filterField : "");
                    cs.setString(6, filterValue != null ? filterValue : "");
                    cs.registerOutParameter(7, OracleTypes.CURSOR);
                    cs.execute();

                    List<LovItem> items = new ArrayList<>();
                    try (ResultSet rs = (ResultSet) cs.getObject(7)) {
                        if (rs != null) {
                            while (rs.next()) {
                                Long poid = rs.getLong("POID");
                                String code = rs.getString("CODE");
                                String description = rs.getString("DESCRIPTION");
                                items.add(new LovItem(poid, code, description));
                            }
                        }
                    }
                    LovResponse response = new LovResponse(items);
                    log.info("PROC_LOV_GETLIST completed for lovName={} itemsFetched={}", lovName, items.size());
                    return response;
                } catch (SQLException ex) {
                    log.error("Error executing PROC_LOV_GETLIST for lovName={}: {}", lovName, ex.getMessage(), ex);
                    throw new RuntimeException("Error fetching LOV list: " + ex.getMessage(), ex);
                }
            });

        }catch (Exception e){
            log.error("Unexpected error fetching LOV list for lovName={}", lovName, e);
            throw new RuntimeException();

        }
    }
}
