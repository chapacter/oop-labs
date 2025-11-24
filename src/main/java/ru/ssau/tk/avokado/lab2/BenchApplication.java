package ru.ssau.tk.avokado.lab2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.testcontainers.containers.PostgreSQLContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
public class BenchApplication {
    private static final Logger logger = LoggerFactory.getLogger(BenchApplication.class);

    public static void main(String[] args) {
        PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
                .withDatabaseName("benchdb")
                .withUsername("bench")
                .withPassword("bench");
        postgres.start();

        System.setProperty("spring.datasource.url", postgres.getJdbcUrl());
        System.setProperty("spring.datasource.username", postgres.getUsername());
        System.setProperty("spring.datasource.password", postgres.getPassword());

        System.setProperty("spring.jpa.hibernate.ddl-auto", "update");

        logger.info("Started Postgres container. JDBC URL: {}", postgres.getJdbcUrl());

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Stopping Postgres container...");
            try {
                postgres.stop();
            } catch (Exception e) {
                logger.warn("Error stopping Postgres container", e);
            }
        }));

        SpringApplication.run(BenchApplication.class, args);
    }
}
