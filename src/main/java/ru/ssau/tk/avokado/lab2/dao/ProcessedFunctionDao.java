package ru.ssau.tk.avokado.lab2.dao;

import ru.ssau.tk.avokado.lab2.entity.ProcessedFunction;
import java.sql.*;

public class ProcessedFunctionDao extends AbstractJdbcDao<ProcessedFunction, Long> {
    public ProcessedFunctionDao() {
        super("processed_functions", rs -> {
            ProcessedFunction pf = new ProcessedFunction();
            pf.setId(rs.getLong("id"));
            pf.setOriginalFunctionId(rs.getLong("original_function_id"));
            pf.setOperationId(rs.getLong("operation_id"));
            pf.setResultingFunctionId(rs.getLong("resulting_function_id"));
            pf.setParameters(rs.getString("parameters"));
            Timestamp ts = rs.getTimestamp("processed_at");
            if (ts != null) pf.setProcessedAt(ts.toInstant().atZone(java.time.ZoneId.systemDefault()));
            return pf;
        });
    }

    @Override protected String insertSql() { return "INSERT INTO processed_functions (original_function_id,operation_id,resulting_function_id,parameters) VALUES (?,?,?,?)"; }
    @Override protected String updateSql() { return "UPDATE processed_functions SET original_function_id=?, operation_id=?, resulting_function_id=?, parameters=? WHERE id=?"; }

    @Override protected void setInsertParameters(PreparedStatement ps, ProcessedFunction pf) throws SQLException {
        ps.setLong(1, pf.getOriginalFunctionId());
        ps.setLong(2, pf.getOperationId());
        ps.setLong(3, pf.getResultingFunctionId());
        ps.setString(4, pf.getParameters());
    }

    @Override protected void setUpdateParameters(PreparedStatement ps, ProcessedFunction pf) throws SQLException {
        setInsertParameters(ps, pf);
        ps.setLong(5, pf.getId());
    }

    @Override protected void setId(ProcessedFunction pf, Long id) { pf.setId(id); }
}