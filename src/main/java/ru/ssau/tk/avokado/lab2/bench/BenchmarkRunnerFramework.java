package ru.ssau.tk.avokado.lab2.bench;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.testcontainers.containers.PostgreSQLContainer;

@SpringBootApplication(scanBasePackages = "ru.ssau.tk.avokado.lab2")
public class BenchmarkRunnerFramework {

    public static void main(String[] args) {
        PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
                .withDatabaseName("benchdb")
                .withUsername("bench")
                .withPassword("bench");

        postgres.start();

        System.setProperty("spring.datasource.url", postgres.getJdbcUrl());
        System.setProperty("spring.datasource.username", postgres.getUsername());
        System.setProperty("spring.datasource.password", postgres.getPassword());

        System.setProperty("logging.level.org.hibernate.SQL", "OFF");
        System.setProperty("logging.level.org.hibernate.type.descriptor.sql.BasicBinder", "OFF");

        SpringApplication.run(BenchmarkRunnerFramework.class, args);
    }

}
