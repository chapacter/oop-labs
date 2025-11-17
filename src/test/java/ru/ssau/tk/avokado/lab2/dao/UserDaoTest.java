package ru.ssau.tk.avokado.lab2.dao;

import static org.junit.jupiter.api.Assertions.*;

import ru.ssau.tk.avokado.lab2.entity.User;
import com.github.javafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
class UserDaoTest {
    private static final Logger log = LoggerFactory.getLogger(UserDaoTest.class);
    private AbstractJdbcDao<User, Long> dao;
    private Faker faker = new Faker();

    @BeforeEach
    void setUp() throws SQLException {
        String url = "jdbc:h2:mem:testes;DB_CLOSE_DELAY=-1";
        try (Connection conn = DriverManager.getConnection(url)) {
            String schema = "CREATE TABLE users (id IDENTITY PRIMARY KEY, username VARCHAR, email VARCHAR, password_hash VARCHAR, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP);";
            conn.createStatement().execute(schema);
        }
        dao = new UserDao() {
            @Override
            public Connection getConnection() throws Exception {
                return DriverManager.getConnection(url);
            }
        };
    }

    @Test
    void testCreateFindUpdateDelete() {
        User user = new User(faker.name().username(), faker.internet().emailAddress(), faker.internet().password());
        User saved = dao.save(user);
        assertNotNull(saved.getId());
        log.info("Saved user: {}", saved.getId());

        Optional<User> found = dao.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals(user.getUsername(), found.get().getUsername());

        user.setEmail("new@email.com");
        dao.update(user);
        found = dao.findById(user.getId());
        assertEquals("new@email.com", found.get().getEmail());

        dao.deleteById(user.getId());
        assertTrue(dao.findById(user.getId()).isEmpty());
    }

    @Test
    void testFindAllWithGeneration() {
        for (int i = 0; i < 100; i++) {
            User u = new User(faker.name().username() + i, faker.internet().emailAddress(), "hash" + i);
            dao.save(u);
        }
        List<User> all = dao.findAll();
        assertEquals(100, all.size());
        log.info("Generated and found {} users", all.size());
    }
}