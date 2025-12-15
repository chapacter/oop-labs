package ru.ssau.tk.avokado.lab2.service;

import ru.ssau.tk.avokado.lab2.dto.SearchResultDto;

import java.util.List;
import java.util.Optional;

public interface SearchService {
    List<SearchResultDto> searchBfs(String start, String field, String value, int limit, String sortBy, boolean asc);
    List<SearchResultDto> searchDfs(String start, String field, String value, int limit, String sortBy, boolean asc);
    Optional<SearchResultDto> findFunctionByUserName(String userName, String functionName);
}
