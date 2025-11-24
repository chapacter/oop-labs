package ru.ssau.tk.avokado.lab2.dao;

import ru.ssau.tk.avokado.lab2.dto.PointDto;
import ru.ssau.tk.avokado.lab2.dto.FunctionDto;
import ru.ssau.tk.avokado.lab2.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class JdbcPointDao implements PointDao {
    private static final Logger logger = LoggerFactory.getLogger(JdbcPointDao.class);

    @Override
    public List<PointDto> findByFunctionId(Long functionId) {
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
            logger.error("Error finding points by function id: {} - {}", functionId, e.getMessage());
        }

        return points;
    }

    @Override
    public Long save(PointDto point) {
        String sql = "INSERT INTO points (function_id, x, y, point_index) VALUES (?, ?, ?, ?) RETURNING id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, point.getFunctionId());
            stmt.setDouble(2, point.getX());
            stmt.setDouble(3, point.getY());
            stmt.setInt(4, point.getPointIndex());

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Long pointId = rs.getLong(1);
                logger.debug("Saved point with id: {} for function id: {}", pointId, point.getFunctionId());
                return pointId;
            }
        } catch (SQLException e) {
            logger.error("Error saving point for function id: {} - {}", point.getFunctionId(), e.getMessage());
        }

        return null;
    }

    @Override
    public boolean deleteByFunctionId(Long functionId) {
        String sql = "DELETE FROM points WHERE function_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, functionId);
            int affectedRows = stmt.executeUpdate();

            logger.debug("Deleted {} points for function id: {}", affectedRows, functionId);
            return affectedRows > 0;
        } catch (SQLException e) {
            logger.error("Error deleting points for function id: {} - {}", functionId, e.getMessage());
            return false;
        }
    }

    private PointDto mapResultSetToPoint(ResultSet rs) throws SQLException {
        return new PointDto(rs.getLong("id"), rs.getLong("function_id"),
                rs.getDouble("x"), rs.getDouble("y"), rs.getInt("point_index"));
    }

    @Override
    public Optional<PointDto> findById(Long id) {
        String sql = "SELECT * FROM points WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                PointDto point = mapResultSetToPoint(rs);
                logger.debug("Found point by id: {}", id);
                return Optional.of(point);
            }
        } catch (SQLException e) {
            logger.error("Error finding point by id: {} - {}", id, e.getMessage());
        }

        return Optional.empty();
    }

    @Override
    public Optional<PointDto> findByFunctionIdAndPointIndex(Long functionId, Integer pointIndex) {
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
            logger.error("Error finding point by function id and point_index: {}, {} - {}",
                    functionId, pointIndex, e.getMessage());
        }

        return Optional.empty();
    }

    @Override
    public boolean update(PointDto point) {
        String sql = "UPDATE points SET x = ?, y = ?, point_index = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, point.getX());
            stmt.setDouble(2, point.getY());
            stmt.setInt(3, point.getPointIndex());
            stmt.setLong(4, point.getId());

            int affectedRows = stmt.executeUpdate();
            boolean success = affectedRows > 0;

            if (success) {
                logger.debug("Updated point with id: {}", point.getId());
            } else {
                logger.warn("No point found to update with id: {}", point.getId());
            }

            return success;
        } catch (SQLException e) {
            logger.error("Error updating point with id: {} - {}", point.getId(), e.getMessage());
            return false;
        }
    }

    @Override
    public boolean delete(Long id) {
        String sql = "DELETE FROM points WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            int affectedRows = stmt.executeUpdate();
            boolean success = affectedRows > 0;

            if (success) {
                logger.debug("Deleted point with id: {}", id);
            } else {
                logger.warn("No point found to delete with id: {}", id);
            }

            return success;
        } catch (SQLException e) {
            logger.error("Error deleting point with id: {} - {}", id, e.getMessage());
            return false;
        }
    }

    @Override
    public int countByFunctionId(Long functionId) {
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
        public List<PointDto> findByX(Double x) {
            List<PointDto> points = new ArrayList<>();
            String sql = "SELECT * FROM points WHERE x = ?";
    
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
    
                stmt.setDouble(1, x);
                ResultSet rs = stmt.executeQuery();
    
                while (rs.next()) {
                    PointDto point = mapResultSetToPoint(rs);
                    points.add(point);
                }
    
                logger.debug("Found {} points with x: {}", points.size(), x);
            } catch (SQLException e) {
                logger.error("Error finding points by x: {} - {}", x, e.getMessage());
            }
    
            return points;
        }
    
        @Override
        public List<PointDto> findByY(Double y) {
            List<PointDto> points = new ArrayList<>();
            String sql = "SELECT * FROM points WHERE y = ?";
    
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
    
                stmt.setDouble(1, y);
                ResultSet rs = stmt.executeQuery();
    
                while (rs.next()) {
                    PointDto point = mapResultSetToPoint(rs);
                    points.add(point);
                }
    
                logger.debug("Found {} points with y: {}", points.size(), y);
            } catch (SQLException e) {
                logger.error("Error finding points by y: {} - {}", y, e.getMessage());
            }
    
            return points;
        }
    
        @Override
        public List<PointDto> findByXBetween(Double x1, Double x2) {
            List<PointDto> points = new ArrayList<>();
            String sql = "SELECT * FROM points WHERE x BETWEEN ? AND ?";
    
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
    
                stmt.setDouble(1, x1);
                stmt.setDouble(2, x2);
                ResultSet rs = stmt.executeQuery();
    
                while (rs.next()) {
                    PointDto point = mapResultSetToPoint(rs);
                    points.add(point);
                }
    
                logger.debug("Found {} points with x between {} and {}", points.size(), x1, x2);
            } catch (SQLException e) {
                logger.error("Error finding points by x between {} and {}: {}", x1, x2, e.getMessage());
            }
    
            return points;
        }
    
        @Override
        public List<PointDto> findByYBetween(Double y1, Double y2) {
            List<PointDto> points = new ArrayList<>();
            String sql = "SELECT * FROM points WHERE y BETWEEN ? AND ?";
    
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
    
                stmt.setDouble(1, y1);
                stmt.setDouble(2, y2);
                ResultSet rs = stmt.executeQuery();
    
                while (rs.next()) {
                    PointDto point = mapResultSetToPoint(rs);
                    points.add(point);
                }
    
                logger.debug("Found {} points with y between {} and {}", points.size(), y1, y2);
            } catch (SQLException e) {
                logger.error("Error finding points by y between {} and {}: {}", y1, y2, e.getMessage());
            }
    
            return points;
        }
    
        @Override
        public List<PointDto> findByFunctionIdsIn(List<Long> functionIds) {
            if (functionIds == null || functionIds.isEmpty()) {
                return new ArrayList<>();
            }
    
            List<PointDto> points = new ArrayList<>();
            
            // Создаем строку с нужным количеством плейсхолдеров
            String placeholders = String.join(",", functionIds.stream().map(id -> "?").collect(Collectors.toList()));
            String sql = "SELECT * FROM points WHERE function_id IN (" + placeholders + ") ORDER BY function_id, point_index";
    
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
    
                for (int i = 0; i < functionIds.size(); i++) {
                    stmt.setLong(i + 1, functionIds.get(i));
                }
                
                ResultSet rs = stmt.executeQuery();
    
                while (rs.next()) {
                    PointDto point = mapResultSetToPoint(rs);
                    points.add(point);
                }
    
                logger.debug("Found {} points for function IDs: {}", points.size(), functionIds);
            } catch (SQLException e) {
                logger.error("Error finding points by function IDs: {} - {}", functionIds, e.getMessage());
            }
    
            return points;
        }
    
        @Override
        public List<PointDto> findByFunctionIdOrderByXAsc(Long functionId) {
            List<PointDto> points = new ArrayList<>();
            String sql = "SELECT * FROM points WHERE function_id = ? ORDER BY x ASC";
    
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
    
                stmt.setLong(1, functionId);
                ResultSet rs = stmt.executeQuery();
    
                while (rs.next()) {
                    PointDto point = mapResultSetToPoint(rs);
                    points.add(point);
                }
    
                logger.debug("Found {} points for function id: {} ordered by x ASC", points.size(), functionId);
            } catch (SQLException e) {
                logger.error("Error finding points by function id ordered by x ASC: {} - {}", functionId, e.getMessage());
            }
    
            return points;
        }
    
        @Override
        public List<PointDto> findByFunctionIdOrderByYAsc(Long functionId) {
            List<PointDto> points = new ArrayList<>();
            String sql = "SELECT * FROM points WHERE function_id = ? ORDER BY y ASC";
    
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
    
                stmt.setLong(1, functionId);
                ResultSet rs = stmt.executeQuery();
    
                while (rs.next()) {
                    PointDto point = mapResultSetToPoint(rs);
                    points.add(point);
                }
    
                logger.debug("Found {} points for function id: {} ordered by y ASC", points.size(), functionId);
            } catch (SQLException e) {
                logger.error("Error finding points by function id ordered by y ASC: {} - {}", functionId, e.getMessage());
            }
    
            return points;
        }
    
        @Override
        public List<PointDto> findByFunctionIdOrderByXDesc(Long functionId) {
            List<PointDto> points = new ArrayList<>();
            String sql = "SELECT * FROM points WHERE function_id = ? ORDER BY x DESC";
    
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
    
                stmt.setLong(1, functionId);
                ResultSet rs = stmt.executeQuery();
    
                while (rs.next()) {
                    PointDto point = mapResultSetToPoint(rs);
                    points.add(point);
                }
    
                logger.debug("Found {} points for function id: {} ordered by x DESC", points.size(), functionId);
            } catch (SQLException e) {
                logger.error("Error finding points by function id ordered by x DESC: {} - {}", functionId, e.getMessage());
            }
    
            return points;
        }
    
        @Override
        public List<PointDto> findByFunctionIdOrderByYDesc(Long functionId) {
            List<PointDto> points = new ArrayList<>();
            String sql = "SELECT * FROM points WHERE function_id = ? ORDER BY y DESC";
    
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
    
                stmt.setLong(1, functionId);
                ResultSet rs = stmt.executeQuery();
    
                while (rs.next()) {
                    PointDto point = mapResultSetToPoint(rs);
                    points.add(point);
                }
    
                logger.debug("Found {} points for function id: {} ordered by y DESC", points.size(), functionId);
            } catch (SQLException e) {
                logger.error("Error finding points by function id ordered by y DESC: {} - {}", functionId, e.getMessage());
            }
    
            return points;
        }
    
        @Override
        public List<PointDto> findByUserId(Long userId) {
            List<PointDto> points = new ArrayList<>();
            String sql = "SELECT p.* FROM points p JOIN functions f ON p.function_id = f.id WHERE f.user_id = ?";
    
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
    
                stmt.setLong(1, userId);
                ResultSet rs = stmt.executeQuery();
    
                while (rs.next()) {
                    PointDto point = mapResultSetToPoint(rs);
                    points.add(point);
                }
    
                logger.debug("Found {} points for user id: {}", points.size(), userId);
            } catch (SQLException e) {
                logger.error("Error finding points by user id: {} - {}", userId, e.getMessage());
            }
    
            return points;
        }
    

}
