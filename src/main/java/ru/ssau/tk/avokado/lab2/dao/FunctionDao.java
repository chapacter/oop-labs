package ru.ssau.tk.avokado.lab2.dao;

import ru.ssau.tk.avokado.lab2.entity.Function;
import java.sql.*;

public class FunctionDao extends AbstractJdbcDao<Function, Long> {
    public FunctionDao() {
        super("functions", rs -> {
            Function f = new Function();
            f.setId(rs.getLong("id"));
            f.setName(rs.getString("name"));
            f.setDescription(rs.getString("description"));
            f.setUserId(rs.getLong("user_id"));
            Timestamp ts = rs.getTimestamp("created_at");
            if (ts != null) f.setCreatedAt(ts.toInstant().atZone(java.time.ZoneId.systemDefault()));
            return f;
        });
    }

    @Override protected String insertSql() { return "INSERT INTO functions (name,description,user_id) VALUES (?,?,?)"; }
    @Override protected String updateSql() { return "UPDATE functions SET name=?, description=? WHERE id=?"; }

    @Override protected void setInsertParameters(PreparedStatement ps, Function f) throws SQLException {
        ps.setString(1, f.getName());
        ps.setString(2, f.getDescription());
        ps.setLong(3, f.getUserId());
    }

    @Override protected void setUpdateParameters(PreparedStatement ps, Function f) throws SQLException {
        ps.setString(1, f.getName());
        ps.setString(2, f.getDescription());
        ps.setLong(3, f.getId());
    }

    @Override protected void setId(Function f, Long id) { f.setId(id); }
}