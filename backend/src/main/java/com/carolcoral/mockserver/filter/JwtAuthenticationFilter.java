package com.carolcoral.mockserver.filter;

import com.carolcoral.mockserver.dto.ApiResponse;
import com.carolcoral.mockserver.entity.User;
import com.carolcoral.mockserver.repository.UserRepository;
import com.carolcoral.mockserver.util.JwtTokenUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String SWAGGER_LOGIN_PATH = "/api/auth/swagger-login";
    private static final String MOCK_PATH_PREFIX = "/api/mock/";
    private static final String AUTH_LOGIN_PATH = "/api/auth/login";
    private static final String AUTH_REGISTER_PATH = "/api/auth/register";

    private final JwtTokenUtil jwtTokenUtil;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    // Swagger凭据从环境变量获取
    private static final String SWAGGER_USERNAME = System.getenv().getOrDefault("SWAGGER_USERNAME", "");
    private static final String SWAGGER_PASSWORD = System.getenv().getOrDefault("SWAGGER_PASSWORD", "");

    @Override
    @Operation(summary = "执行JWT认证过滤")
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String requestUri = request.getRequestURI();
        
        // 公开接口 - 设置匿名认证并放行
        if (isPublicPath(requestUri)) {
            setAnonymousAuthentication(request);
            filterChain.doFilter(request, response);
            return;
        }
        
        // Swagger登录接口
        if (SWAGGER_LOGIN_PATH.equals(requestUri)) {
            handleSwaggerLogin(request, response);
            return;
        }

        // Swagger相关路径 - 需要验证
        if (isSwaggerPath(requestUri)) {
            String token = getTokenFromRequest(request);
            if (StringUtils.hasText(token)) {
                // 如果有token，验证token
                if (!validateSwaggerAccess(token, request, response)) {
                    return;
                }
            } else {
                // 如果没有token，返回401
                writeJsonResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                        ApiResponse.unauthorized());
                return;
            }
        }

        // Mock接口路径 - 不需要认证
        if (requestUri.startsWith(MOCK_PATH_PREFIX)) {
            setAnonymousAuthentication(request);
            filterChain.doFilter(request, response);
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

                if (username != null && userId != null) {
                    UserDetails userDetails = userRepository.findByUsername(username).orElse(null);

                    if (userDetails != null && jwtTokenUtil.validateToken(token, userDetails)) {
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);

                        // 设置用户ID到请求属性，方便后续使用
                        request.setAttribute("userId", userId);
                        log.debug("JWT认证成功: username={}", username);
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
            log.debug("请求没有提供JWT token: {}", requestUri);
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
        return AUTH_LOGIN_PATH.equals(requestUri) || 
               AUTH_REGISTER_PATH.equals(requestUri) ||
               requestUri.equals("/error");
    }

    /**
     * 处理Swagger登录
     */
    private void handleSwaggerLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        // 检查环境变量是否配置
        if (SWAGGER_USERNAME.isEmpty() || SWAGGER_PASSWORD.isEmpty()) {
            writeJsonResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    ApiResponse.error("Swagger登录未配置，请设置环境变量SWAGGER_USERNAME和SWAGGER_PASSWORD"));
            return;
        }

        if (SWAGGER_USERNAME.equals(username) && SWAGGER_PASSWORD.equals(password)) {
            String token = jwtTokenUtil.generateToken(
                    org.springframework.security.core.userdetails.User.builder()
                            .username(username)
                            .password("")
                            .roles("ADMIN")
                            .build(),
                    0L,
                    "ADMIN"
            );

            ApiResponse<com.carolcoral.mockserver.dto.LoginResponse> successResponse = ApiResponse.success(
                    com.carolcoral.mockserver.dto.LoginResponse.builder()
                            .token(token)
                            .tokenType("Bearer")
                            .username(username)
                            .role("ADMIN")
                            .build()
            );

            writeJsonResponse(response, HttpServletResponse.SC_OK, successResponse);
        } else {
            writeJsonResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                    ApiResponse.error("用户名或密码错误"));
        }
    }

    /**
     * 验证Swagger访问权限
     */
    private boolean validateSwaggerAccess(String token, HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!StringUtils.hasText(token) || !jwtTokenUtil.validateToken(token)) {
            writeJsonResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                    ApiResponse.unauthorized());
            return false;
        }
        return true;
    }

    /**
     * 判断是否是Swagger相关路径
     */
    private boolean isSwaggerPath(String requestUri) {
        return requestUri.contains("/v3/api-docs") || 
               requestUri.contains("/swagger-ui") ||
               requestUri.contains("/swagger-resources") ||
               requestUri.contains("/webjars") ||
               requestUri.equals("/api/swagger-login");
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
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }

        // 2. 尝试从URL参数获取（用于Swagger等场景）
        String tokenParam = request.getParameter("token");
        if (StringUtils.hasText(tokenParam)) {
            return tokenParam;
        }

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
