package ru.ssau.tk.avokado.lab2.search;

import ru.ssau.tk.avokado.lab2.DatabaseConnection;
import ru.ssau.tk.avokado.lab2.Role;
import ru.ssau.tk.avokado.lab2.dao.JdbcFunctionDao;
import ru.ssau.tk.avokado.lab2.dao.JdbcUserDao;
import ru.ssau.tk.avokado.lab2.dao.SearchJdbcFunctionDao;
import ru.ssau.tk.avokado.lab2.dto.FunctionDto;
import ru.ssau.tk.avokado.lab2.dto.SearchCriteria;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import ru.ssau.tk.avokado.lab2.dto.UserDto;
import ru.ssau.tk.avokado.lab2.DataGenerator;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static java.lang.Math.random;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(OrderAnnotation.class)
@DisplayName("Search System Tests")
class SearchTest {

    private SearchJdbcFunctionDao advancedDao;
    private JdbcFunctionDao functionDao;
    private Long testUserId;

    @BeforeEach
    void setUp() {
        // Удаляем все существующие таблицы перед созданием новых
        try (Connection conn = DatabaseConnection.getConnection()) {
            String dropTablesScript =
                "DROP TABLE IF EXISTS result_values CASCADE;" +
                "DROP TABLE IF EXISTS processed_functions CASCADE;" +
                "DROP TABLE IF EXISTS tabulated_func CASCADE;" +
                "DROP TABLE IF EXISTS points CASCADE;" +
                "DROP TABLE IF EXISTS functions CASCADE;" +
                "DROP TABLE IF EXISTS operations CASCADE;" +
                "DROP TABLE IF EXISTS users CASCADE;";
            
            PreparedStatement dropStmt = conn.prepareStatement(dropTablesScript);
            dropStmt.execute();
        } catch (SQLException e) {
            // Игнорируем ошибки при удалении таблиц, если они не существуют
        }

        StringBuilder script = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader("src/main/resources/scripts/create_tables.sql"))) {
            String line;

            while ((line = reader.readLine()) != null) {
                script.append(line).append("\n");
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(script.toString());
            stmt.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        JdbcUserDao userDao = new JdbcUserDao();
        UserDto user = new UserDto(1L, "test", 0, "111");
        userDao.save(user);

        JdbcFunctionDao baseDao = new JdbcFunctionDao();
        advancedDao = new SearchJdbcFunctionDao(baseDao);
        functionDao = new JdbcFunctionDao();
        testUserId = 1L;

        List<FunctionDto> functions = DataGenerator.generateFunctionsDto(1L, (int) (100 * random()));
        functions.forEach(f -> functionDao.save(f));
    }

    @Test
    @Order(1)
    @DisplayName("Single field search")
    void testSingleFieldSearch() {
        String searchName = "Test";

        List<FunctionDto> results = advancedDao.findByFieldLike("name", searchName);

        assertNotNull(results);
        assertTrue(results.stream().allMatch(f -> f.getName().contains(searchName)));
    }

    @Test
    @Order(2)
    @DisplayName("Multiple criteria search")
    void testMultipleCriteriaSearch() {
        SearchCriteria criteria = new SearchCriteria()
                .addCondition("format", SearchCriteria.Operator.EQUALS, 0)
                .sortBy("name", SearchCriteria.SortDirection.ASC);

        var result = advancedDao.search(criteria);

        assertNotNull(result);
        assertFalse(result.getItems().isEmpty());
    }

    @Test
    @Order(3)
    @DisplayName("Pagination search")
    void testPaginationSearch() {
        int page = 1;
        int pageSize = 5;

        List<FunctionDto> results = advancedDao.findWithPagination(page, pageSize);

        assertNotNull(results);
        assertTrue(results.size() <= pageSize);
    }

    @Test
    @Order(4)
    @DisplayName("IN clause search")
    void testInClauseSearch() {
        List<Integer> formats = Arrays.asList(0, 1);

        List<FunctionDto> results = advancedDao.findByFieldIn("format", formats);

        assertNotNull(results);
        assertTrue(results.stream().allMatch(f -> formats.contains(f.getFormat())));
    }

    @Test
    @Order(5)
    @DisplayName("Breadth-first search")
    void testBreadthFirstSearch() {
        String searchPattern = "Test";

        List<FunctionDto> results = advancedDao.breadthFirstSearch(testUserId, searchPattern);

        assertNotNull(results);
    }

    @Test
    @Order(6)
    @DisplayName("Multi-criteria search")
    void testMultiCriteriaSearch() {
        SearchCriteria nameCriteria = new SearchCriteria()
                .addCondition("name", SearchCriteria.Operator.LIKE, "Test");

        SearchCriteria typeCriteria = new SearchCriteria()
                .addCondition("type", SearchCriteria.Operator.EQUALS, "TABULATED");

        List<SearchCriteria> criteriaList = Arrays.asList(nameCriteria, typeCriteria);

        var result = advancedDao.multiFieldSearch(criteriaList);

        assertNotNull(result);
        assertFalse(result.getItems().isEmpty());
    }

    @Test
    @Order(7)
    @DisplayName("Search with sorting")
    void testSearchWithSorting() {
        List<FunctionDto> ascendingResults = advancedDao.findWithSorting("name", SearchCriteria.SortDirection.ASC);
        List<FunctionDto> descendingResults = advancedDao.findWithSorting("name", SearchCriteria.SortDirection.DESC);

        assertNotNull(ascendingResults);
        assertNotNull(descendingResults);

        if (ascendingResults.size() > 1 && descendingResults.size() > 1) {
            // Проверяем, что сортировка работает
            String firstAsc = ascendingResults.get(0).getName();
            String lastAsc = ascendingResults.get(ascendingResults.size() - 1).getName();
            String firstDesc = descendingResults.get(0).getName();
            String lastDesc = descendingResults.get(descendingResults.size() - 1).getName();

            assertTrue(firstAsc.compareTo(lastAsc) <= 0);
            assertTrue(firstDesc.compareTo(lastDesc) >= 0);
        }
    }
}
