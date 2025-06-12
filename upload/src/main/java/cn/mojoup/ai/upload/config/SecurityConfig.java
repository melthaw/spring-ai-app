package cn.mojoup.ai.upload.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security配置类
 * 
 * @author matt
 */
@Slf4j
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 禁用CSRF（开发环境，生产环境需要根据实际情况配置）
            .csrf(AbstractHttpConfigurer::disable)
            
            // 配置请求授权
            .authorizeHttpRequests(authz -> authz
                // Swagger相关路径允许匿名访问
                .requestMatchers(
                    "/swagger-ui/**",
                    "/swagger-ui.html", 
                    "/v3/api-docs/**",
                    "/webjars/**"
                ).permitAll()
                
                // 文件上传相关接口需要认证
                .requestMatchers("/api/upload/**").authenticated()
                
                // 其他请求允许匿名访问（开发环境）
                .anyRequest().permitAll()
            )
            
            // 使用HTTP Basic认证（开发环境，生产环境建议使用JWT等）
            .httpBasic(httpBasic -> {
                log.info("HTTP Basic authentication configured");
            })
            
            // 配置异常处理
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint((request, response, authException) -> {
                    log.warn("Authentication failed for request: {}", request.getRequestURI());
                    response.setStatus(401);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"error\":\"Authentication required\",\"message\":\"请先登录\"}");
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    log.warn("Access denied for request: {}", request.getRequestURI());
                    response.setStatus(403);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"error\":\"Access denied\",\"message\":\"权限不足\"}");
                })
            );
        
        log.info("Security filter chain configured successfully");
        return http.build();
    }
} 