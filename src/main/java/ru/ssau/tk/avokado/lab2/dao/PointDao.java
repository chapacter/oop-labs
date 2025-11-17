package ru.ssau.tk.avokado.lab2.dao;

import ru.ssau.tk.avokado.lab2.entity.Point;
import java.sql.*;

public class PointDao extends AbstractJdbcDao<Point, Long> {
    public PointDao() {
        super("points", rs -> {
            Point p = new Point();
            p.setId(rs.getLong("id"));
            p.setFunctionId(rs.getLong("function_id"));
            p.setXValue(rs.getDouble("x_value"));
            p.setYValue(rs.getDouble("y_value"));
            p.setPointOrder(rs.getInt("point_order"));
            return p;
        });
    }

    @Override protected String insertSql() { return "INSERT INTO points (function_id,x_value,y_value,point_order) VALUES (?,?,?,?)"; }
    @Override protected String updateSql() { return "UPDATE points SET function_id=?, x_value=?, y_value=?, point_order=? WHERE id=?"; }

    @Override protected void setInsertParameters(PreparedStatement ps, Point p) throws SQLException {
        ps.setLong(1, p.getFunctionId());
        ps.setDouble(2, p.getXValue());
        ps.setDouble(3, p.getYValue());
        ps.setInt(4, p.getPointOrder());
    }

    @Override protected void setUpdateParameters(PreparedStatement ps, Point p) throws SQLException {
        ps.setLong(1, p.getFunctionId());
        ps.setDouble(2, p.getXValue());
        ps.setDouble(3, p.getYValue());
        ps.setInt(4, p.getPointOrder());
        ps.setLong(5, p.getId());
    }

    @Override protected void setId(Point p, Long id) { p.setId(id); }
}