package ru.ssau.tk.avokado.lab2.dao;

import ru.ssau.tk.avokado.lab2.DatabaseConnection;
import ru.ssau.tk.avokado.lab2.dto.ProcessedFunctionDto;

import java.sql.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcProcessedFunctionDao implements ProcessedFunctionDao {
    private final Connection connection;

    public JdbcProcessedFunctionDao(Connection connection) {
        this.connection = connection;
    }

    public JdbcProcessedFunctionDao() {
        try {
            this.connection = DatabaseConnection.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException("Error getting database connection", e);
        }
    }

    @Override
    public Optional<ProcessedFunctionDto> findById(Long id) {
        String sql = "SELECT id, function_id, operation_id, result_summary, created_at FROM processed_functions WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return Optional.of(mapResultSetToDto(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding processed function by id: " + id, e);
        }
        return Optional.empty();
    }

    @Override
    public List<ProcessedFunctionDto> findAll() {
        String sql = "SELECT id, function_id, operation_id, result_summary, created_at FROM processed_functions";
        List<ProcessedFunctionDto> processedFunctions = new ArrayList<>();
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                processedFunctions.add(mapResultSetToDto(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding all processed functions", e);
        }
        return processedFunctions;
    }

    @Override
    public List<ProcessedFunctionDto> findByFunctionId(Long functionId) {
        String sql = "SELECT id, function_id, operation_id, result_summary, created_at FROM processed_functions WHERE function_id = ?";
        List<ProcessedFunctionDto> processedFunctions = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, functionId);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                processedFunctions.add(mapResultSetToDto(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding processed functions by function id: " + functionId, e);
        }
        return processedFunctions;
    }

    @Override
    public List<ProcessedFunctionDto> findByOperationId(Long operationId) {
        String sql = "SELECT id, function_id, operation_id, result_summary, created_at FROM processed_functions WHERE operation_id = ?";
        List<ProcessedFunctionDto> processedFunctions = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, operationId);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                processedFunctions.add(mapResultSetToDto(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding processed functions by operation id: " + operationId, e);
        }
        return processedFunctions;
    }

    @Override
    public List<ProcessedFunctionDto> findByResultSummaryLike(String resultSummaryPattern) {
        String sql = "SELECT id, function_id, operation_id, result_summary, created_at FROM processed_functions WHERE result_summary LIKE ?";
        List<ProcessedFunctionDto> processedFunctions = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, "%" + resultSummaryPattern + "%");
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                processedFunctions.add(mapResultSetToDto(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding processed functions by result summary pattern: " + resultSummaryPattern, e);
        }
        return processedFunctions;
    }

    @Override
    public List<ProcessedFunctionDto> findByCreatedAtBetween(ZonedDateTime start, ZonedDateTime end) {
        String sql = "SELECT id, function_id, operation_id, result_summary, created_at FROM processed_functions WHERE created_at BETWEEN ? AND ?";
        List<ProcessedFunctionDto> processedFunctions = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setTimestamp(1, Timestamp.from(start.toInstant()));
            statement.setTimestamp(2, Timestamp.from(end.toInstant()));
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                processedFunctions.add(mapResultSetToDto(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding processed functions by created at range: " + start + " to " + end, e);
        }
        return processedFunctions;
    }

    @Override
    public Long save(ProcessedFunctionDto processedFunction) {
        String sql = "INSERT INTO processed_functions (function_id, operation_id, result_summary, created_at) VALUES (?, ?, ?, ?) RETURNING id";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, processedFunction.getFunctionId());
            statement.setLong(2, processedFunction.getOperationId());
            statement.setString(3, processedFunction.getResultSummary());
            statement.setTimestamp(4, processedFunction.getCreatedAt() != null ?
                    Timestamp.from(processedFunction.getCreatedAt().toInstant()) :
                    Timestamp.from(ZonedDateTime.now().toInstant()));
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getLong("id");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error saving processed function", e);
        }
        return null;
    }

    @Override
    public boolean update(ProcessedFunctionDto processedFunction) {
        String sql = "UPDATE processed_functions SET function_id = ?, operation_id = ?, result_summary = ?, created_at = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, processedFunction.getFunctionId());
            statement.setLong(2, processedFunction.getOperationId());
            statement.setString(3, processedFunction.getResultSummary());
            statement.setTimestamp(4, processedFunction.getCreatedAt() != null ?
                    Timestamp.from(processedFunction.getCreatedAt().toInstant()) :
                    Timestamp.from(ZonedDateTime.now().toInstant()));
            statement.setLong(5, processedFunction.getId());
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating processed function: " + processedFunction.getId(), e);
        }
    }

    @Override
    public boolean updateResultSummary(Long id, String resultSummary) {
        String sql = "UPDATE processed_functions SET result_summary = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, resultSummary);
            statement.setLong(2, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating processed function result summary: " + id, e);
        }
    }

    @Override
    public boolean delete(Long id) {
        String sql = "DELETE FROM processed_functions WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting processed function: " + id, e);
        }
    }

    @Override
    public boolean deleteByFunctionId(Long functionId) {
        String sql = "DELETE FROM processed_functions WHERE function_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, functionId);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting processed functions by function id: " + functionId, e);
        }
    }

    @Override
    public boolean deleteByOperationId(Long operationId) {
        String sql = "DELETE FROM processed_functions WHERE operation_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, operationId);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting processed functions by operation id: " + operationId, e);
        }
    }

    private ProcessedFunctionDto mapResultSetToDto(ResultSet resultSet) throws SQLException {
        return new ProcessedFunctionDto(
                resultSet.getLong("id"),
                resultSet.getLong("function_id"),
                resultSet.getLong("operation_id"),
                resultSet.getString("result_summary"),
                resultSet.getTimestamp("created_at").toInstant().atZone(ZoneId.systemDefault())
        );
    }
}