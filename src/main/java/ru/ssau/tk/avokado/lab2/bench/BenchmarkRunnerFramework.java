package ru.ssau.tk.avokado.lab2.bench;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Запуск бенчмарка (framework-only). Этот main стартует Postgres контейнер (Testcontainers),
 * затем вызывает SpringApplication.run(...).
 *
 * Запускать в IDEA — Run 'BenchmarkRunnerFramework'.
 */
@SpringBootApplication(scanBasePackages = "ru.ssau.tk.avokado.lab2")
public class BenchmarkRunnerFramework {

    // Контейнер поднимаем до старта Spring — чтобы Spring взял JDBC URL из system properties
    public static void main(String[] args) {
        PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
                .withDatabaseName("benchdb")
                .withUsername("bench")
                .withPassword("bench");

        postgres.start();

        // Передаём контейнерные настройки Spring Boot через системные свойства
        System.setProperty("spring.datasource.url", postgres.getJdbcUrl());
        System.setProperty("spring.datasource.username", postgres.getUsername());
        System.setProperty("spring.datasource.password", postgres.getPassword());

        // Настройки для удобства (минимум логов Hibernate в консоли)
        System.setProperty("logging.level.org.hibernate.SQL", "OFF");
        System.setProperty("logging.level.org.hibernate.type.descriptor.sql.BasicBinder", "OFF");

        SpringApplication.run(BenchmarkRunnerFramework.class, args);
    }

    // CommandLineRunner bean находится в BenchmarkServiceFramework (автоматически вызовется)
}
