package com.carolcoral.mockserver.controller;

import com.carolcoral.mockserver.dto.ApiResponse;
import com.carolcoral.mockserver.dto.LoginRequest;
import com.carolcoral.mockserver.dto.LoginResponse;
import com.carolcoral.mockserver.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
