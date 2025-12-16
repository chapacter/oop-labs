package ru.ssau.tk.avokado.lab2.dao;

import ru.ssau.tk.avokado.lab2.DatabaseConnection;
import ru.ssau.tk.avokado.lab2.dto.TabulatedFuncDto;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcTabulatedFuncDao implements TabulatedFuncDao {
    private final Connection connection;

    public JdbcTabulatedFuncDao(Connection connection) {
        this.connection = connection;
    }

    public JdbcTabulatedFuncDao() {
        try {
            this.connection = DatabaseConnection.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException("Error getting database connection", e);
        }
    }

    @Override
    public Optional<TabulatedFuncDto> findById(Long id) {
        String sql = "SELECT id, func_id, x_val, y_val FROM tabulated_func WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return Optional.of(mapResultSetToDto(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding tabulated function by id: " + id, e);
        }
        return Optional.empty();
    }

    @Override
    public List<TabulatedFuncDto> findAll() {
        String sql = "SELECT id, func_id, x_val, y_val FROM tabulated_func";
        List<TabulatedFuncDto> tabulatedFuncs = new ArrayList<>();
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                tabulatedFuncs.add(mapResultSetToDto(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding all tabulated functions", e);
        }
        return tabulatedFuncs;
    }

    @Override
    public List<TabulatedFuncDto> findByFuncId(Long funcId) {
        String sql = "SELECT id, func_id, x_val, y_val FROM tabulated_func WHERE func_id = ?";
        List<TabulatedFuncDto> tabulatedFuncs = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, funcId);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                tabulatedFuncs.add(mapResultSetToDto(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding tabulated functions by func id: " + funcId, e);
        }
        return tabulatedFuncs;
    }

    @Override
    public List<TabulatedFuncDto> findByXVal(Double xVal) {
        String sql = "SELECT id, func_id, x_val, y_val FROM tabulated_func WHERE x_val = ?";
        List<TabulatedFuncDto> tabulatedFuncs = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDouble(1, xVal);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                tabulatedFuncs.add(mapResultSetToDto(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding tabulated functions by x value: " + xVal, e);
        }
        return tabulatedFuncs;
    }

    @Override
    public List<TabulatedFuncDto> findByYVal(Double yVal) {
        String sql = "SELECT id, func_id, x_val, y_val FROM tabulated_func WHERE y_val = ?";
        List<TabulatedFuncDto> tabulatedFuncs = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDouble(1, yVal);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                tabulatedFuncs.add(mapResultSetToDto(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding tabulated functions by y value: " + yVal, e);
        }
        return tabulatedFuncs;
    }

    @Override
    public List<TabulatedFuncDto> findByXValBetween(Double x1, Double x2) {
        String sql = "SELECT id, func_id, x_val, y_val FROM tabulated_func WHERE x_val BETWEEN ? AND ?";
        List<TabulatedFuncDto> tabulatedFuncs = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDouble(1, Math.min(x1, x2));
            statement.setDouble(2, Math.max(x1, x2));
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                tabulatedFuncs.add(mapResultSetToDto(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding tabulated functions by x value range: " + x1 + " to " + x2, e);
        }
        return tabulatedFuncs;
    }

    @Override
    public List<TabulatedFuncDto> findByYValBetween(Double y1, Double y2) {
        String sql = "SELECT id, func_id, x_val, y_val FROM tabulated_func WHERE y_val BETWEEN ? AND ?";
        List<TabulatedFuncDto> tabulatedFuncs = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDouble(1, Math.min(y1, y2));
            statement.setDouble(2, Math.max(y1, y2));
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                tabulatedFuncs.add(mapResultSetToDto(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding tabulated functions by y value range: " + y1 + " to " + y2, e);
        }
        return tabulatedFuncs;
    }

    @Override
    public List<TabulatedFuncDto> findByFuncIdsIn(List<Long> funcIds) {
        if (funcIds == null || funcIds.isEmpty()) {
            return new ArrayList<>();
        }

        String sql = "SELECT id, func_id, x_val, y_val FROM tabulated_func WHERE func_id IN (" +
                String.join(",", funcIds.stream().map(id -> "?").toArray(String[]::new)) + ")";

        List<TabulatedFuncDto> tabulatedFuncs = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < funcIds.size(); i++) {
                statement.setLong(i + 1, funcIds.get(i));
            }
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                tabulatedFuncs.add(mapResultSetToDto(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding tabulated functions by func ids: " + funcIds, e);
        }
        return tabulatedFuncs;
    }

    @Override
    public Long save(TabulatedFuncDto tabulatedFunc) {
        String sql = "INSERT INTO tabulated_func (func_id, x_val, y_val) VALUES (?, ?, ?) RETURNING id";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, tabulatedFunc.getFuncId());
            statement.setDouble(2, tabulatedFunc.getXVal());
            statement.setDouble(3, tabulatedFunc.getYVal());
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getLong("id");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error saving tabulated function", e);
        }
        return null;
    }

    @Override
    public boolean update(TabulatedFuncDto tabulatedFunc) {
        String sql = "UPDATE tabulated_func SET func_id = ?, x_val = ?, y_val = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, tabulatedFunc.getFuncId());
            statement.setDouble(2, tabulatedFunc.getXVal());
            statement.setDouble(3, tabulatedFunc.getYVal());
            statement.setLong(4, tabulatedFunc.getId());
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating tabulated function: " + tabulatedFunc.getId(), e);
        }
    }

    @Override
    public boolean delete(Long id) {
        String sql = "DELETE FROM tabulated_func WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting tabulated function: " + id, e);
        }
    }

    @Override
    public boolean deleteByFuncId(Long funcId) {
        String sql = "DELETE FROM tabulated_func WHERE func_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, funcId);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting tabulated functions by func id: " + funcId, e);
        }
    }

    @Override
    public int countByFuncId(Long funcId) {
        String sql = "SELECT COUNT(*) FROM tabulated_func WHERE func_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, funcId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error counting tabulated functions by func id: " + funcId, e);
        }
        return 0;
    }

    private TabulatedFuncDto mapResultSetToDto(ResultSet resultSet) throws SQLException {
        return new TabulatedFuncDto(
                resultSet.getLong("id"),
                resultSet.getLong("func_id"),
                resultSet.getDouble("x_val"),
                resultSet.getDouble("y_val")
        );
    }
}