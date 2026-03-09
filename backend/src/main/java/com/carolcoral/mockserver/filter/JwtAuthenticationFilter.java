/**
* Copyright (c) 2026, XINDU.SITE
* All Rights Reserved.
* XINDU.SITE CONFIDENTIAL
*/

package com.carolcoral.mockserver.filter;

import com.carolcoral.mockserver.dto.ApiResponse;
import com.carolcoral.mockserver.entity.User;
import com.carolcoral.mockserver.repository.UserRepository;
import com.carolcoral.mockserver.util.JwtTokenUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cdimascio.dotenv.Dotenv;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * JWT认证过滤器
 *
 * @author carolcoral
 */
@Tag(name = "JWT过滤器", description = "JWT认证过滤器")
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    /**
     * 构造器
     */
    public JwtAuthenticationFilter(JwtTokenUtil jwtTokenUtil,
        UserRepository userRepository,
        ObjectMapper objectMapper) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String SWAGGER_AUTO_LOGIN_PATH = "/auth/swagger-auto-login";
    private static final String MOCK_PATH_PREFIX = "/mock/";
    private static final String MOCK_SERVER_PATH_PREFIX = "/mock-server/";
    private static final String AUTH_LOGIN_PATH = "/auth/login";
    private static final String AUTH_REGISTER_PATH = "/auth/register";
    private static final String ACTUATOR_PATH_PREFIX = "/actuator/";

    private final JwtTokenUtil jwtTokenUtil;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Operation(summary = "执行JWT认证过滤")
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String requestUri = request.getRequestURI();
        String servletPath = request.getServletPath(); // 去除context-path后的路径

        // 公开接口 - 直接放行，不设置认证
        if (isPublicPath(requestUri)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Swagger自动登录接口（前端已登录用户调用）
        if (SWAGGER_AUTO_LOGIN_PATH.equals(requestUri) || SWAGGER_AUTO_LOGIN_PATH.equals(servletPath)) {
            handleSwaggerAutoLogin(request, response);
            return;
        }



        // Mock接口路径 - 不需要认证
        if (servletPath.startsWith(MOCK_PATH_PREFIX) || requestUri.startsWith(MOCK_PATH_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Mock Server接口路径 - 不需要认证
        if (servletPath.startsWith(MOCK_SERVER_PATH_PREFIX) || requestUri.startsWith(MOCK_SERVER_PATH_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Swagger相关路径 - 需要认证，但token无效时重定向到登录页
        if (isSwaggerPath(requestUri) || isSwaggerPath(servletPath)) {
                String token = getTokenFromRequest(request);
                if (StringUtils.hasText(token) && jwtTokenUtil.validateToken(token)) {
                    // token有效，设置认证并放行
                    String username = jwtTokenUtil.getUsernameFromToken(token);
                    Long userId = jwtTokenUtil.getUserIdFromToken(token);
                    String role = jwtTokenUtil.getUserRoleFromToken(token);

                    if (username != null && userId != null && role != null) {
                        UserDetails userDetails = userRepository.findByUsername(username).orElse(null);
                        if (userDetails != null) {
                            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());
                            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                            request.setAttribute("userId", userId);
                            request.setAttribute("userRole", User.UserRole.valueOf(role));
                            log.debug("Swagger访问认证成功: username={}, role={}", username, role);
                        }
                    }
                    filterChain.doFilter(request, response);
            } else {
                // token无效或不存在，返回401（Swagger UI会显示认证按钮）
                if (!StringUtils.hasText(token)) {
                    log.warn("Swagger访问未提供token: {}", requestUri);
                } else {
                    log.warn("Swagger访问token无效: {}", requestUri);
                }
                writeJsonResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                        ApiResponse.unauthorized());
            }
            return;
        }

        // 其他接口 - 需要JWT认证
        String token = getTokenFromRequest(request);
        if (StringUtils.hasText(token)) {
            try {
                // 首先验证token是否有效（包括过期检查）
                if (!jwtTokenUtil.validateToken(token)) {
                    log.warn("JWT token无效或已过期");
                    writeJsonResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                            ApiResponse.unauthorized());
                    return;
                }
                
                String username = jwtTokenUtil.getUsernameFromToken(token);
                Long userId = jwtTokenUtil.getUserIdFromToken(token);
                String role = jwtTokenUtil.getUserRoleFromToken(token);

                if (username != null && userId != null) {
                    UserDetails userDetails = userRepository.findByUsername(username).orElse(null);

                    if (userDetails != null && jwtTokenUtil.validateToken(token, userDetails)) {
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);

                        // 设置用户ID和角色到请求属性，方便后续使用
                        request.setAttribute("userId", userId);
                        request.setAttribute("userRole", User.UserRole.valueOf(role));
                        log.debug("JWT认证成功: username={}, role={}", username, role);
                    } else {
                        log.warn("JWT token验证失败或用户不存在");
                        writeJsonResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                                ApiResponse.unauthorized());
                        return;
                    }
                } else {
                    log.warn("JWT token中无法获取用户名或用户ID");
                    writeJsonResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                            ApiResponse.unauthorized());
                    return;
                }
            } catch (Exception e) {
                log.warn("JWT认证失败: {}", e.getMessage());
                writeJsonResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                        ApiResponse.unauthorized());
                return;
            }
        } else {
            // 没有提供token，且不是公开接口，返回401未授权
            log.debug("请求没有提供JWT token: {}", requestUri);
            writeJsonResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                    ApiResponse.unauthorized());
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 设置匿名认证到SecurityContext（用于公开接口）
     */
    private void setAnonymousAuthentication(HttpServletRequest request) {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            UsernamePasswordAuthenticationToken anonymousAuth = new UsernamePasswordAuthenticationToken(
                    "anonymous", null, java.util.Collections.emptyList());
            anonymousAuth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(anonymousAuth);
        }
    }

    /**
     * 判断是否是公开路径（不需要认证）
     */
    private boolean isPublicPath(String requestUri) {
        // 支持完整路径和去除context-path后的路径
        return AUTH_LOGIN_PATH.equals(requestUri) ||
               ("/api" + AUTH_LOGIN_PATH).equals(requestUri) ||
               AUTH_REGISTER_PATH.equals(requestUri) ||
               ("/api" + AUTH_REGISTER_PATH).equals(requestUri) ||
               requestUri.startsWith(MOCK_SERVER_PATH_PREFIX) ||
               requestUri.startsWith("/api" + MOCK_SERVER_PATH_PREFIX) ||
               requestUri.startsWith(ACTUATOR_PATH_PREFIX) ||
               requestUri.startsWith("/api" + ACTUATOR_PATH_PREFIX) ||
               requestUri.equals("/error") ||
               requestUri.equals("/api/error");
    }

    /**
     * 处理Swagger自动登录（已登录用户调用）
     */
    private void handleSwaggerAutoLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            // 从请求头获取前端传递的JWT token
            String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);
            if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
                writeJsonResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                        ApiResponse.error("未提供有效的认证令牌"));
                return;
            }
            
            String userToken = authorizationHeader.substring(BEARER_PREFIX.length());
            
            // 验证用户token是否有效
            if (!jwtTokenUtil.validateToken(userToken)) {
                writeJsonResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                        ApiResponse.error("用户令牌无效或已过期"));
                return;
            }
            
            // 获取用户信息
            String username = jwtTokenUtil.getUsernameFromToken(userToken);
            Long userId = jwtTokenUtil.getUserIdFromToken(userToken);
            String role = jwtTokenUtil.getUserRoleFromToken(userToken);
            
            if (username == null || userId == null) {
                writeJsonResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                        ApiResponse.error("无法从令牌中获取用户信息"));
                return;
            }
            
            // 从系统属性获取Swagger凭据（由.env文件加载）
            String swaggerUsername = System.getProperty("SWAGGER_USERNAME");
            String swaggerPassword = System.getProperty("SWAGGER_PASSWORD");
            
            // 添加调试日志
            log.debug("Swagger认证配置 - username: {}, password: {}", 
                     swaggerUsername != null ? "已设置" : "未设置",
                     swaggerPassword != null ? "已设置" : "未设置");
            
            // 检查环境变量是否配置（用于Swagger访问）
            if (swaggerUsername == null || swaggerUsername.isEmpty() || swaggerPassword == null || swaggerPassword.isEmpty()) {
                log.error("Swagger登录未配置，当前username: {}, password: {}", swaggerUsername, swaggerPassword);
                writeJsonResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        ApiResponse.error("Swagger登录未配置，请设置环境变量SWAGGER_USERNAME和SWAGGER_PASSWORD"));
                return;
            }
            
            // 生成Swagger专用token（使用Swagger配置的用户信息）
            String swaggerToken = jwtTokenUtil.generateToken(
                    org.springframework.security.core.userdetails.User.builder()
                            .username(swaggerUsername)
                            .password("")
                            .roles("ADMIN")
                            .build(),
                    0L,
                    "ADMIN"
            );
            
            log.info("Swagger自动登录成功: user={}, role={}", username, role);
            
            ApiResponse<com.carolcoral.mockserver.dto.LoginResponse> successResponse = ApiResponse.success(
                    com.carolcoral.mockserver.dto.LoginResponse.builder()
                            .token(swaggerToken)
                            .tokenType("Bearer")
                            .username(swaggerUsername)
                            .role("ADMIN")
                            .build()
            );
            
            writeJsonResponse(response, HttpServletResponse.SC_OK, successResponse);
            
        } catch (Exception e) {
            log.error("Swagger自动登录失败: {}", e.getMessage(), e);
            writeJsonResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    ApiResponse.error("Swagger自动登录失败：" + e.getMessage()));
        }
    }

    /**
     * 判断是否是Swagger相关路径
     */
    private boolean isSwaggerPath(String requestUri) {
        return requestUri.contains("/v3/api-docs") || 
               requestUri.contains("/swagger-ui") ||
               requestUri.contains("/swagger-resources") ||
               requestUri.contains("/webjars");
    }



    /**
     * 从请求中获取令牌
     *
     * @param request 请求
     * @return 令牌
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        // 1. 尝试从Header获取
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        log.debug("Authorization Header: {}", bearerToken);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            String token = bearerToken.substring(BEARER_PREFIX.length());
            log.debug("从Header提取token: {}", token != null ? "token存在" : "token为空");
            return token;
        }

        // 2. 尝试从URL参数获取（用于Swagger等场景）
        String tokenParam = request.getParameter("token");
        if (StringUtils.hasText(tokenParam)) {
            log.debug("从URL参数提取token");
            return tokenParam;
        }

        log.warn("请求未提供token, URI: {}", request.getRequestURI());
        return null;
    }

    /**
     * 写入JSON响应
     */
    private void writeJsonResponse(HttpServletResponse response, int status, ApiResponse<?> apiResponse) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        try (PrintWriter writer = response.getWriter()) {
            writer.write(objectMapper.writeValueAsString(apiResponse));
            writer.flush();
        }
    }
}
