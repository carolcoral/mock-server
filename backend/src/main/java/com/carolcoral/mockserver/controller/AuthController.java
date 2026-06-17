/**
* Copyright (c) 2026, XINDU.SITE
* All Rights Reserved.
* XINDU.SITE CONFIDENTIAL
*/

package com.carolcoral.mockserver.controller;

import com.carolcoral.mockserver.dto.ApiResponse;
import com.carolcoral.mockserver.dto.LoginRequest;
import com.carolcoral.mockserver.dto.LoginResponse;
import com.carolcoral.mockserver.dto.RegisterRequest;
import com.carolcoral.mockserver.repository.EmailConfigRepository;
import com.carolcoral.mockserver.service.EmailService;
import com.carolcoral.mockserver.service.SystemConfigService;
import com.carolcoral.mockserver.service.UserService;
import com.carolcoral.mockserver.util.JwtTokenUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 认证控制器
 *
 * @author carolcoral
 */
@Tag(name = "认证管理", description = "用户认证相关接口")
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AuthController.class);

    /**
     * 构造器
     */
    public AuthController(UserService userService,
        JwtTokenUtil jwtTokenUtil,
        SystemConfigService systemConfigService,
        EmailService emailService,
        EmailConfigRepository emailConfigRepository) {
        this.userService = userService;
        this.jwtTokenUtil = jwtTokenUtil;
        this.systemConfigService = systemConfigService;
        this.emailService = emailService;
        this.emailConfigRepository = emailConfigRepository;
    }

    private final UserService userService;
    private final JwtTokenUtil jwtTokenUtil;
    private final SystemConfigService systemConfigService;
    private final EmailService emailService;
    private final EmailConfigRepository emailConfigRepository;

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
     * 用户登出
     *
     * @param authorizationHeader Authorization头
     * @return 登出结果
     */
    @Operation(summary = "用户登出", description = "用户登出接口，使当前token失效")
    @PostMapping("/logout")
    public ApiResponse<Boolean> logout(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        try {
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                String token = authorizationHeader.substring(7);
                log.info("用户登出: token={}", token.substring(0, Math.min(20, token.length())));
                // JWT是无状态的，这里可以记录token到黑名单（可选）
                // 对于简单的实现，只需要前端删除token即可
            }
            
            return ApiResponse.success(true);
            
        } catch (Exception e) {
            log.error("登出失败: {}", e.getMessage());
            return ApiResponse.error("登出失败：" + e.getMessage());
        }
    }

    /**
     * 验证Swagger访问权限（已登录用户）
     *
     * @param token JWT令牌
     * @return 验证结果
     */
    /**
     * 用户注册
     *
     * @param registerRequest 注册请求
     * @return 注册结果
     */
    @Operation(summary = "用户注册", description = "用户注册接口，需管理员在系统设置中开启注册功能。开启邮箱验证后需提供验证码。")
    @PostMapping("/register")
    public ApiResponse<?> register(@Parameter(description = "注册请求") @Valid @RequestBody RegisterRequest registerRequest) {
        log.info("用户注册请求: username={}, email={}", registerRequest.getUsername(), registerRequest.getEmail());

        // 检查注册功能是否开启
        String enableReg = systemConfigService.getConfig("enableRegistration");
        if (!"true".equals(enableReg)) {
            return ApiResponse.error("注册功能未开启");
        }

        // 检查是否开启了邮箱验证
        String enableEmailVerify = systemConfigService.getConfig("enableEmailVerification");
        boolean emailVerificationEnabled = "true".equals(enableEmailVerify);

        if (emailVerificationEnabled) {
            // 验证验证码
            if (registerRequest.getVerificationCode() == null || registerRequest.getVerificationCode().isEmpty()) {
                return ApiResponse.error("请输入邮箱验证码");
            }
            boolean codeValid = emailService.verifyRegisterCode(
                    registerRequest.getEmail(), registerRequest.getVerificationCode());
            if (!codeValid) {
                return ApiResponse.error("验证码错误或已过期");
            }
        }

        // 检查邮箱域名是否在允许列表中
        String allowedDomains = systemConfigService.getConfig("allowedEmailDomains");
        if (allowedDomains != null && !allowedDomains.isEmpty()) {
            String emailDomain = registerRequest.getEmail().substring(registerRequest.getEmail().indexOf('@') + 1).toLowerCase();
            List<String> domainList = Arrays.asList(allowedDomains.toLowerCase().split("\\s*,\\s*"));
            boolean matched = domainList.stream().anyMatch(d -> d.equals(emailDomain) || emailDomain.endsWith("." + d));
            if (!matched) {
                return ApiResponse.error("不支持该邮箱域名注册，允许的域名: " + allowedDomains);
            }
        }

        // 检查邮箱是否已被使用
        if (userService.isEmailTaken(registerRequest.getEmail())) {
            return ApiResponse.error("该邮箱已被注册");
        }

        // 创建用户
        com.carolcoral.mockserver.entity.User user = new com.carolcoral.mockserver.entity.User();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(registerRequest.getPassword());
        user.setEmail(registerRequest.getEmail());

        return userService.createUser(user);
    }

    /**
     * 发送邮箱验证码
     *
     * @param requestBody 包含邮箱的请求体
     * @return 发送结果
     */
    @Operation(summary = "发送邮箱验证码", description = "向指定邮箱发送注册验证码，需管理员开启邮箱验证功能")
    @PostMapping("/send-verification-code")
    public ApiResponse<?> sendVerificationCode(@RequestBody Map<String, Object> requestBody) {
        String email = (String) requestBody.get("email");
        if (email == null || email.isEmpty()) {
            return ApiResponse.error("邮箱不能为空");
        }

        // 检查邮箱验证功能是否开启
        String enableEmailVerify = systemConfigService.getConfig("enableEmailVerification");
        if (!"true".equals(enableEmailVerify)) {
            return ApiResponse.error("邮箱验证功能未开启");
        }

        // 检查邮箱是否已被使用
        if (userService.isEmailTaken(email)) {
            return ApiResponse.error("该邮箱已被注册");
        }

        // 获取模板ID（可选），请求未传则使用邮箱配置中的默认模板
        Long templateId = null;
        Object tidObj = requestBody.get("templateId");
        if (tidObj instanceof Number) {
            templateId = ((Number) tidObj).longValue();
        } else if (tidObj instanceof String && !((String) tidObj).isEmpty()) {
            try { templateId = Long.parseLong((String) tidObj); } catch (NumberFormatException e) {}
        }
        if (templateId == null) {
            templateId = emailConfigRepository.findFirstByEnabledTrue()
                    .map(config -> config.getVerificationTemplateId())
                    .orElse(null);
        }

        log.info("请求发送验证码到: {}, templateId: {}", email, templateId);
        String code = emailService.sendRegisterVerificationCode(email, templateId);
        if (code != null) {
            return ApiResponse.success("验证码已发送");
        } else {
            return ApiResponse.error("验证码发送失败，请检查邮箱配置");
        }
    }

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
