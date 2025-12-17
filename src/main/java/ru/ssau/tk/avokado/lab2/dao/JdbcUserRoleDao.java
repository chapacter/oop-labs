package ru.ssau.tk.avokado.lab2.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.ssau.tk.avokado.lab2.DatabaseConnection;
import ru.ssau.tk.avokado.lab2.dto.UserDto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JdbcUserRoleDao implements UserRoleDao {
    private static final Logger logger = LoggerFactory.getLogger(JdbcUserRoleDao.class);

    @Override
    public boolean addUserRole(Long userId, String role) {
        String sql = "INSERT INTO user_roles (user_id, role) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, userId);
            stmt.setString(2, role);

            int affectedRows = stmt.executeUpdate();
            boolean success = affectedRows > 0;

            if (success) {
                logger.info("Added role {} to user with id: {}", role, userId);
            } else {
                logger.warn("Failed to add role {} to user with id: {}", role, userId);
            }

            return success;
        } catch (SQLException e) {
            logger.error("Error adding role {} to user with id: {} - {}", role, userId, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean removeUserRole(Long userId, String role) {
        String sql = "DELETE FROM user_roles WHERE user_id = ? AND role = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, userId);
            stmt.setString(2, role);

            int affectedRows = stmt.executeUpdate();
            boolean success = affectedRows > 0;

            if (success) {
                logger.info("Removed role {} from user with id: {}", role, userId);
            } else {
                logger.warn("No role {} found for user with id: {} to remove", role, userId);
            }

            return success;
        } catch (SQLException e) {
            logger.error("Error removing role {} from user with id: {} - {}", role, userId, e.getMessage());
            return false;
        }
    }

    @Override
    public Set<String> getUserRoles(Long userId) {
        Set<String> roles = new HashSet<>();
        String sql = "SELECT role FROM user_roles WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                roles.add(rs.getString("role"));
            }

            logger.debug("Found roles for user id {}: {}", userId, roles);
        } catch (SQLException e) {
            logger.error("Error getting roles for user with id: {} - {}", userId, e.getMessage());
        }

        return roles;
    }

    @Override
    public boolean userHasRole(Long userId, String role) {
        String sql = "SELECT 1 FROM user_roles WHERE user_id = ? AND role = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, userId);
            stmt.setString(2, role);
            ResultSet rs = stmt.executeQuery();

            boolean hasRole = rs.next();

            logger.debug("User id {} has role {}: {}", userId, role, hasRole);
            return hasRole;
        } catch (SQLException e) {
            logger.error("Error checking role {} for user with id: {} - {}", role, userId, e.getMessage());
            return false;
        }
    }

    @Override
    public List<UserDto> getUsersByRole(String role) {
        List<UserDto> users = new ArrayList<>();
        String sql = "SELECT u.id, u.name, u.access_lvl, u.password_hash FROM users u " +
                "JOIN user_roles ur ON u.id = ur.user_id WHERE ur.role = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, role);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                UserDto user = new UserDto(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getInt("access_lvl"),
                        rs.getString("password_hash")
                );
                users.add(user);
            }

            logger.debug("Found {} users with role: {}", users.size(), role);
        } catch (SQLException e) {
            logger.error("Error getting users with role: {} - {}", role, e.getMessage());
        }

        return users;
    }

    @Override
    public boolean deleteAllUserRoles(Long userId) {
        String sql = "DELETE FROM user_roles WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, userId);

            int affectedRows = stmt.executeUpdate();
            boolean success = affectedRows > 0;

            if (success) {
                logger.info("Deleted all roles for user with id: {}", userId);
            } else {
                logger.warn("No roles found for user with id: {} to delete", userId);
            }

            return success;
        } catch (SQLException e) {
            logger.error("Error deleting all roles for user with id: {} - {}", userId, e.getMessage());
            return false;
        }
    }
}