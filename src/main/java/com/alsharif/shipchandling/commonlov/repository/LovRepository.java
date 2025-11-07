package com.alsharif.shipchandling.commonlov.repository;

import com.alsharif.shipchandling.commonlov.dto.LovItem;
import com.alsharif.shipchandling.commonlov.dto.LovResponse;
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
public class LovRepository {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    public LovResponse getLovList(String lovName, Long docKeyPoid, String filterValue) {
        try {
            final String sql = "BEGIN PROC_LOV_GETLIST(?,?,?,?,?,?,?); END;";

            return jdbcTemplate.execute((Connection con) -> {
                try (CallableStatement cs = con.prepareCall(sql)) {
                    cs.setLong(1, 1);
                    cs.setLong(2, 1);
                    cs.setLong(3, 1);
                    cs.setString(4, lovName);
                    if (docKeyPoid != null) {
                        cs.setString(5, "");
                    } else {
                        cs.setObject(5, null);
                    }
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
                    return new LovResponse(items);
                } catch (SQLException ex) {
                    throw new RuntimeException("Error fetching LOV list: " + ex.getMessage(), ex);
                }
            });

        }catch (Exception e){
            throw new RuntimeException();

        }
    }
}
