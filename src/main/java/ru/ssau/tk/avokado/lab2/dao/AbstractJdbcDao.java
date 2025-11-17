package ru.ssau.tk.avokado.lab2.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class AbstractJdbcDao<T, ID> implements GenericDao<T, ID> {
    protected final Logger log = LoggerFactory.getLogger(getClass());
    protected final String tableName;
    protected final RowMapper<T> mapper;

    protected AbstractJdbcDao(String tableName, RowMapper<T> mapper) {
        this.tableName = tableName;
        this.mapper = mapper;
    }

    @Override public T save(T entity) {
        String sql = insertSql();
        try (Connection c = JdbcEntity.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setInsertParameters(ps, entity);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) setId(entity, (Long) rs.getObject(1));
            }
            log.info("Saved {}: {}", tableName, entity);
            return entity;
        } catch (Exception e) {
            log.error("Save failed: " + tableName, e);
            throw new RuntimeException(e);
        }
    }

    @Override public Optional<T> findById(ID id) {
        String sql = "SELECT * FROM " + tableName + " WHERE id = ?";
        try (Connection c = JdbcEntity.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setObject(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapper.map(rs)) : Optional.empty();
            }
        } catch (Exception e) {
            log.error("findById failed", e);
            return Optional.empty();
        }
    }

    @Override public List<T> findAll() {
        List<T> list = new ArrayList<>();
        String sql = "SELECT * FROM " + tableName;
        try (Connection c = JdbcEntity.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapper.map(rs));
        } catch (Exception e) {
            log.error("findAll failed", e);
        }
        return list;
    }

    @Override public T update(T entity) {
        String sql = updateSql();
        try (Connection c = JdbcEntity.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            setUpdateParameters(ps, entity);
            ps.executeUpdate();
            log.info("Updated {}: {}", tableName, entity);
            return entity;
        } catch (Exception e) {
            log.error("Update failed", e);
            throw new RuntimeException(e);
        }
    }

    @Override public void deleteById(ID id) {
        String sql = "DELETE FROM " + tableName + " WHERE id = ?";
        try (Connection c = JdbcEntity.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setObject(1, id);
            ps.executeUpdate();
            log.info("Deleted from {} id={}", tableName, id);
        } catch (Exception e) {
            log.error("Delete failed", e);
        }
    }

    protected abstract String insertSql();
    protected abstract String updateSql();
    protected abstract void setInsertParameters(PreparedStatement ps, T entity) throws SQLException;
    protected abstract void setUpdateParameters(PreparedStatement ps, T entity) throws SQLException;
    protected abstract void setId(T entity, Long id);
}

@FunctionalInterface
interface RowMapper<T> { T map(ResultSet rs) throws SQLException; }