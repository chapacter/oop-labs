package ru.ssau.tk.avokado.lab2.dao;

import ru.ssau.tk.avokado.lab2.entity.Operation;
import java.sql.*;

public class OperationDao extends AbstractJdbcDao<Operation, Long> {
    public OperationDao() {
        super("operations", rs -> {
            Operation o = new Operation();
            o.setId(rs.getLong("id"));
            o.setName(rs.getString("name"));
            o.setDescription(rs.getString("description"));
            return o;
        });
    }

    @Override protected String insertSql() { return "INSERT INTO operations (name,description) VALUES (?,?)"; }
    @Override protected String updateSql() { return "UPDATE operations SET name=?, description=? WHERE id=?"; }

    @Override protected void setInsertParameters(PreparedStatement ps, Operation o) throws SQLException {
        ps.setString(1, o.getName());
        ps.setString(2, o.getDescription());
    }

    @Override protected void setUpdateParameters(PreparedStatement ps, Operation o) throws SQLException {
        ps.setString(1, o.getName());
        ps.setString(2, o.getDescription());
        ps.setLong(3, o.getId());
    }

    @Override protected void setId(Operation o, Long id) { o.setId(id); }
}