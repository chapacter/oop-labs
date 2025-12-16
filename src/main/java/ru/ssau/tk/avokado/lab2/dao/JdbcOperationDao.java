package ru.ssau.tk.avokado.lab2.dao;

import ru.ssau.tk.avokado.lab2.DatabaseConnection;
import ru.ssau.tk.avokado.lab2.dto.OperationDto;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcOperationDao implements OperationDao {
    private final Connection connection;

    public JdbcOperationDao(Connection connection) {
        this.connection = connection;
    }

    public JdbcOperationDao() {
        try {
            this.connection = DatabaseConnection.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException("Error getting database connection", e);
        }
    }

    @Override
    public Optional<OperationDto> findById(Long id) {
        String sql = "SELECT id, name, description FROM operations WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return Optional.of(mapResultSetToDto(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding operation by id: " + id, e);
        }
        return Optional.empty();
    }

    @Override
    public List<OperationDto> findAll() {
        String sql = "SELECT id, name, description FROM operations";
        List<OperationDto> operations = new ArrayList<>();
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                operations.add(mapResultSetToDto(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding all operations", e);
        }
        return operations;
    }

    @Override
    public List<OperationDto> findByName(String name) {
        String sql = "SELECT id, name, description FROM operations WHERE name = ?";
        List<OperationDto> operations = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                operations.add(mapResultSetToDto(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding operations by name: " + name, e);
        }
        return operations;
    }

    @Override
    public List<OperationDto> findByDescriptionLike(String descriptionPattern) {
        String sql = "SELECT id, name, description FROM operations WHERE description LIKE ?";
        List<OperationDto> operations = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, "%" + descriptionPattern + "%");
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                operations.add(mapResultSetToDto(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding operations by description pattern: " + descriptionPattern, e);
        }
        return operations;
    }

    @Override
    public Long save(OperationDto operation) {
        String sql = "INSERT INTO operations (name, description) VALUES (?, ?) RETURNING id";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, operation.getName());
            statement.setString(2, operation.getDescription());
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getLong("id");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error saving operation", e);
        }
        return null;
    }

    @Override
    public boolean update(OperationDto operation) {
        String sql = "UPDATE operations SET name = ?, description = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, operation.getName());
            statement.setString(2, operation.getDescription());
            statement.setLong(3, operation.getId());
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating operation: " + operation.getId(), e);
        }
    }

    @Override
    public boolean updateName(Long id, String name) {
        String sql = "UPDATE operations SET name = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            statement.setLong(2, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating operation name: " + id, e);
        }
    }

    @Override
    public boolean updateDescription(Long id, String description) {
        String sql = "UPDATE operations SET description = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, description);
            statement.setLong(2, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating operation description: " + id, e);
        }
    }

    @Override
    public boolean delete(Long id) {
        String sql = "DELETE FROM operations WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting operation: " + id, e);
        }
    }

    @Override
    public boolean existsByName(String name) {
        String sql = "SELECT COUNT(*) FROM operations WHERE name = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error checking if operation exists by name: " + name, e);
        }
        return false;
    }

    private OperationDto mapResultSetToDto(ResultSet resultSet) throws SQLException {
        return new OperationDto(
                resultSet.getLong("id"),
                resultSet.getString("name"),
                resultSet.getString("description")
        );
    }

    @Override
    public List<OperationDto> findByNameContaining(String name) {
        String sql = "SELECT id, name, description FROM operations WHERE name ILIKE ?";
        List<OperationDto> operations = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, "%" + name + "%");
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                operations.add(mapResultSetToDto(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding operations by name containing: " + name, e);
        }
        return operations;
    }
}