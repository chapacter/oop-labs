package ru.ssau.tk.avokado.lab2.bench;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.testcontainers.containers.PostgreSQLContainer;

@ComponentScan(basePackages = "ru.ssau.tk.avokado.lab2", excludeFilters = @ComponentScan.Filter(
        type = org.springframework.context.annotation.FilterType.REGEX,
        pattern = "ru\\.ssau\\.tk\\.avokado\\.lab2\\.search\\.SearchController"
))
@EntityScan(basePackages = "ru.ssau.tk.avokado.lab2.entities")
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
        System.setProperty("spring.jpa.hibernate.ddl-auto", "create-drop");
        System.setProperty("spring.profiles.active", "testcontainers");

        SpringApplication.run(BenchmarkRunnerFramework.class, args);
    }

}
