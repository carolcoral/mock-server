package com.carolcoral.mockserver.controller;

import com.carolcoral.mockserver.dto.ApiResponse;
import com.carolcoral.mockserver.dto.LoginRequest;
import com.carolcoral.mockserver.dto.LoginResponse;
import com.carolcoral.mockserver.service.UserService;
import com.carolcoral.mockserver.util.JwtTokenUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

/**
 * 认证控制器
 *
 * @author carolcoral
 */
@Tag(name = "认证管理", description = "用户认证相关接口")
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtTokenUtil jwtTokenUtil;

    /**
     * 用户登录
     *
     * @param loginRequest 登录请求
     * @return 登录响应
     */
    @Operation(summary = "用户登录", description = "用户登录接口，返回JWT令牌")
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Parameter(description = "登录请求") @Valid @RequestBody LoginRequest loginRequest) {
        log.info("用户登录请求: {}", loginRequest.getUsername());
        return userService.login(loginRequest);
    }

    /**
     * Swagger登录（兼容Swagger UI认证）
     *
     * @param loginRequest 登录请求
     * @return 登录响应
     */
    @Operation(summary = "Swagger登录", description = "Swagger UI专用登录接口")
    @PostMapping("/swagger-login")
    public ApiResponse<LoginResponse> swaggerLogin(@Parameter(description = "登录请求") @Valid @RequestBody LoginRequest loginRequest) {
        log.info("Swagger登录请求: {}", loginRequest.getUsername());
        return userService.login(loginRequest);
    }

    /**
     * 验证Swagger访问权限（已登录用户）
     *
     * @param token JWT令牌
     * @return 验证结果
     */
    @Operation(summary = "验证Swagger访问权限", description = "验证已登录用户的token，授权访问Swagger UI")
    @PostMapping("/verify-swagger-access")
    public ApiResponse<Boolean> verifySwaggerAccess(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                return ApiResponse.error("未提供有效的认证令牌");
            }
            
            String token = authorizationHeader.substring(7); // 移除 "Bearer "
            
            // 验证token是否有效
            if (!jwtTokenUtil.validateToken(token)) {
                return ApiResponse.error("令牌无效或已过期");
            }
            
            log.info("Swagger访问权限验证成功");
            return ApiResponse.success(true);
            
        } catch (Exception e) {
            log.error("Swagger访问权限验证失败: {}", e.getMessage());
            return ApiResponse.error("验证失败：" + e.getMessage());
        }
    }
}
