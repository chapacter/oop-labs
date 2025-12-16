package ru.ssau.tk.avokado.lab2.dao;

import ru.ssau.tk.avokado.lab2.DatabaseConnection;
import ru.ssau.tk.avokado.lab2.dto.ResultValueDto;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcResultValueDao implements ResultValueDao {
    private final Connection connection;

    public JdbcResultValueDao(Connection connection) {
        this.connection = connection;
    }

    public JdbcResultValueDao() {
        try {
            this.connection = DatabaseConnection.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException("Error getting database connection", e);
        }
    }

    @Override
    public Optional<ResultValueDto> findById(Long id) {
        String sql = "SELECT id, processed_function_id, point_index, x, y FROM result_values WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return Optional.of(mapResultSetToDto(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding result value by id: " + id, e);
        }
        return Optional.empty();
    }

    @Override
    public List<ResultValueDto> findAll() {
        String sql = "SELECT id, processed_function_id, point_index, x, y FROM result_values";
        List<ResultValueDto> resultValues = new ArrayList<>();
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                resultValues.add(mapResultSetToDto(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding all result values", e);
        }
        return resultValues;
    }

    @Override
    public List<ResultValueDto> findByProcessedFunctionId(Long processedFunctionId) {
        String sql = "SELECT id, processed_function_id, point_index, x, y FROM result_values WHERE processed_function_id = ?";
        List<ResultValueDto> resultValues = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, processedFunctionId);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                resultValues.add(mapResultSetToDto(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding result values by processed function id: " + processedFunctionId, e);
        }
        return resultValues;
    }

    @Override
    public List<ResultValueDto> findByXBetween(Double x1, Double x2) {
        String sql = "SELECT id, processed_function_id, point_index, x, y FROM result_values WHERE x BETWEEN ? AND ?";
        List<ResultValueDto> resultValues = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDouble(1, Math.min(x1, x2));
            statement.setDouble(2, Math.max(x1, x2));
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                resultValues.add(mapResultSetToDto(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding result values by x range: " + x1 + " to " + x2, e);
        }
        return resultValues;
    }

    @Override
    public List<ResultValueDto> findByYBetween(Double y1, Double y2) {
        String sql = "SELECT id, processed_function_id, point_index, x, y FROM result_values WHERE y BETWEEN ? AND ?";
        List<ResultValueDto> resultValues = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDouble(1, Math.min(y1, y2));
            statement.setDouble(2, Math.max(y1, y2));
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                resultValues.add(mapResultSetToDto(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding result values by y range: " + y1 + " to " + y2, e);
        }
        return resultValues;
    }

    @Override
    public List<ResultValueDto> findByPointIndex(Integer pointIndex) {
        String sql = "SELECT id, processed_function_id, point_index, x, y FROM result_values WHERE point_index = ?";
        List<ResultValueDto> resultValues = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, pointIndex);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                resultValues.add(mapResultSetToDto(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding result values by point index: " + pointIndex, e);
        }
        return resultValues;
    }

    @Override
    public List<ResultValueDto> findByProcessedFunctionIdsIn(List<Long> processedFunctionIds) {
        if (processedFunctionIds == null || processedFunctionIds.isEmpty()) {
            return new ArrayList<>();
        }

        String sql = "SELECT id, processed_function_id, point_index, x, y FROM result_values WHERE processed_function_id IN (" +
                String.join(",", processedFunctionIds.stream().map(id -> "?").toArray(String[]::new)) + ")";

        List<ResultValueDto> resultValues = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < processedFunctionIds.size(); i++) {
                statement.setLong(i + 1, processedFunctionIds.get(i));
            }
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                resultValues.add(mapResultSetToDto(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding result values by processed function ids: " + processedFunctionIds, e);
        }
        return resultValues;
    }

    @Override
    public Long save(ResultValueDto resultValue) {
        String sql = "INSERT INTO result_values (processed_function_id, point_index, x, y) VALUES (?, ?, ?, ?) RETURNING id";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, resultValue.getProcessedFunctionId());
            statement.setInt(2, resultValue.getPointIndex());
            statement.setDouble(3, resultValue.getX());
            statement.setDouble(4, resultValue.getY());
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getLong("id");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error saving result value", e);
        }
        return null;
    }

    @Override
    public boolean update(ResultValueDto resultValue) {
        String sql = "UPDATE result_values SET processed_function_id = ?, point_index = ?, x = ?, y = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, resultValue.getProcessedFunctionId());
            statement.setInt(2, resultValue.getPointIndex());
            statement.setDouble(3, resultValue.getX());
            statement.setDouble(4, resultValue.getY());
            statement.setLong(5, resultValue.getId());
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating result value: " + resultValue.getId(), e);
        }
    }

    @Override
    public boolean delete(Long id) {
        String sql = "DELETE FROM result_values WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting result value: " + id, e);
        }
    }

    @Override
    public boolean deleteByProcessedFunctionId(Long processedFunctionId) {
        String sql = "DELETE FROM result_values WHERE processed_function_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, processedFunctionId);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting result values by processed function id: " + processedFunctionId, e);
        }
    }

    @Override
    public int countByProcessedFunctionId(Long processedFunctionId) {
        String sql = "SELECT COUNT(*) FROM result_values WHERE processed_function_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, processedFunctionId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error counting result values by processed function id: " + processedFunctionId, e);
        }
        return 0;
    }

    private ResultValueDto mapResultSetToDto(ResultSet resultSet) throws SQLException {
        return new ResultValueDto(
                resultSet.getLong("id"),
                resultSet.getLong("processed_function_id"),
                resultSet.getInt("point_index"),
                resultSet.getDouble("x"),
                resultSet.getDouble("y")
        );
    }
}