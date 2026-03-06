/**
* Copyright (c) 2026, XINDU.SITE
* All Rights Reserved.
* XINDU.SITE CONFIDENTIAL
*/

package com.carolcoral.mockserver.controller;

import com.carolcoral.mockserver.dto.ApiResponse;
import com.carolcoral.mockserver.entity.User;
import com.carolcoral.mockserver.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 *
 * @author carolcoral
 */
@Tag(name = "用户管理", description = "用户管理相关接口")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/users")
@Validated
public class UserController {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(UserController.class);

    /**
     * 构造器
     */
    public UserController(UserService userService) {
        this.userService = userService;
    }

    private final UserService userService;

    /**
     * 创建用户
     *
     * @param user 用户信息
     * @return 创建的用户
     */
    @Operation(summary = "创建用户", description = "创建新用户，需要管理员权限")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ApiResponse<User> createUser(@Parameter(description = "用户信息") @Valid @RequestBody User user) {
        log.info("创建用户请求: {}", user.getUsername());
        return userService.createUser(user);
    }

    /**
     * 更新用户
     *
     * @param user 用户信息
     * @return 更新的用户
     */
    @Operation(summary = "更新用户", description = "更新用户信息，需要管理员权限或本人操作")
    @PreAuthorize("hasRole('ADMIN') or #user.id == authentication.principal.id")
    @PutMapping("/update-profile")
    public ApiResponse<User> updateUserProfile(@RequestBody User user) {
        log.info("更新用户信息请求: {}", user.getId());
        return userService.updateUserProfile(user);
    }

    /**
     * 获取当前用户信息
     *
     * @return 用户信息
     */
    @GetMapping("/profile")
    @Operation(summary = "获取当前用户信息", description = "需要登录")
    public ApiResponse<User> getCurrentUserProfile() {
        return userService.getCurrentUser();
    }

    /**
     * 修改密码
     *
     * @param passwordChangeRequest 密码修改请求
     * @return 操作结果
     */
    @PostMapping("/change-password")
    @Operation(summary = "修改密码", description = "需要登录，原密码必须正确")
    public ApiResponse<Void> changePassword(@RequestBody UserService.PasswordChangeRequest passwordChangeRequest) {
        return userService.changePassword(passwordChangeRequest);
    }

    /**
     * 删除用户
     *
     * @param userId 用户ID
     * @return 删除结果
     */
    @Operation(summary = "删除用户", description = "删除用户，需要管理员权限")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{userId}")
    public ApiResponse<Void> deleteUser(@Parameter(description = "用户ID", example = "1") @PathVariable Long userId) {
        log.info("删除用户请求: {}", userId);
        return userService.deleteUser(userId);
    }

    /**
     * 根据ID查询用户
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    @Operation(summary = "根据ID查询用户", description = "根据用户ID查询用户信息")
    @GetMapping("/{userId}")
    public ApiResponse<User> getUserById(@Parameter(description = "用户ID", example = "1") @PathVariable Long userId) {
        log.info("查询用户请求: {}", userId);
        return userService.getUserById(userId);
    }

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户信息
     */
    @Operation(summary = "根据用户名查询用户", description = "根据用户名查询用户信息")
    @GetMapping("/username/{username}")
    public ApiResponse<User> getUserByUsername(
            @Parameter(description = "用户名", example = "admin")
            @PathVariable @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线") String username) {
        log.info("查询用户请求: {}", username);
        return userService.getUserByUsername(username);
    }

    /**
     * 查询所有用户
     *
     * @return 用户列表
     */
    @Operation(summary = "查询所有用户", description = "查询所有用户列表，需要管理员权限")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ApiResponse<java.util.List<User>> getAllUsers() {
        log.info("查询所有用户请求");
        return userService.getAllUsers();
    }

    /**
     * 根据角色查询用户
     *
     * @param role 用户角色
     * @return 用户列表
     */
    @Operation(summary = "根据角色查询用户", description = "根据用户角色查询用户列表")
    @GetMapping("/role/{role}")
    public ApiResponse<java.util.List<User>> getUsersByRole(@Parameter(description = "用户角色", example = "USER") @PathVariable User.UserRole role) {
        log.info("查询用户请求，角色: {}", role);
        return userService.getUsersByRole(role);
    }

    /**
     * 查询启用状态的用户
     *
     * @return 用户列表
     */
    @Operation(summary = "查询启用状态的用户", description = "查询所有启用状态的用户")
    @GetMapping("/enabled")
    public ApiResponse<java.util.List<User>> getEnabledUsers() {
        log.info("查询启用状态用户请求");
        return userService.getEnabledUsers();
    }
}
