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
    private static final String SWAGGER_AUTO_LOGIN_PATH = "/api/auth/swagger-auto-login";
    private static final String MOCK_PATH_PREFIX = "/api/mock/";
    private static final String AUTH_LOGIN_PATH = "/api/auth/login";
    private static final String AUTH_REGISTER_PATH = "/api/auth/register";

    private final JwtTokenUtil jwtTokenUtil;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    // Swagger凭据从系统属性（由.env文件加载）获取
    private static final String SWAGGER_USERNAME = System.getProperty("SWAGGER_USERNAME", "");
    private static final String SWAGGER_PASSWORD = System.getProperty("SWAGGER_PASSWORD", "");

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
        
        // Swagger自动登录接口（前端已登录用户调用）
        if (SWAGGER_AUTO_LOGIN_PATH.equals(requestUri)) {
            handleSwaggerAutoLogin(request, response);
            return;
        }



        // Mock接口路径 - 不需要认证
        if (requestUri.startsWith(MOCK_PATH_PREFIX)) {
            setAnonymousAuthentication(request);
            filterChain.doFilter(request, response);
            return;
        }

        // Swagger相关路径 - 需要认证，但token无效时重定向到登录页
        if (isSwaggerPath(requestUri)) {
            String token = getTokenFromRequest(request);
            if (StringUtils.hasText(token) && jwtTokenUtil.validateToken(token)) {
                // token有效，设置认证并放行
                String username = jwtTokenUtil.getUsernameFromToken(token);
                Long userId = jwtTokenUtil.getUserIdFromToken(token);
                
                if (username != null && userId != null) {
                    UserDetails userDetails = userRepository.findByUsername(username).orElse(null);
                    if (userDetails != null) {
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        request.setAttribute("userId", userId);
                        log.debug("Swagger访问认证成功: username={}", username);
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
            String role = jwtTokenUtil.getRoleFromToken(userToken);
            
            if (username == null || userId == null) {
                writeJsonResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                        ApiResponse.error("无法从令牌中获取用户信息"));
                return;
            }
            
            // 检查环境变量是否配置（用于Swagger访问）
            if (SWAGGER_USERNAME.isEmpty() || SWAGGER_PASSWORD.isEmpty()) {
                writeJsonResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        ApiResponse.error("Swagger登录未配置，请设置环境变量SWAGGER_USERNAME和SWAGGER_PASSWORD"));
                return;
            }
            
            // 生成Swagger专用token（使用Swagger配置的用户信息）
            String swaggerToken = jwtTokenUtil.generateToken(
                    org.springframework.security.core.userdetails.User.builder()
                            .username(SWAGGER_USERNAME)
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
                            .username(SWAGGER_USERNAME)
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
