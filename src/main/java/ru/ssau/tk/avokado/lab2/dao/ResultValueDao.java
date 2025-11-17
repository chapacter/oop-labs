package ru.ssau.tk.avokado.lab2.dao;

import ru.ssau.tk.avokado.lab2.entity.ResultValue;
import java.sql.*;

public class ResultValueDao extends AbstractJdbcDao<ResultValue, Long> {
    public ResultValueDao() {
        super("result_values", rs -> {
            ResultValue rv = new ResultValue();
            rv.setId(rs.getLong("id"));
            rv.setProcessedFunctionId(rs.getLong("processed_function_id"));
            rv.setKey(rs.getString("key"));
            rv.setValue(rs.getDouble("value"));
            rv.setValueType(rs.getString("value_type"));
            return rv;
        });
    }

    @Override protected String insertSql() { return "INSERT INTO result_values (processed_function_id,key,value,value_type) VALUES (?,?,?,?)"; }
    @Override protected String updateSql() { return "UPDATE result_values SET processed_function_id=?, key=?, value=?, value_type=? WHERE id=?"; }

    @Override protected void setInsertParameters(PreparedStatement ps, ResultValue rv) throws SQLException {
        ps.setLong(1, rv.getProcessedFunctionId());
        ps.setString(2, rv.getKey());
        ps.setDouble(3, rv.getValue());
        ps.setString(4, rv.getValueType());
    }

    @Override protected void setUpdateParameters(PreparedStatement ps, ResultValue rv) throws SQLException {
        setInsertParameters(ps, rv);
        ps.setLong(5, rv.getId());
    }

    @Override protected void setId(ResultValue rv, Long id) { rv.setId(id); }
}