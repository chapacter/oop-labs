package ru.ssau.tk.avokado.lab2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@ComponentScan(basePackages = "ru.ssau.tk.avokado.lab2", excludeFilters = @ComponentScan.Filter(
        type = org.springframework.context.annotation.FilterType.REGEX,
        pattern = "ru\\.ssau\\.tk\\.avokado\\.lab2\\.search\\.SearchController"
))
@EnableJpaRepositories(basePackages = "ru.ssau.tk.avokado.lab2.repositories")
@EntityScan(basePackages = "ru.ssau.tk.avokado.lab2.entities")
@SpringBootApplication
public class BenchApplication {
    private static final Logger logger = LoggerFactory.getLogger(BenchApplication.class);

    public static void main(String[] args) {
        System.setProperty("spring.profiles.active", "default");
        SpringApplication.run(BenchApplication.class, args);
    }
}
