package ru.ssau.tk.avokado.lab2.dao;

import ru.ssau.tk.avokado.lab2.dto.UserDto;
import ru.ssau.tk.avokado.lab2.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

public class JdbcUserDao implements UserDao {
    private static final Logger logger = LoggerFactory.getLogger(JdbcUserDao.class);

    @Override
    public Optional<UserDto> findById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                UserDto user = mapResultSetToUser(rs);
                logger.debug("Found user by id: {}", id);
                return Optional.of(user);
            }
        } catch (SQLException e) {
            logger.error("Error finding user by id: {} - {}", id, e.getMessage());
        }

        return Optional.empty();
    }

    @Override
    public Optional<UserDto> findByName(String name) {
        String sql = "SELECT * FROM users WHERE name = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                UserDto user = mapResultSetToUser(rs);
                logger.debug("Found user by name: {}", name);
                return Optional.of(user);
            }
        } catch (SQLException e) {
            logger.error("Error finding user by name: {} - {}", name, e.getMessage());
        }

        return Optional.empty();
    }

    @Override
    public List<UserDto> findAll() {
        List<UserDto> users = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY name";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                UserDto user = mapResultSetToUser(rs);
                users.add(user);
            }

            logger.debug("Found {} users", users.size());
        } catch (SQLException e) {
            logger.error("Error finding all users: {}", e.getMessage());
        }

        return users;
    }

    @Override
    public Long save(UserDto user) {
        String sql = "INSERT INTO users (name, access_lvl, password_hash) VALUES (?, ?, ?) RETURNING id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getName());
            stmt.setInt(2, user.getAccessLvl());
            stmt.setString(3, user.getPasswordHash());

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Long userId = rs.getLong(1);
                logger.info("Saved user with id: {}, name: {}", userId, user.getName());
                return userId;
            }
        } catch (SQLException e) {
            logger.error("Error saving user: {} - {}", user.getName(), e.getMessage());
        }

        return null;
    }

    private UserDto mapResultSetToUser(ResultSet rs) throws SQLException {
        UserDto user = new UserDto(rs.getLong("id"), rs.getString("name"),
                rs.getInt("access_lvl"), rs.getString("password_hash"));
        return user;
    }

    @Override
    public boolean update(UserDto user) {
        String sql = "UPDATE users SET name = ?, access_lvl = ?, password_hash = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getName());
            stmt.setInt(2, user.getAccessLvl());
            stmt.setString(3, user.getPasswordHash());
            stmt.setLong(4, user.getId());

            int affectedRows = stmt.executeUpdate();
            boolean success = affectedRows > 0;

            if (success) {
                logger.info("Updated user with id: {}", user.getId());
            } else {
                logger.warn("No user found to update with id: {}", user.getId());
            }

            return success;
        } catch (SQLException e) {
            logger.error("Error updating user with id: {} - {}", user.getId(), e.getMessage());
            return false;
        }
    }

    @Override
    public boolean delete(Long id) {
        String sql = "DELETE FROM users WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            int affectedRows = stmt.executeUpdate();
            boolean success = affectedRows > 0;

            if (success) {
                logger.info("Deleted user with id: {}", id);
            } else {
                logger.warn("No user found to delete with id: {}", id);
            }

            return success;
        } catch (SQLException e) {
            logger.error("Error deleting user with id: {} - {}", id, e.getMessage());
            return false;
        }
    }

    @Override
    public Optional<UserDto> findByNameAndPassword(String name, String passwordHash) {
        String sql = "SELECT * FROM users WHERE name = ? AND password_hash = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            stmt.setString(2, passwordHash);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                UserDto user = mapResultSetToUser(rs);
                logger.debug("Found user by name and password: {}", name);
                return Optional.of(user);
            }
        } catch (SQLException e) {
            logger.error("Error finding user by name and password: {} - {}", name, e.getMessage());
        }

        return Optional.empty();
    }

    @Override
    public boolean updateName(Long id, String name) {
        String sql = "UPDATE users SET name = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            stmt.setLong(2, id);

            int affectedRows = stmt.executeUpdate();
            boolean success = affectedRows > 0;

            if (success) {
                logger.info("Updated user name with id: {}", id);
            } else {
                logger.warn("No user found to update name with id: {}", id);
            }

            return success;
        } catch (SQLException e) {
            logger.error("Error updating user name with id: {} - {}", id, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean updateAccessLvl(Long id, Integer accessLvl) {
        String sql = "UPDATE users SET access_lvl = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (accessLvl != null) {
                stmt.setInt(1, accessLvl);
            } else {
                stmt.setNull(1, java.sql.Types.INTEGER);
            }
            stmt.setLong(2, id);

            int affectedRows = stmt.executeUpdate();
            boolean success = affectedRows > 0;

            if (success) {
                logger.info("Updated user access level with id: {}", id);
            } else {
                logger.warn("No user found to update access level with id: {}", id);
            }

            return success;
        } catch (SQLException e) {
            logger.error("Error updating user access level with id: {} - {}", id, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean updatePasswordHash(Long id, String passwordHash) {
        String sql = "UPDATE users SET password_hash = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, passwordHash);
            stmt.setLong(2, id);

            int affectedRows = stmt.executeUpdate();
            boolean success = affectedRows > 0;

            if (success) {
                logger.info("Updated user password hash with id: {}", id);
            } else {
                logger.warn("No user found to update password hash with id: {}", id);
            }

            return success;
        } catch (SQLException e) {
            logger.error("Error updating user password hash with id: {} - {}", id, e.getMessage());
            return false;
        }
    }

    @Override
    public List<UserDto> getUserStatistics(Long userId) {
        String sql = "SELECT\n" +
                "    f.id,\n" +
                "    f.name,\n" +
                "    COUNT(p.id) as point_count,\n" +
                "    MIN(p.x) as min_x,\n" +
                "    MAX(p.x) as max_x,\n" +
                "    AVG(p.y) as avg_y\n" +
                "FROM functions f LEFT JOIN points p ON f.id = p.function_id WHERE f.user_id = ? GROUP BY f.id, f.name";

        List<UserDto> users = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                UserDto user = new UserDto(
                    rs.getLong("id"),
                    rs.getString("name"),
                    rs.getInt("point_count")
                );
                users.add(user);
            }

            logger.debug("Found {} user statistics for user id: {}", users.size(), userId);
        } catch (SQLException e) {
            logger.error("Error getting user statistics for user id: {} - {}", userId, e.getMessage());
        }

        return users;
    }

    @Override
    public boolean existsByName(String name) {
        String sql = "SELECT 1 FROM users WHERE name = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            boolean exists = rs.next();

            logger.debug("User existence check for {}: {}", name, exists);
            return exists;
        } catch (SQLException e) {
            logger.error("Error checking user existence: {} - {}", name, e.getMessage());
            return false;
        }
    }
}
