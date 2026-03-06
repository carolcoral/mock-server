/**
* Copyright (c) 2026, XINDU.SITE
* All Rights Reserved.
* XINDU.SITE CONFIDENTIAL
*/

package com.carolcoral.mockserver.config;

import com.carolcoral.mockserver.filter.JwtAuthenticationFilter;
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
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

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

    /**
     * 构造器
     */
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
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
                        // 完全公开接口（不需要认证）- 注意：由于设置了context-path=/api，这些路径实际上是相对于/api的
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/webjars/**",
                                "/auth/login",
                                "/auth/register",
                                "/auth/swagger-auto-login",
                                "/mock-server/**",
                                "/mock/**",
                                "/ws/**",
                                "/error",
                                "/api/v3/api-docs/**",
                                "/api/swagger-resources/**",
                                "/api/webjars/**",
                                "/api/auth/**",
                                "/api/mock-server/**",
                                "/api/mock/**",
                                "/api/ws/**",
                                "/api/error",
                                "/api/system-config/**"
                        ).permitAll()
                        // Swagger UI 页面（需要认证，但由JWT过滤器处理）
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/api/swagger-ui/**",
                                "/api/swagger-ui.html"
                        ).authenticated()
                        // 系统配置接口 - 需要管理员权限
                        .requestMatchers(
                                "/api/system-config/language"
                        ).hasRole("ADMIN")
                        // 用户信息接口 - 需要认证
                        .requestMatchers(
                                "/api/user/profile",
                                "/api/user/update-profile",
                                "/api/user/change-password"
                        ).authenticated()
                        // 允许所有OPTIONS预检请求
                        .requestMatchers(request -> "OPTIONS".equals(request.getMethod())).permitAll()
                        // 其他所有请求需要认证
                        .anyRequest().authenticated()
                )
                // JWT过滤器 - 只拦截需要认证的请求
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

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
}
