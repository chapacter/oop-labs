package ru.ssau.tk.avokado.lab2.dao;

import ru.ssau.tk.avokado.lab2.entity.User;
import java.sql.*;

public abstract class UserDao extends AbstractJdbcDao<User, Long> {
    public UserDao() {
        super("users", rs -> {
            User u = new User();
            u.setId(rs.getLong("id"));
            u.setUsername(rs.getString("username"));
            u.setEmail(rs.getString("email"));
            u.setPasswordHash(rs.getString("password_hash"));
            Timestamp ts = rs.getTimestamp("created_at");
            if (ts != null) u.setCreatedAt(ts.toInstant().atZone(java.time.ZoneId.systemDefault()));
            return u;
        });
    }
    @Override protected String insertSql() { return "INSERT INTO users (username,email,password_hash) VALUES (?,?,?)"; }
    @Override protected String updateSql() { return "UPDATE users SET username=?, email=?, password_hash=? WHERE id=?"; }
    @Override protected void setInsertParameters(PreparedStatement ps, User u) throws SQLException {
        ps.setString(1, u.getUsername()); ps.setString(2, u.getEmail()); ps.setString(3, u.getPasswordHash());
    }
    @Override protected void setUpdateParameters(PreparedStatement ps, User u) throws SQLException {
        setInsertParameters(ps, u); ps.setLong(4, u.getId());
    }
    @Override protected void setId(User u, Long id) { u.setId(id); }

    public abstract Connection getConnection() throws Exception;
}