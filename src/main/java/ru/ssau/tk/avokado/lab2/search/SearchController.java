package ru.ssau.tk.avokado.lab2.search;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private static final Logger logger = LoggerFactory.getLogger(SearchController.class);
    private final SearchServiceFramework service;

    public SearchController(SearchServiceFramework service) {
        this.service = service;
    }

    @GetMapping("/bfs")
    public List<SearchResultDto> bfs(
            @RequestParam(defaultValue = "user") String start,
            @RequestParam String field,
            @RequestParam String value,
            @RequestParam(defaultValue = "100") int limit,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "true") boolean asc
    ) {
        logger.info("HTTP /api/search/bfs start={}, field={}, value={}, limit={}, sortBy={}, asc={}",
                start, field, value, limit, sortBy, asc);
        return service.searchBfs(start, field, value, limit, sortBy, asc);
    }

    @GetMapping("/dfs")
    public List<SearchResultDto> dfs(
            @RequestParam(defaultValue = "user") String start,
            @RequestParam String field,
            @RequestParam String value,
            @RequestParam(defaultValue = "100") int limit,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "true") boolean asc
    ) {
        logger.info("HTTP /api/search/dfs start={}, field={}, value={}, limit={}, sortBy={}, asc={}",
                start, field, value, limit, sortBy, asc);
        return service.searchDfs(start, field, value, limit, sortBy, asc);
    }

    @GetMapping("/function-by-user")
    public SearchResultDto functionByUser(
            @RequestParam String userName,
            @RequestParam String functionName
    ) {
        logger.info("HTTP /api/search/function-by-user user={}, function={}", userName, functionName);
        return service.findFunctionByUserName(userName, functionName).orElse(null);
    }
}
