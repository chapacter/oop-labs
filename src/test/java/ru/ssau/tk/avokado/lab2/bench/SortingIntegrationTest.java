package ru.ssau.tk.avokado.lab2.bench;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.PostgreSQLContainer;
import ru.ssau.tk.avokado.lab2.entities.FunctionEntity;
import ru.ssau.tk.avokado.lab2.repositories.FunctionRepository;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SortingIntegrationTest {

    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("benchdb")
            .withUsername("bench")
            .withPassword("bench");

    static {
        postgres.start();
        System.setProperty("spring.datasource.url", postgres.getJdbcUrl());
        System.setProperty("spring.datasource.username", postgres.getUsername());
        System.setProperty("spring.datasource.password", postgres.getPassword());
        System.setProperty("spring.jpa.hibernate.ddl-auto", "update");
    }

    @Autowired
    private DbPopulatorFramework populator;

    @Autowired
    private FunctionRepository functionRepository;

    @Test
    public void dbSortEqualsInMemorySort() {
        int required = 2000;
        if (!populator.isPopulated(required)) {
            populator.populate(required, 2, 500);
        }

        List<FunctionEntity> dbSorted = functionRepository.findAll(org.springframework.data.domain.Sort.by("name"));
        List<FunctionEntity> mem = functionRepository.findAll().stream()
                .sorted(Comparator.comparing(FunctionEntity::getName, Comparator.nullsFirst(String::compareTo)))
                .collect(Collectors.toList());

        int check = Math.min(100, Math.min(dbSorted.size(), mem.size()));
        for (int i = 0; i < check; i++) {
            Assert.assertEquals("Mismatch at index " + i, dbSorted.get(i).getName(), mem.get(i).getName());
        }
    }
}
