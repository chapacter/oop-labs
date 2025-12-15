package ru.ssau.tk.avokado.lab2.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.Customizer;

@Configuration
@EnableMethodSecurity // разрешает @PreAuthorize и т.п.
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    private final UserDetailsService userDetailsService;

    public SecurityConfig(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // выключаем CSRF для REST API
                .csrf(csrf -> csrf.disable())
                // Тут важный кусок: разрешаем публичный доступ к /api/auth/** и /api/admin/** (временно)
                .authorizeHttpRequests(authorize -> authorize
                        // регистрация/логин — открыты
                        .requestMatchers("/api/auth/**").permitAll()
                        // actuator (если есть) — открыты
                        .requestMatchers("/actuator/**").permitAll()
                        // ВАЖНО: сделать /api/admin/** permitAll для запуска populate/clear без авторизации
                        // (после тестов можно вернуть .hasRole("ADMIN") и потребовать авторизацию)
                        .requestMatchers("/api/admin/**").permitAll()
                        // все остальные требуют аутентификации
                        .anyRequest().authenticated()
                )
                // Basic Auth включаем
                .httpBasic(Customizer.withDefaults());

        logger.info("SecurityFilterChain configured (Basic Auth; admin endpoints temporarily permitAll)");
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // если где-то нужен AuthenticationManager
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
