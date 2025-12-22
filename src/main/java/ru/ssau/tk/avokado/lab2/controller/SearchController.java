package ru.ssau.tk.avokado.lab2.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.ssau.tk.avokado.lab2.dto.SearchResultDto;
import ru.ssau.tk.avokado.lab2.service.SearchService;

import java.util.List;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final SearchService service;

    public SearchController(SearchService service) {
        this.service = service;
    }

    @GetMapping("/bfs")
    public List<SearchResultDto> bfs(
            @RequestParam(defaultValue = "user:seed_user") String start,
            @RequestParam String field,
            @RequestParam String value,
            @RequestParam(defaultValue = "100") int limit,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "true") boolean asc
    ) {
        return service.searchBfs(start, field, value, limit, sortBy, asc);
    }

    @GetMapping("/dfs")
    public List<SearchResultDto> dfs(
            @RequestParam(defaultValue = "user:seed_user") String start,
            @RequestParam String field,
            @RequestParam String value,
            @RequestParam(defaultValue = "100") int limit,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "true") boolean asc
    ) {
        return service.searchDfs(start, field, value, limit, sortBy, asc);
    }

    @GetMapping("/function-by-user")
    public SearchResultDto functionByUser(
            @RequestParam String userName,
            @RequestParam String functionName
    ) {
        return service.findFunctionByUserName(userName, functionName).orElse(null);
    }
}
