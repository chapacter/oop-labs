package ru.ssau.tk.avokado.lab2.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.ssau.tk.avokado.lab2.dto.SearchResultDto;
import ru.ssau.tk.avokado.lab2.entities.FunctionEntity;
import ru.ssau.tk.avokado.lab2.entities.TabulatedPoint;
import ru.ssau.tk.avokado.lab2.entities.User;
import ru.ssau.tk.avokado.lab2.repositories.FunctionRepository;
import ru.ssau.tk.avokado.lab2.repositories.PointRepository;
import ru.ssau.tk.avokado.lab2.repositories.UserRepository;
import ru.ssau.tk.avokado.lab2.service.SearchService;

import java.util.*;

@Service
public class SearchServiceImpl implements SearchService {

    private static final Logger logger = LoggerFactory.getLogger(SearchServiceImpl.class);

    private final UserRepository userRepository;
    private final FunctionRepository functionRepository;
    private final PointRepository pointRepository;

    public SearchServiceImpl(UserRepository userRepository,
                             FunctionRepository functionRepository,
                             PointRepository pointRepository) {
        this.userRepository = userRepository;
        this.functionRepository = functionRepository;
        this.pointRepository = pointRepository;
    }

    @Override
    public List<SearchResultDto> searchBfs(String start, String field, String value, int limit, String sortBy, boolean asc) {
        logger.debug("SearchService.searchBfs start='{}' field='{}' value='{}' limit={} sortBy={} asc={}",
                start, field, value, limit, sortBy, asc);
        return genericSearch(start, field, value, limit, true);
    }

    @Override
    public List<SearchResultDto> searchDfs(String start, String field, String value, int limit, String sortBy, boolean asc) {
        logger.debug("SearchService.searchDfs start='{}' field='{}' value='{}' limit={} sortBy={} asc={}",
                start, field, value, limit, sortBy, asc);
        return genericSearch(start, field, value, limit, false);
    }

    @Override
    public Optional<SearchResultDto> findFunctionByUserName(String userName, String functionName) {
        logger.debug("SearchService.findFunctionByUserName user='{}' function='{}'", userName, functionName);
        Optional<User> userOpt = userRepository.findByName(userName);
        if (userOpt.isEmpty()) return Optional.empty();
        User u = userOpt.get();
        List<FunctionEntity> funcs = functionRepository.findByUser(u);
        return funcs.stream().filter(f -> f.getName().equals(functionName)).findFirst()
                .map(f -> new SearchResultDto("function", f.getId(), f.getName(), Map.of("funcResult", f.getFuncResult())));
    }

    private List<SearchResultDto> genericSearch(String start, String field, String value, int limit, boolean bfs) {
        List<SearchResultDto> out = new ArrayList<>();
        Queue<Object> queue = new ArrayDeque<>();
        Deque<Object> stack = new ArrayDeque<>();

        // seed
        if (start != null) {
            if (start.startsWith("user:")) {
                String name = start.substring(5);
                userRepository.findByName(name).ifPresent(queue::add);
                userRepository.findByName(name).ifPresent(stack::add);
            } else if (start.startsWith("function:")) {
                String name = start.substring(9);
                functionRepository.findByName(name).ifPresent(queue::add);
                functionRepository.findByName(name).ifPresent(stack::add);
            } else {
                // default: search users by substring
                userRepository.findByNameContaining(start, Pageable.unpaged()).getContent().forEach(queue::add);
                userRepository.findByNameContaining(start, Pageable.unpaged()).getContent().forEach(stack::add);
            }
        } else {
            userRepository.findAll().forEach(queue::add);
            userRepository.findAll().forEach(stack::add);
        }

        Set<Object> visited = new HashSet<>();
        while ((bfs ? !queue.isEmpty() : !stack.isEmpty()) && out.size() < limit) {
            Object cur = bfs ? queue.poll() : stack.poll();
            if (cur == null) break;
            if (visited.contains(cur)) continue;
            visited.add(cur);

            if (cur instanceof User u) {
                if (matchesUser(u, field, value)) {
                    out.add(new SearchResultDto("user", u.getId(), u.getName(), Map.of("accessLvl", u.getAccessLvl())));
                    if (out.size() >= limit) break;
                }
                // expand: user -> functions
                List<FunctionEntity> funcs = functionRepository.findByUser(u);
                for (FunctionEntity f : funcs) {
                    if (bfs) queue.add(f); else stack.add(f);
                }
            } else if (cur instanceof FunctionEntity f) {
                if (matchesFunction(f, field, value)) {
                    out.add(new SearchResultDto("function", f.getId(), f.getName(), Map.of("funcResult", f.getFuncResult())));
                    if (out.size() >= limit) break;
                }
                // expand: function -> points
                List<TabulatedPoint> pts = pointRepository.findByFunction(f);
                for (TabulatedPoint p : pts) {
                    if (bfs) queue.add(p); else stack.add(p);
                }
            } else if (cur instanceof TabulatedPoint p) {
                if (matchesPoint(p, field, value)) {
                    out.add(new SearchResultDto("point", p.getId(), "x=" + p.getX() + ", y=" + p.getY(), Map.of("index", p.getIndexInFunction())));
                    if (out.size() >= limit) break;
                }
            }
        }
        return out;
    }

    private boolean matchesUser(User u, String field, String value) {
        if (field == null || "name".equalsIgnoreCase(field)) {
            return u.getName() != null && u.getName().contains(value);
        }
        return false;
    }

    private boolean matchesFunction(FunctionEntity f, String field, String value) {
        if (field == null || "name".equalsIgnoreCase(field)) {
            return f.getName() != null && f.getName().contains(value);
        }
        if ("funcResult".equalsIgnoreCase(field)) {
            return f.getFuncResult() != null && f.getFuncResult().contains(value);
        }
        return false;
    }

    private boolean matchesPoint(TabulatedPoint p, String field, String value) {
        if (field == null) return false;
        if ("index".equalsIgnoreCase(field)) return String.valueOf(p.getIndexInFunction()).equals(value);
        if ("x".equalsIgnoreCase(field)) return String.valueOf(p.getX()).equals(value);
        if ("y".equalsIgnoreCase(field)) return String.valueOf(p.getY()).equals(value);
        return false;
    }
}
