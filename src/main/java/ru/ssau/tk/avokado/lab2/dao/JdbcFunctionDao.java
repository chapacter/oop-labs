package ru.ssau.tk.avokado.lab2.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.ssau.tk.avokado.lab2.DatabaseConnection;
import ru.ssau.tk.avokado.lab2.dto.FunctionDto;
import ru.ssau.tk.avokado.lab2.dto.PointDto;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcFunctionDao implements FunctionDao {
    private static final Logger logger = LoggerFactory.getLogger(JdbcFunctionDao.class);

    @Override
    public Optional<FunctionDto> findById(Long id) {
        String sql = "SELECT * FROM functions WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                FunctionDto function = mapResultSetToFunction(rs);
                logger.debug("Found function: {}", function);
                return Optional.of(function);
            }
        } catch (SQLException e) {
            logger.error("Error finding function by id: {} - {}", id, e.getMessage());
        }

        return Optional.empty();
    }

    @Override
    public List<FunctionDto> findByUserId(Long userId) {
        List<FunctionDto> functions = new ArrayList<>();
        String sql = "SELECT * FROM functions WHERE user_id = ? ORDER BY name ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                FunctionDto function = mapResultSetToFunction(rs);
                functions.add(function);
            }

            logger.info("Found " + functions.size() + " functions for user: " + userId);
        } catch (SQLException e) {
            logger.error("Error finding functions by user id: " + userId + " - " + e.getMessage());
        }

        return functions;
    }

    public List<FunctionDto> findByUserIdAndType(Long userId, String type) {
        List<FunctionDto> functions = new ArrayList<>();
        String sql = "SELECT * FROM functions WHERE user_id = ? AND type = ? ORDER BY name";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, userId);
            stmt.setString(2, type);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                FunctionDto function = mapResultSetToFunction(rs);
                functions.add(function);
            }

            logger.debug("Found {} functions of type {} for user id: {}", functions.size(), type, userId);
        } catch (SQLException e) {
            logger.error("Error finding functions by user id and type: {}, {} - {}", userId, type, e.getMessage());
        }

        return functions;
    }

    @Override
    public Long save(FunctionDto function) {
        String sql = "INSERT INTO functions (user_id, name, format, func_result) " +
                "VALUES (?, ?, ?, ?) RETURNING id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, function.getUserId());
            stmt.setString(2, function.getName());
            if (function.getFormat() != null) {
                stmt.setInt(3, function.getFormat());
            } else {
                stmt.setNull(3, Types.INTEGER);
            }
            stmt.setString(4, function.getFuncResult());

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Long functionId = rs.getLong(1);
                logger.info("Saved function: {}", function);
                return functionId;
            }
        } catch (SQLException e) {
            logger.error("Error saving function: {} - {}", function, e.getMessage());
        }

        return null;
    }

    @Override
    public boolean update(FunctionDto function) {
        String sql = "UPDATE functions SET name = ?, format = ?, func_result = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, function.getName());
            if (function.getFormat() != null) {
                stmt.setInt(2, function.getFormat());
            } else {
                stmt.setNull(2, Types.INTEGER);
            }
            stmt.setString(3, function.getFuncResult());
            stmt.setLong(4, function.getId());

            int affectedRows = stmt.executeUpdate();
            boolean success = affectedRows > 0;

            if (success) {
                logger.info("Updated function with id: {}", function.getId());
            } else {
                logger.warn("No function found to update with id: {}", function.getId());
            }

            return success;
        } catch (SQLException e) {
            logger.error("Error updating function with id: {} - {}", function.getId(), e.getMessage());
            return false;
        }
    }

    @Override
    public boolean delete(Long id) {
        // Сначала удаляем точки (каскадно в БД, но лучше явно для логирования)
        deleteAllPointsByFunctionId(id);

        String sql = "DELETE FROM functions WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            int affectedRows = stmt.executeUpdate();
            boolean success = affectedRows > 0;

            if (success) {
                logger.info("Deleted function with id: {}", id);
            } else {
                logger.warn("No function found to delete with id: {}", id);
            }

            return success;
        } catch (SQLException e) {
            logger.error("Error deleting function with id: {} - {}", id, e.getMessage());
            return false;
        }
    }

    @Override
    public List<FunctionDto> findByName(String name) {
        List<FunctionDto> functions = new ArrayList<>();
        String sql = "SELECT * FROM functions WHERE name = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                FunctionDto function = mapResultSetToFunction(rs);
                functions.add(function);
            }

            logger.debug("Found {} functions with name: {}", functions.size(), name);
        } catch (SQLException e) {
            logger.error("Error finding functions by name: {} - {}", name, e.getMessage());
        }

        return functions;
    }

    @Override
    public List<FunctionDto> findByNameLike(String namePattern) {
        List<FunctionDto> functions = new ArrayList<>();
        String sql = "SELECT * FROM functions WHERE name LIKE ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, namePattern);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                FunctionDto function = mapResultSetToFunction(rs);
                functions.add(function);
            }

            logger.debug("Found {} functions with name pattern: {}", functions.size(), namePattern);
        } catch (SQLException e) {
            logger.error("Error finding functions by name pattern: {} - {}", namePattern, e.getMessage());
        }

        return functions;
    }

    @Override
    public List<FunctionDto> findByUserIdAndNameLikeAndCreatedAtBetween(Long userId, String namePattern, java.time.ZonedDateTime start, java.time.ZonedDateTime end) {
        List<FunctionDto> functions = new ArrayList<>();
        String sql = "SELECT * FROM functions WHERE user_id = ? AND name LIKE ? AND created_at BETWEEN ? AND ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, userId);
            stmt.setString(2, namePattern);
            stmt.setTimestamp(3, Timestamp.from(start.toInstant()));
            stmt.setTimestamp(4, Timestamp.from(end.toInstant()));

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                FunctionDto function = mapResultSetToFunction(rs);
                functions.add(function);
            }

            logger.debug("Found {} functions for user {} with name pattern {} and created between {} and {}",
                    functions.size(), userId, namePattern, start, end);
        } catch (SQLException e) {
            logger.error("Error finding functions by user id, name pattern and created at range: {} - {}", userId, e.getMessage());
        }

        return functions;
    }

    @Override
    public List<FunctionDto> findByUserIdOrderByNameAsc(Long userId) {
        List<FunctionDto> functions = new ArrayList<>();
        String sql = "SELECT * FROM functions WHERE user_id = ? ORDER BY name ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                FunctionDto function = mapResultSetToFunction(rs);
                functions.add(function);
            }

            logger.info("Found " + functions.size() + " functions for user: " + userId + " ordered by name ASC");
        } catch (SQLException e) {
            logger.error("Error finding functions by user id ordered by name ASC: " + userId + " - " + e.getMessage());
        }

        return functions;
    }

    @Override
    public boolean updateName(Long id, String name) {
        String sql = "UPDATE functions SET name = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            stmt.setLong(2, id);

            int affectedRows = stmt.executeUpdate();
            boolean success = affectedRows > 0;

            if (success) {
                logger.info("Updated function name with id: {}", id);
            } else {
                logger.warn("No function found to update name with id: {}", id);
            }

            return success;
        } catch (SQLException e) {
            logger.error("Error updating function name with id: {} - {}", id, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean updateFormat(Long id, Integer format) {
        String sql = "UPDATE functions SET format = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (format != null) {
                stmt.setInt(1, format);
            } else {
                stmt.setNull(1, Types.INTEGER);
            }
            stmt.setLong(2, id);

            int affectedRows = stmt.executeUpdate();
            boolean success = affectedRows > 0;

            if (success) {
                logger.info("Updated function format with id: {}", id);
            } else {
                logger.warn("No function found to update format with id: {}", id);
            }

            return success;
        } catch (SQLException e) {
            logger.error("Error updating function format with id: {} - {}", id, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean updateFuncResult(Long id, String funcResult) {
        String sql = "UPDATE functions SET func_result = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, funcResult);
            stmt.setLong(2, id);

            int affectedRows = stmt.executeUpdate();
            boolean success = affectedRows > 0;

            if (success) {
                logger.info("Updated function func_result with id: {}", id);
            } else {
                logger.warn("No function found to update func_result with id: {}", id);
            }

            return success;
        } catch (SQLException e) {
            logger.error("Error updating function func_result with id: {} - {}", id, e.getMessage());
            return false;
        }
    }

    public boolean existsById(Long id) {
        String sql = "SELECT 1 FROM functions WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            boolean exists = rs.next();

            logger.debug("Function existence check for id {}: {}", id, exists);
            return exists;
        } catch (SQLException e) {
            logger.error("Error checking function existence for id: {} - {}", id, e.getMessage());
            return false;
        }
    }

    @Override
    public List<PointDto> findPointsByFunctionId(Long functionId) {
        List<PointDto> points = new ArrayList<>();
        String sql = "SELECT * FROM points WHERE function_id = ? ORDER BY point_index";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, functionId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                PointDto point = mapResultSetToPoint(rs);
                points.add(point);
            }

            logger.debug("Found {} points for function id: {}", points.size(), functionId);
        } catch (SQLException e) {
            logger.error("Error finding points for function id: {} - {}", functionId, e.getMessage());
        }

        return points;
    }

    @Override
    public Optional<PointDto> findPointByFunctionIdAndPointIndex(Long functionId, Integer pointIndex) {
        String sql = "SELECT * FROM points WHERE function_id = ? AND point_index = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, functionId);
            stmt.setInt(2, pointIndex);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                PointDto point = mapResultSetToPoint(rs);
                logger.debug("Found point at point_index {} for function id: {}", pointIndex, functionId);
                return Optional.of(point);
            }
        } catch (SQLException e) {
            logger.error("Error finding point at point_index {} for function id: {} - {}", pointIndex, functionId, e.getMessage());
        }

        return Optional.empty();
    }

    @Override
    public void savePoints(Long functionId, List<PointDto> points) {
        String sql = "INSERT INTO points (function_id, x, y, point_index) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (PointDto point : points) {
                stmt.setLong(1, functionId);
                stmt.setDouble(2, point.getX());
                stmt.setDouble(3, point.getY());
                stmt.setInt(4, point.getPointIndex());
                stmt.addBatch();
            }

            int[] results = stmt.executeBatch();
            logger.info("Saved {} points for function id: {}", results.length, functionId);

            // Обновляем points_count в функции
            updatePointsCount(functionId, points.size());

        } catch (SQLException e) {
            logger.error("Error saving points for function id: {} - {}", functionId, e.getMessage());
        }
    }

    @Override
    public boolean updatePoint(Long functionId, PointDto point) {
        String sql = "UPDATE points SET x = ?, y = ? WHERE function_id = ? AND point_index = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, point.getX());
            stmt.setDouble(2, point.getY());
            stmt.setLong(3, functionId);
            stmt.setInt(4, point.getPointIndex());

            int affectedRows = stmt.executeUpdate();
            boolean success = affectedRows > 0;

            if (success) {
                logger.debug("Updated point at point_index {} for function id: {}", point.getPointIndex(), functionId);
            } else {
                logger.warn("No point found to update at point_index {} for function id: {}", point.getPointIndex(), functionId);
            }

            return success;
        } catch (SQLException e) {
            logger.error("Error updating point at point_index {} for function id: {} - {}",
                    point.getPointIndex(), functionId, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean deletePoint(Long pointId) {
        String sql = "DELETE FROM points WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, pointId);
            int affectedRows = stmt.executeUpdate();
            boolean success = affectedRows > 0;

            if (success) {
                logger.debug("Deleted point with id: {}", pointId);
                Optional<Long> functionId = findFunctionIdByPointId(pointId);
                functionId.ifPresent(this::updatePointsCountBasedOnActualPoints);

            } else {
                logger.warn("No point found to delete with id: {}", pointId);
            }

            return success;
        } catch (SQLException e) {
            logger.error("Error deleting point with id: {} - {}", pointId, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean deleteAllPointsByFunctionId(Long functionId) {
        String sql = "DELETE FROM points WHERE function_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, functionId);
            int affectedRows = stmt.executeUpdate();

            // Обновляем points_count
            updatePointsCount(functionId, 0);

            logger.debug("Deleted {} points for function id: {}", affectedRows, functionId);
            return affectedRows > 0;
        } catch (SQLException e) {
            logger.error("Error deleting points for function id: {} - {}", functionId, e.getMessage());
            return false;
        }
    }

    @Override
    public int countPointsByFunctionId(Long functionId) {
        String sql = "SELECT COUNT(*) FROM points WHERE function_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, functionId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int count = rs.getInt(1);
                logger.debug("Counted {} points for function id: {}", count, functionId);
                return count;
            }
        } catch (SQLException e) {
            logger.error("Error counting points for function id: {} - {}", functionId, e.getMessage());
        }

        return 0;
    }

    @Override
    public List<FunctionDto> findAll() {
        return List.of();
    }

    // Вспомогательные методы
    private FunctionDto mapResultSetToFunction(ResultSet rs) throws SQLException {
        FunctionDto function = new FunctionDto(rs.getLong("id"), rs.getLong("user_id"),
                rs.getString("name"), rs.getObject("format", Integer.class), rs.getString("func_result"));
        Timestamp timestamp = rs.getTimestamp("created_at");
        if (timestamp != null) {
            function.setCreatedAt(timestamp.toInstant().atZone(java.time.ZoneId.systemDefault()));
        }
        return function;
    }

    private PointDto mapResultSetToPoint(ResultSet rs) throws SQLException {
        return new PointDto(rs.getLong("id"), rs.getLong("function_id"),
                rs.getDouble("x"), rs.getDouble("y"), rs.getInt("point_index"));
    }

    private void setFunctionParameters(PreparedStatement stmt, FunctionDto function) throws SQLException {
        stmt.setLong(1, function.getUserId());
        stmt.setString(2, function.getName());
        if (function.getFormat() != null) {
            stmt.setInt(3, function.getFormat());
        } else {
            stmt.setNull(3, Types.INTEGER);
        }
        stmt.setString(4, function.getFuncResult());
    }

    private void updatePointsCount(Long functionId, int count) {
        String sql = "UPDATE functions SET point_count = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, count);
            stmt.setLong(2, functionId);
            stmt.executeUpdate();

            logger.debug("Updated points_count to {} for function id: {}", count, functionId);
        } catch (SQLException e) {
            logger.error("Error updating points_count for function id: {} - {}", functionId, e.getMessage());
        }
    }

    private void updatePointsCountBasedOnActualPoints(Long functionId) {
        int actualCount = countPointsByFunctionId(functionId);
        updatePointsCount(functionId, actualCount);
    }

    private Optional<Long> findFunctionIdByPointId(Long pointId) {
        String sql = "SELECT function_id FROM points WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, pointId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(rs.getLong("function_id"));
            }
        } catch (SQLException e) {
            logger.error("Error finding function_id for point id: {} - {}", pointId, e.getMessage());
        }

        return Optional.empty();
    }

    @Override
    public List<FunctionDto> findByNameContaining(String name) {
        List<FunctionDto> functions = new ArrayList<>();
        String sql = "SELECT * FROM functions WHERE name ILIKE ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + name + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                functions.add(mapResultSetToFunction(rs));
            }

            logger.debug("Found {} functions with name containing: {}", functions.size(), name);
        } catch (SQLException e) {
            logger.error("Error finding functions by name containing: {} - {}", name, e.getMessage());
        }

        return functions;
    }
}