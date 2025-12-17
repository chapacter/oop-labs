package ru.ssau.tk.avokado.lab2.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.ssau.tk.avokado.lab2.dto.UserDto;

import java.io.IOException;
import java.util.Set;

@WebFilter("/*")
public class AuthorizationFilter implements Filter {
    private static final Logger logger = LoggerFactory.getLogger(AuthorizationFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Получаем информацию о пользователе из атрибута, установленного AuthenticationFilter
        UserDto authenticatedUser = (UserDto) httpRequest.getAttribute("authenticatedUser");

        String path = httpRequest.getRequestURI().substring(httpRequest.getContextPath().length());
        String method = httpRequest.getMethod();

        // Пропускаем фильтр для публичных URL
        if (isPublicUrl(path)) {
            chain.doFilter(request, response);
            return;
        }

        // Если пользователь не аутентифицирован, прерываем выполнение
        if (authenticatedUser == null) {
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.setContentType("application/json");
            httpResponse.getWriter().write("{\"error\":\"User not authenticated\"}");
            logger.warn("Access attempt to {} {} without authentication", method, path);
            return;
        }

        // Проверяем права доступа к ресурсу
        if (!hasAccess(authenticatedUser, path, method)) {
            httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
            httpResponse.setContentType("application/json");
            httpResponse.getWriter().write("{\"error\":\"Access denied\"}");
            logger.warn("User {} denied access to {} {} - insufficient role permissions",
                    authenticatedUser.getName(), method, path);
            return;
        }

        // Проверяем права на основе владельца ресурса (если применимо)
        if (!hasResourceOwnershipAccess(authenticatedUser, httpRequest, path, method)) {
            httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
            httpResponse.setContentType("application/json");
            httpResponse.getWriter().write("{\"error\":\"Access denied - insufficient permissions\"}");
            logger.warn("User {} denied access to resource based on ownership for {} {}",
                    authenticatedUser.getName(), method, path);
            return;
        }

        logger.info("User {} authorized for {} {}", authenticatedUser.getName(), method, path);
        chain.doFilter(request, response);
    }

    private boolean isPublicUrl(String path) {
        // Определяем список публичных URL, которые не требуют авторизации
        return path.equals("/") ||
                path.equals("/index.html") ||
                path.startsWith("/api-docs") ||
                path.startsWith("/api-documentation") ||
                path.startsWith("/css/") ||
                path.startsWith("/js/") ||
                path.equals("/web.xml") ||
                path.equals("/WEB-INF/web.xml") ||
                path.startsWith("/api/auth") ||
                path.startsWith("/api/test-");
    }

    private boolean hasAccess(UserDto user, String path, String method) {
        Set<String> userRoles = user.getRoles();

        // Определяем права доступа на основе ролей пользователя
        if (path.startsWith("/api/admin")) {
            // Только пользователи с ролью ADMIN могут получить доступ к административным API
            return userRoles.contains("ADMIN");
        } else if (path.startsWith("/api/users")) {
            // Только пользователи с ролью ADMIN могут управлять пользователями
            return userRoles.contains("ADMIN");
        } else if (path.startsWith("/api/functions") || path.startsWith("/api/points") ||
                path.startsWith("/api/operations") || path.startsWith("/api/processed") ||
                path.startsWith("/api/results") || path.startsWith("/api/tabulated")) {
            // Все аутентифицированные пользователи могут использовать API функций, точек и т.д.
            return true;
        } else {
            // Для всех остальных путей разрешаем доступ аутентифицированным пользователям
            return true;
        }
    }

    private boolean hasResourceOwnershipAccess(UserDto user, HttpServletRequest request, String path, String method) {
        Set<String> userRoles = user.getRoles();

        // Администраторы имеют полный доступ
        if (userRoles.contains("ADMIN")) {
            return true;
        }

        // Проверяем, является ли пользователь владельцем ресурса для операций с функциями, точками и т.д.
        if (path.startsWith("/api/functions") && !path.equals("/api/functions") && !path.startsWith("/api/functions/by-user")) {
            // Для операций с конкретными функциями проверяем принадлежность пользователю
            if (method.equals("GET") || method.equals("PUT") || method.equals("DELETE")) {
                // В реальной реализации здесь должна быть проверка, является ли пользователь владельцем функции
                // Пока возвращаем true, но в будущем нужно реализовать проверку владельца
                return true;
            }
        }

        // Для других путей, где может потребоваться проверка владения
        if (path.startsWith("/api/points") && !path.equals("/api/points")) {
            if (method.equals("GET") || method.equals("PUT") || method.equals("DELETE")) {
                // Здесь должна быть проверка, является ли пользователь владельцем точки
                return true;
            }
        }

        if (path.startsWith("/api/operations") && !path.equals("/api/operations")) {
            if (method.equals("GET") || method.equals("PUT") || method.equals("DELETE")) {
                // Здесь должна быть проверка, является ли пользователь владельцем операции
                return true;
            }
        }

        if (path.startsWith("/api/processed") && !path.equals("/api/processed")) {
            if (method.equals("GET") || method.equals("PUT") || method.equals("DELETE")) {
                // Здесь должна быть проверка, является ли пользователь владельцем обработанной функции
                return true;
            }
        }

        if (path.startsWith("/api/results") && !path.equals("/api/results")) {
            if (method.equals("GET") || method.equals("PUT") || method.equals("DELETE")) {
                // Здесь должна быть проверка, является ли пользователь владельцем результата
                return true;
            }
        }

        if (path.startsWith("/api/tabulated") && !path.equals("/api/tabulated")) {
            if (method.equals("GET") || method.equals("PUT") || method.equals("DELETE")) {
                // Здесь должна быть проверка, является ли пользователь владельцем табулированной функции
                return true;
            }
        }

        // Для всех остальных случаев разрешаем доступ
        return true;
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }
}