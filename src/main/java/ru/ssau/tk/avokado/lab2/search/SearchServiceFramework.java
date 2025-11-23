package ru.ssau.tk.avokado.lab2.search;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ssau.tk.avokado.lab2.entities.FunctionEntity;
import ru.ssau.tk.avokado.lab2.entities.TabulatedPoint;
import ru.ssau.tk.avokado.lab2.entities.User;
import ru.ssau.tk.avokado.lab2.repositories.FunctionRepository;
import ru.ssau.tk.avokado.lab2.repositories.PointRepository;
import ru.ssau.tk.avokado.lab2.repositories.UserRepository;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SearchServiceFramework {

    private static final Logger logger = LoggerFactory.getLogger(SearchServiceFramework.class);

    private final UserRepository userRepository;
    private final FunctionRepository functionRepository;
    private final PointRepository pointRepository;

    public SearchServiceFramework(UserRepository userRepository,
                                  FunctionRepository functionRepository,
                                  PointRepository pointRepository) {
        this.userRepository = userRepository;
        this.functionRepository = functionRepository;
        this.pointRepository = pointRepository;
    }

    @Transactional(readOnly = true)
    public List<SearchResultDto> searchBfs(String start, String fieldName, String value, int limit, String sortBy, boolean asc) {
        logger.info("BFS start={}, field={}, value={}, limit={}, sortBy={}, asc={}", start, fieldName, value, limit, sortBy, asc);
        List<Object> roots = loadRoots(start);
        List<SearchResultDto> found = bfs(roots, fieldName, value, limit);
        sortIfNeeded(found, sortBy, asc);
        logger.info("BFS finished, found {}", found.size());
        return found;
    }

    @Transactional(readOnly = true)
    public List<SearchResultDto> searchDfs(String start, String fieldName, String value, int limit, String sortBy, boolean asc) {
        logger.info("DFS start={}, field={}, value={}, limit={}, sortBy={}, asc={}", start, fieldName, value, limit, sortBy, asc);
        List<Object> roots = loadRoots(start);
        List<SearchResultDto> found = dfs(roots, fieldName, value, limit);
        sortIfNeeded(found, sortBy, asc);
        logger.info("DFS finished, found {}", found.size());
        return found;
    }

    @Transactional(readOnly = true)
    public Optional<SearchResultDto> findFunctionByUserName(String userName, String functionName) {
        logger.info("Hierarchical search function={} by user={}", functionName, userName);
        return functionRepository.findByName(functionName)
                .filter(f -> {
                    User u = f.getUser(); // fetch parent (may be lazy)
                    return u != null && userName.equals(u.getName());
                })
                .map(f -> new SearchResultDto("Function", safeGetId(f), f));
    }

    private List<SearchResultDto> bfs(List<Object> roots, String fieldName, String value, int limit) {
        List<SearchResultDto> result = new ArrayList<>();
        Queue<Object> q = new ArrayDeque<>(roots);
        Set<Object> visited = new HashSet<>();
        while (!q.isEmpty() && result.size() < limit) {
            Object cur = q.poll();
            Object id = safeGetId(cur);
            if (id != null && visited.contains(id)) continue;
            if (id != null) visited.add(id);

            if (matchesField(cur, fieldName, value)) {
                result.add(new SearchResultDto(detectType(cur), id, cur));
                if (result.size() >= limit) break;
            }
            q.addAll(childrenOf(cur));
        }
        return result;
    }

    private List<SearchResultDto> dfs(List<Object> roots, String fieldName, String value, int limit) {
        List<SearchResultDto> result = new ArrayList<>();
        Deque<Object> stack = new ArrayDeque<>(roots);
        Set<Object> visited = new HashSet<>();
        while (!stack.isEmpty() && result.size() < limit) {
            Object cur = stack.pop();
            Object id = safeGetId(cur);
            if (id != null && visited.contains(id)) continue;
            if (id != null) visited.add(id);

            if (matchesField(cur, fieldName, value)) {
                result.add(new SearchResultDto(detectType(cur), id, cur));
                if (result.size() >= limit) break;
            }
            List<Object> children = childrenOf(cur);
            for (int i = children.size() - 1; i >= 0; i--) stack.push(children.get(i));
        }
        return result;
    }

    private List<Object> loadRoots(String start) {
        if (start == null) start = "user";
        String s = start.toLowerCase();
        if (s.startsWith("user")) {
            logger.debug("Loading users as roots");
            return new ArrayList<>(userRepository.findAll());
        } else if (s.startsWith("function")) {
            logger.debug("Loading functions as roots");
            return new ArrayList<>(functionRepository.findAll());
        } else if (s.startsWith("point")) {
            logger.debug("Loading points as roots");
            return new ArrayList<>(pointRepository.findAll());
        } else {
            logger.debug("Unknown start, default to users");
            return new ArrayList<>(userRepository.findAll());
        }
    }

    @SuppressWarnings("unchecked")
    private List<Object> childrenOf(Object o) {
        if (o == null) return Collections.emptyList();
        try {
            if (o instanceof User) {
                User u = (User) o;
                if (u.getFunctions() == null) return Collections.emptyList();
                return new ArrayList<>(u.getFunctions());
            } else if (o instanceof FunctionEntity) {
                FunctionEntity f = (FunctionEntity) o;
                if (f.getPoints() == null) return Collections.emptyList();
                return new ArrayList<>(f.getPoints());
            } else if (o instanceof TabulatedPoint) {
                return Collections.emptyList();
            } else {
                Object candidate = tryCall(o, "getFunctions");
                if (candidate instanceof Collection) return new ArrayList<>((Collection<?>) candidate);
                candidate = tryCall(o, "getPoints");
                if (candidate instanceof Collection) return new ArrayList<>((Collection<?>) candidate);
            }
        } catch (Exception e) {
            logger.debug("childrenOf error: {}", e.toString());
        }
        return Collections.emptyList();
    }

    private Object tryCall(Object target, String methodName) {
        try {
            Method m = target.getClass().getMethod(methodName);
            return m.invoke(target);
        } catch (Exception ignored) {}
        return null;
    }

    private boolean matchesField(Object entity, String fieldName, String value) {
        if (entity == null || fieldName == null || value == null) return false;
        Object val = readFieldSafely(entity, fieldName);
        if (val == null) return false;
        try {
            double dVal = Double.parseDouble(value);
            if (val instanceof Number) {
                double dField = ((Number) val).doubleValue();
                return Double.compare(dVal, dField) == 0;
            }
        } catch (Exception ignored) {}
        return value.equalsIgnoreCase(String.valueOf(val));
    }

    private Object readFieldSafely(Object target, String fieldName) {
        if (target == null || fieldName == null) return null;
        String camel = toCamel(fieldName);
        String getter = "get" + Character.toUpperCase(camel.charAt(0)) + camel.substring(1);
        try {
            Method m = target.getClass().getMethod(getter);
            return m.invoke(target);
        } catch (Exception ignored) {}
        List<String> candidates = Arrays.asList(fieldName, camel, fieldName.replaceAll("_", ""), "indexInFunction", "index", "pointIndex");
        for (String cand : candidates) {
            Field f = findFieldInHierarchy(target.getClass(), cand);
            if (f != null) {
                try { f.setAccessible(true); return f.get(target); } catch (Exception ignored) {}
            }
        }
        for (Field f : target.getClass().getDeclaredFields()) {
            if (normalize(f.getName()).equals(normalize(fieldName))) {
                try { f.setAccessible(true); return f.get(target); } catch (Exception ignored) {}
            }
        }
        return null;
    }

    private Field findFieldInHierarchy(Class<?> cls, String name) {
        Class<?> cur = cls;
        while (cur != null && cur != Object.class) {
            try { return cur.getDeclaredField(name); } catch (NoSuchFieldException ignored) {}
            cur = cur.getSuperclass();
        }
        return null;
    }

    private String normalize(String s) {
        return s == null ? "" : s.replaceAll("_", "").toLowerCase();
    }

    private String toCamel(String s) {
        if (s == null) return "";
        String[] parts = s.split("[_\\s]+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            String p = parts[i].toLowerCase();
            if (i == 0) sb.append(p);
            else sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1));
        }
        return sb.toString();
    }

    private Object safeGetId(Object entity) {
        if (entity == null) return null;
        Object id = readFieldSafely(entity, "id");
        if (id != null) return id;
        try {
            Method m = entity.getClass().getMethod("getId");
            return m.invoke(entity);
        } catch (Exception ignored) {}
        return entity.hashCode();
    }

    private String detectType(Object entity) {
        if (entity instanceof User) return "User";
        if (entity instanceof FunctionEntity) return "Function";
        if (entity instanceof TabulatedPoint) return "Point";
        return entity.getClass().getSimpleName();
    }

    private void sortIfNeeded(List<SearchResultDto> results, String sortBy, boolean asc) {
        if (sortBy == null || sortBy.isBlank()) return;
        Comparator<SearchResultDto> cmp = (a, b) -> {
            Object va = readFieldSafely(a.getEntity(), sortBy);
            Object vb = readFieldSafely(b.getEntity(), sortBy);
            if (va == null && vb == null) return 0;
            if (va == null) return -1;
            if (vb == null) return 1;
            if (va instanceof Comparable && vb instanceof Comparable) {
                return ((Comparable) va).compareTo(vb);
            }
            return String.valueOf(va).compareTo(String.valueOf(vb));
        };
        if (!asc) cmp = cmp.reversed();
        results.sort(cmp);
    }
}
