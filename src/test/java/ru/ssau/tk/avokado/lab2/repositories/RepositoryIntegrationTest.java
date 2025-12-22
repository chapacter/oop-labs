package ru.ssau.tk.avokado.lab2.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.ssau.tk.avokado.lab2.entities.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@EntityScan("ru.ssau.tk.avokado.lab2.entities")
@EnableJpaRepositories("ru.ssau.tk.avokado.lab2.repositories")
class RepositoryIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FunctionRepository functionRepository;

    @Autowired
    private PointRepository pointRepository;

    @Autowired
    private OperationRepository operationRepository;

    @Autowired
    private ProcessedFunctionRepository processedFunctionRepository;

    @Autowired
    private ResultValueRepository resultValueRepository;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
    }

    @BeforeEach
    void cleanup() {
        resultValueRepository.deleteAll();
        processedFunctionRepository.deleteAll();
        pointRepository.deleteAll();
        functionRepository.deleteAll();
        operationRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @Transactional
    @Rollback
    void testCreateFindDeleteFlow() {
        User user = new User();
        user.setName("alice");
        user.setAccessLvl(1);
        user.setPasswordHash("hash");
        user = userRepository.save(user);
        assertNotNull(user.getId());

        OperationEntity op = new OperationEntity("left_diff", "Left difference");
        op = operationRepository.save(op);
        assertNotNull(op.getId());

        FunctionEntity func = new FunctionEntity();
        func.setName("f1");
        func.setUser(user);
        func = functionRepository.save(func);
        assertNotNull(func.getId());

        TabulatedPoint p0 = new TabulatedPoint(0, 1.0, 1.0, func);
        TabulatedPoint p1 = new TabulatedPoint(1, 2.0, 4.0, func);
        pointRepository.saveAll(List.of(p0, p1));

        List<TabulatedPoint> points = pointRepository.findByFunction(func);
        assertEquals(2, points.size());

        ProcessedFunctionEntity processed = new ProcessedFunctionEntity(func, op, "summary");
        processed = processedFunctionRepository.save(processed);
        assertNotNull(processed.getId());

        ResultValueEntity rv = new ResultValueEntity(0, 1.0, 0.0, processed);
        resultValueRepository.save(rv);

        Optional<User> foundUser = userRepository.findByName("alice");
        assertTrue(foundUser.isPresent());

        Optional<OperationEntity> foundOp = operationRepository.findByName("left_diff");
        assertTrue(foundOp.isPresent());

        Optional<FunctionEntity> foundFunc = functionRepository.findByName("f1");
        assertTrue(foundFunc.isPresent());

        processedFunctionRepository.delete(processed);
        assertFalse(processedFunctionRepository.findById(processed.getId()).isPresent());
    }

    @SpringBootApplication
    static class TestConfig {
    }
}
