/**
* Copyright (c) 2026, XINDU.SITE
* All Rights Reserved.
* XINDU.SITE CONFIDENTIAL
*/

package com.carolcoral.mockserver.config;

import com.carolcoral.mockserver.filter.JwtAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cdimascio.dotenv.Dotenv;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.servlet.http.HttpServletResponse;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Spring Security配置类
 *
 * @author carolcoral
 */
@Tag(name = "安全配置", description = "Spring Security配置")
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ObjectMapper objectMapper;
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SecurityConfig.class);

    /**
     * 构造器
     */
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          ObjectMapper objectMapper) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.objectMapper = objectMapper;
    }

    /**
     * 配置安全过滤器链
     *
     * @param http HttpSecurity
     * @return SecurityFilterChain
     * @throws Exception 异常
     */
    @Bean
    @Operation(summary = "配置安全过滤器链")
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 特别注意：CORS必须在所有其他配置之前
                .cors(cors -> {
                    cors.configurationSource(corsConfigurationSource());
                })
                .csrf(AbstractHttpConfigurer::disable)
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::deny))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authz -> authz
                        // 允许所有OPTIONS预检请求（必须放在最前面，确保CORS预检不被后续权限规则拦截）
                        .requestMatchers(request -> "OPTIONS".equals(request.getMethod())).permitAll()
                        // 完全公开接口（不需要认证）
                        .requestMatchers(
                                // API 接口
                                "/api/auth/**",
                                "/api/public/**",
                                "/api/mock-server/**",
                                "/api/mock/**",
                                "/api/ws/**",
                                "/api/error",
                                "/api/actuator/**",
                                "/api/system/version",
                                // Bing 图片代理
                                "/bing-hp",
                                // 前端静态资源
                                "/",
                                "/index.html",
                                "/favicon.ico",
                                "/robots.txt",
                                "/assets/**",
                                "/badges/**",
                                "/USER_GUIDE.md",
                                "/CHANGELOG.md",
                                "/README.md",
                                // 前端静态文件（图片、字体等）
                                "/*.jpg",
                                "/*.jpeg",
                                "/*.png",
                                "/*.svg",
                                "/*.gif",
                                "/*.webp",
                                "/*.ico",
                                "/*.woff",
                                "/*.woff2",
                                "/*.ttf",
                                "/*.css",
                                "/*.js",
                                "/*.map",
                                // SPA 路由（Vue Router history 模式）
                                "/login",
                                "/register",
                                "/dashboard",
                                "/home",
                                "/projects",
                                "/projects/**",
                                "/apis",
                                "/users",
                                "/settings",
                                "/statistics",
                                "/guide",
                                "/profile",
                                "/code-templates",
                                "/changelog",
                                "/email-templates",
                                "/ai-settings",
                                "/ai-chat"
                        ).permitAll()
                        // Swagger 静态资源 - 公开访问（页面加载 CSS/JS 需要）
                        .requestMatchers(
                                "/api/swagger-resources/**",
                                "/api/webjars/**",
                                "/api/swagger-ui/**",
                                // Swagger 静态资源（不带 /api 前缀，springdoc 默认路径）
                                "/swagger-resources/**",
                                "/webjars/**",
                                "/swagger-ui/**"
                        ).permitAll()
                        // Swagger 页面入口和 API 文档数据 - 需要管理员权限
                        .requestMatchers(
                                "/api/v3/api-docs/**",
                                "/api/swagger-ui.html",
                                // Swagger 路径（不带 /api 前缀，springdoc 默认路径）
                                "/v3/api-docs/**",
                                "/swagger-ui.html"
                        ).hasRole("ADMIN")
                        // 系统配置读写接口 - 需要管理员权限
                        .requestMatchers(
                                "/api/system-config/language",
                                "/api/system-config/date-format",
                                "/api/system-config/registration"
                        ).hasRole("ADMIN")
                        // 邮箱配置接口 - 需要管理员权限
                        .requestMatchers("/api/email-config/**").hasRole("ADMIN")
                        // 邮件模板管理接口 - 需要管理员权限
                        .requestMatchers("/api/email-templates/**").hasRole("ADMIN")
                        // AI配置接口 - 需要管理员权限
                        .requestMatchers("/api/ai-config/**").hasRole("ADMIN")
                        // AI功能接口 - 需要认证（所有登录用户均可使用）
                        .requestMatchers("/api/ai/**").authenticated()
                        // 用户信息接口 - 需要认证
                        .requestMatchers(
                                "/api/users/profile",
                                "/api/users/update-profile",
                                "/api/users/change-password",
                                "/api/system/info"
                        ).authenticated()
                        // 系统配置读取接口 - 需要认证
                        .requestMatchers(
                                "/api/system-config",
                                "/api/system-config/footer"
                        ).authenticated()
                        // Mock API管理接口 - 需要认证
                        .requestMatchers("/api/mock-apis/**").authenticated()
                        // 其他所有请求需要认证
                        .anyRequest().authenticated()
                )
                // JWT过滤器 - 只拦截需要认证的请求
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                // 自定义异常处理 - 返回JSON格式错误
                .exceptionHandling(exceptions -> exceptions
                        .accessDeniedHandler(accessDeniedHandler())
                );

        return http.build();
    }

    /**
     * 配置跨域
     *
     * @return CorsConfigurationSource
     */
    @Bean
    @Operation(summary = "配置跨域")
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // 允许所有来源访问
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        
        // 明确允许的HTTP方法
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH", "HEAD"));
        
        // 明确允许的请求头
        configuration.setAllowedHeaders(Arrays.asList("*"));
        
        // 暴露的响应头
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));
        
        // 禁用凭据支持，避免CORS问题
        configuration.setAllowCredentials(false);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * 配置密码编码器
     *
     * @return PasswordEncoder
     */
    @Bean
    @Operation(summary = "配置密码编码器")
    public PasswordEncoder passwordEncoder() {
        // 使用12轮BCrypt以提高安全性
        return new BCryptPasswordEncoder(12);
    }

    /**
     * 配置认证管理器
     *
     * @param configuration AuthenticationConfiguration
     * @return AuthenticationManager
     * @throws Exception 异常
     */
    @Bean
    @Operation(summary = "配置认证管理器")
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    /**
     * 自定义访问拒绝处理器 - 返回JSON格式403响应
     */
    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            log.warn("访问被拒绝(accessDeniedHandler): {} {}, 用户: {}, 角色: {}, 异常: {}",
                    request.getMethod(), request.getRequestURI(),
                    request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : "未知",
                    request.isUserInRole("ADMIN") ? "ADMIN" : "非ADMIN",
                    accessDeniedException.getMessage());

            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json;charset=UTF-8");

            Map<String, Object> body = new HashMap<>();
            body.put("code", 403);
            body.put("message", "权限不足，需要管理员权限才能执行此操作");

            response.getWriter().write(objectMapper.writeValueAsString(body));
        };
    }
}
