package com.carolcoral.mockserver.service;

import com.carolcoral.mockserver.dto.ApiResponse;
import com.carolcoral.mockserver.dto.LoginRequest;
import com.carolcoral.mockserver.dto.LoginResponse;
import com.carolcoral.mockserver.entity.User;
import com.carolcoral.mockserver.repository.UserRepository;
import com.carolcoral.mockserver.util.JwtTokenUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 用户服务类
 *
 * @author carolcoral
 */
@Tag(name = "用户服务", description = "用户业务逻辑处理")
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;

    /**
     * 用户登录
     *
     * @param loginRequest 登录请求
     * @return 登录响应
     */
    @Operation(summary = "用户登录")
    public ApiResponse<LoginResponse> login(@Parameter(description = "登录请求") LoginRequest loginRequest) {
        try {
            Optional<User> userOpt = userRepository.findByUsername(loginRequest.getUsername());
            
            if (!userOpt.isPresent()) {
                return ApiResponse.error(401, "用户名或密码错误");
            }

            User user = userOpt.get();
            
            if (!user.getEnabled()) {
                return ApiResponse.error(403, "用户已被禁用");
            }

            if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                return ApiResponse.error(401, "用户名或密码错误");
            }

            // 生成JWT令牌
            String token = jwtTokenUtil.generateToken(user, user.getId(), user.getRole().name());

            LoginResponse loginResponse = LoginResponse.builder()
                    .token(token)
                    .tokenType("Bearer")
                    .userId(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .role(user.getRole().name())
                    .expiresIn(jwtTokenUtil.getExpiration())
                    .build();

            log.info("用户登录成功: {}", user.getUsername());
            return ApiResponse.success(loginResponse);

        } catch (Exception e) {
            log.error("用户登录失败: {}", e.getMessage(), e);
            return ApiResponse.error("登录失败，请稍后重试");
        }
    }

    /**
     * 创建用户
     *
     * @param user 用户信息
     * @return 创建的用户
     */
    @Operation(summary = "创建用户")
    @Transactional
    public ApiResponse<User> createUser(@Parameter(description = "用户信息") User user) {
        try {
            // 检查用户名是否已存在
            if (userRepository.existsByUsername(user.getUsername())) {
                return ApiResponse.error("用户名已存在");
            }

            // 检查邮箱是否已存在
            if (user.getEmail() != null && userRepository.existsByEmail(user.getEmail())) {
                return ApiResponse.error("邮箱已存在");
            }

            // 加密密码
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            
            // 设置默认角色
            if (user.getRole() == null) {
                user.setRole(User.UserRole.USER);
            }

            User savedUser = userRepository.save(user);
            log.info("创建用户成功: {}", savedUser.getUsername());
            return ApiResponse.success(savedUser);

        } catch (Exception e) {
            log.error("创建用户失败: {}", e.getMessage(), e);
            return ApiResponse.error("创建用户失败，请稍后重试");
        }
    }

    /**
     * 更新用户
     *
     * @param user 用户信息
     * @return 更新的用户
     */
    @Operation(summary = "更新用户")
    @Transactional
    public ApiResponse<User> updateUser(@Parameter(description = "用户信息") User user) {
        try {
            Optional<User> existingUserOpt = userRepository.findById(user.getId());
            
            if (!existingUserOpt.isPresent()) {
                return ApiResponse.error("用户不存在");
            }

            User existingUser = existingUserOpt.get();
            
            // 更新用户信息
            if (user.getEmail() != null) {
                existingUser.setEmail(user.getEmail());
            }
            if (user.getRole() != null) {
                existingUser.setRole(user.getRole());
            }
            if (user.getEnabled() != null) {
                existingUser.setEnabled(user.getEnabled());
            }

            // 如果更新了密码，需要加密
            if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
            }

            User updatedUser = userRepository.save(existingUser);
            log.info("更新用户成功: {}", updatedUser.getUsername());
            return ApiResponse.success(updatedUser);

        } catch (Exception e) {
            log.error("更新用户失败: {}", e.getMessage(), e);
            return ApiResponse.error("更新用户失败，请稍后重试");
        }
    }

    /**
     * 删除用户
     *
     * @param userId 用户ID
     * @return 删除结果
     */
    @Operation(summary = "删除用户")
    @Transactional
    public ApiResponse<Void> deleteUser(@Parameter(description = "用户ID", example = "1") Long userId) {
        try {
            if (!userRepository.existsById(userId)) {
                return ApiResponse.error("用户不存在");
            }

            // 不能删除管理员账号
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isPresent() && userOpt.get().getRole() == User.UserRole.ADMIN) {
                return ApiResponse.error("不能删除管理员账号");
            }

            userRepository.deleteById(userId);
            log.info("删除用户成功: {}", userId);
            return ApiResponse.success();

        } catch (Exception e) {
            log.error("删除用户失败: {}", e.getMessage(), e);
            return ApiResponse.error("删除用户失败，请稍后重试");
        }
    }

    /**
     * 根据ID查询用户
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    @Operation(summary = "根据ID查询用户")
    public ApiResponse<User> getUserById(@Parameter(description = "用户ID", example = "1") Long userId) {
        try {
            Optional<User> userOpt = userRepository.findById(userId);
            
            if (!userOpt.isPresent()) {
                return ApiResponse.error("用户不存在");
            }

            return ApiResponse.success(userOpt.get());

        } catch (Exception e) {
            log.error("查询用户失败: {}", e.getMessage(), e);
            return ApiResponse.error("查询用户失败，请稍后重试");
        }
    }

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户信息
     */
    @Operation(summary = "根据用户名查询用户")
    public ApiResponse<User> getUserByUsername(@Parameter(description = "用户名", example = "admin") String username) {
        try {
            Optional<User> userOpt = userRepository.findByUsername(username);
            
            if (!userOpt.isPresent()) {
                return ApiResponse.error("用户不存在");
            }

            return ApiResponse.success(userOpt.get());

        } catch (Exception e) {
            log.error("查询用户失败: {}", e.getMessage(), e);
            return ApiResponse.error("查询用户失败，请稍后重试");
        }
    }

    /**
     * 查询所有用户
     *
     * @return 用户列表
     */
    @Operation(summary = "查询所有用户")
    public ApiResponse<List<User>> getAllUsers() {
        try {
            List<User> users = userRepository.findAll();
            return ApiResponse.success(users);

        } catch (Exception e) {
            log.error("查询用户列表失败: {}", e.getMessage(), e);
            return ApiResponse.error("查询用户列表失败，请稍后重试");
        }
    }

    /**
     * 根据角色查询用户
     *
     * @param role 用户角色
     * @return 用户列表
     */
    @Operation(summary = "根据角色查询用户")
    public ApiResponse<List<User>> getUsersByRole(@Parameter(description = "用户角色") User.UserRole role) {
        try {
            List<User> users = userRepository.findByRole(role);
            return ApiResponse.success(users);

        } catch (Exception e) {
            log.error("查询用户列表失败: {}", e.getMessage(), e);
            return ApiResponse.error("查询用户列表失败，请稍后重试");
        }
    }

    /**
     * 查询启用状态的用户
     *
     * @return 用户列表
     */
    @Operation(summary = "查询启用状态的用户")
    public ApiResponse<List<User>> getEnabledUsers() {
        try {
            List<User> users = userRepository.findByEnabledTrue();
            return ApiResponse.success(users);

        } catch (Exception e) {
            log.error("查询用户列表失败: {}", e.getMessage(), e);
            return ApiResponse.error("查询用户列表失败，请稍后重试");
        }
    }
}
