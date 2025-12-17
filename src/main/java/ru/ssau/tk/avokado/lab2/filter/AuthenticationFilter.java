package ru.ssau.tk.avokado.lab2.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.ssau.tk.avokado.lab2.dto.UserDto;
import ru.ssau.tk.avokado.lab2.service.UserService;

import java.io.IOException;
import java.util.Base64;
import java.util.Optional;

@WebFilter("/*")
public class AuthenticationFilter implements Filter {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);
    private final UserService userService = new UserService();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String path = httpRequest.getRequestURI().substring(httpRequest.getContextPath().length());

        if (isPublicUrl(path)) {
            chain.doFilter(request, response);
            return;
        }

        String authHeader = httpRequest.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Basic ")) {
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.setContentType("application/json");
            httpResponse.getWriter().write("{\"error\":\"Authorization header is required\"}");
            logger.warn("Unauthorized access attempt to: {} - missing or invalid Authorization header", path);
            return;
        }

        try {
            String encodedCredentials = authHeader.substring("Basic ".length());
            String decodedCredentials = new String(Base64.getDecoder().decode(encodedCredentials));
            String[] credentials = decodedCredentials.split(":", 2);

            if (credentials.length != 2) {
                httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                httpResponse.setContentType("application/json");
                httpResponse.getWriter().write("{\"error\":\"Invalid credentials format\"}");
                logger.warn("Invalid credentials format for path: {} - expected username:password", path);
                return;
            }

            String username = credentials[0];
            String password = credentials[1];

            Optional<UserDto> user = userService.findByUsernameAndPassword(username, password);

            if (user.isEmpty()) {
                httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                httpResponse.setContentType("application/json");
                httpResponse.getWriter().write("{\"error\":\"Invalid username or password\"}");
                logger.warn("Authentication failed for user: {} attempting to access: {}", username, path);
                return;
            }

            httpRequest.setAttribute("authenticatedUser", user.get());
            logger.info("User {} successfully authenticated for path: {}", username, path);

            chain.doFilter(request, response);

        } catch (IllegalArgumentException e) {
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.setContentType("application/json");
            httpResponse.getWriter().write("{\"error\":\"Invalid authorization header\"}");
            logger.warn("Invalid authorization header for path: {}", path, e);
        } catch (Exception e) {
            httpResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            httpResponse.setContentType("application/json");
            httpResponse.getWriter().write("{\"error\":\"Internal server error\"}");
            logger.error("Error during authentication for path: {}", path, e);
        }
    }

    private boolean isPublicUrl(String path) {
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

    @Override
    public void destroy() {
        Filter.super.destroy();
    }
}