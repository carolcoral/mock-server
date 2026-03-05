/**
* Copyright (c) 2026, XINDU.SITE
* All Rights Reserved.
* XINDU.SITE CONFIDENTIAL
*/

package com.carolcoral.mockserver.config;

import com.carolcoral.mockserver.entity.MockApi;
import com.carolcoral.mockserver.entity.MockResponse;
import com.carolcoral.mockserver.entity.Project;
import com.carolcoral.mockserver.entity.User;
import com.carolcoral.mockserver.repository.MockApiRepository;
import com.carolcoral.mockserver.repository.MockResponseRepository;
import com.carolcoral.mockserver.repository.ProjectRepository;
import com.carolcoral.mockserver.repository.UserRepository;
import com.carolcoral.mockserver.util.CacheUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 启动配置类 - 初始化数据
 *
 * @author carolcoral
 */
@Tag(name = "启动配置", description = "应用启动初始化配置")
@Slf4j
@Component
@RequiredArgsConstructor
public class StartupConfig implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final MockApiRepository mockApiRepository;
    private final MockResponseRepository mockResponseRepository;
    private final PasswordEncoder passwordEncoder;
    private final CacheUtil cacheUtil;

    // 从系统属性（由.env文件加载）获取管理员配置
    private String adminUsername = System.getProperty("ADMIN_USERNAME", "admin");
    private String adminPassword = System.getProperty("ADMIN_PASSWORD", "Admin@123!");
    private String adminEmail = System.getProperty("ADMIN_EMAIL", "admin@mockserver.com");

    @Override
    @Operation(summary = "初始化数据", description = "应用启动时初始化管理员账号和示例数据")
    public void run(String... args) throws Exception {
        log.info("开始初始化应用数据...");

        try {
            // 检查密码是否配置
            if (adminPassword == null || adminPassword.isEmpty()) {
                log.warn("管理员密码未配置，跳过创建管理员账号。请在环境变量中设置ADMIN_PASSWORD");
                return;
            }

            // 验证密码强度
            if (!isStrongPassword(adminPassword)) {
                log.warn("管理员密码强度不足，跳过创建管理员账号。密码必须包含大小写字母、数字和特殊字符，且长度至少8位");
                return;
            }

            // 初始化管理员账号
            initAdminUser();

            // 初始化示例项目
            initExampleProject();

            log.info("应用数据初始化完成");
        } catch (Exception e) {
            log.error("应用数据初始化失败");
        }
    }

    /**
     * 检查密码强度
     */
    private boolean isStrongPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasLower = password.matches(".*[a-z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSpecial = password.matches(".*[@$!%*?&].*");
        return hasUpper && hasLower && hasDigit && hasSpecial;
    }

    /**
     * 初始化管理员账号
     */
    @Operation(summary = "初始化管理员账号")
    private void initAdminUser() {
        try {
            // 检查管理员账号是否已存在（使用findByUsername代替existsByUsername避免SQLite兼容性问题）
            Optional<User> existingAdmin = userRepository.findByUsername(adminUsername);
            if (existingAdmin.isPresent()) {
                log.info("管理员账号已存在: {}", adminUsername);
                return;
            }

            // 创建管理员账号
            User admin = new User();
            admin.setUsername(adminUsername);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setEmail(adminEmail);
            admin.setRole(User.UserRole.ADMIN);
            admin.setEnabled(true);
            admin.setCreateTime(LocalDateTime.now());
            admin.setUpdateTime(LocalDateTime.now());

            userRepository.save(admin);
            log.info("初始化管理员账号成功: {}", adminUsername);

        } catch (Exception e) {
            log.error("初始化管理员账号失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 初始化示例项目
     */
    @Operation(summary = "初始化示例项目")
    private void initExampleProject() {
        try {
            // 检查示例项目是否已存在
            String projectCode = "demo";
            if (projectRepository.existsByCode(projectCode)) {
                log.info("示例项目已存在: {}", projectCode);
                return;
            }

            // 获取管理员用户
            User admin = userRepository.findByUsername(adminUsername).orElse(null);
            if (admin == null) {
                log.error("管理员用户不存在，无法创建示例项目");
                return;
            }

            // 创建示例项目
            Project project = new Project();
            project.setName("示例项目");
            project.setCode(projectCode);
            project.setDescription("这是一个示例项目，包含一些常用的Mock接口");
            project.setEnabled(true);
            project.setCreateUserId(admin.getId());
            project.setCreateTime(LocalDateTime.now());
            project.setUpdateTime(LocalDateTime.now());

            Project savedProject = projectRepository.save(project);
            log.info("创建示例项目成功: {}", projectCode);

            // 创建示例接口
            initExampleApis(savedProject, admin.getId());

        } catch (Exception e) {
            log.error("初始化示例项目失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 初始化示例接口
     *
     * @param project  项目
     * @param userId   用户ID
     */
    @Operation(summary = "初始化示例接口")
    private void initExampleApis(Project project, Long userId) {
        try {
            // 创建用户登录接口
            MockApi loginApi = createMockApi(project, "用户登录接口", "/user/login", MockApi.HttpMethod.POST, userId);
            createMockResponse(loginApi, 200, "{\"code\": 200, \"message\": \"登录成功\", \"data\": {\"token\": \"mock-token-123\", \"userId\": 1, \"username\": \"admin\"}}", userId, true);
            createMockResponse(loginApi, 401, "{\"code\": 401, \"message\": \"用户名或密码错误\"}", userId, false);

            // 创建获取用户信息接口
            MockApi userInfoApi = createMockApi(project, "获取用户信息", "/user/info", MockApi.HttpMethod.GET, userId);
            createMockResponse(userInfoApi, 200, "{\"code\": 200, \"message\": \"成功\", \"data\": {\"userId\": 1, \"username\": \"admin\", \"email\": \"admin@example.com\", \"role\": \"ADMIN\"}}", userId, true);

            // 创建商品列表接口
            MockApi productListApi = createMockApi(project, "商品列表", "/products", MockApi.HttpMethod.GET, userId);
            createMockResponse(productListApi, 200, "{\"code\": 200, \"message\": \"成功\", \"data\": [{\"id\": 1, \"name\": \"iPhone 15\", \"price\": 5999}, {\"id\": 2, \"name\": \"MacBook Pro\", \"price\": 12999}]}", userId, true);

            // 创建订单创建接口（带条件响应）
            MockApi orderApi = createMockApi(project, "创建订单", "/order/create", MockApi.HttpMethod.POST, userId);
            MockResponse successResponse = createMockResponse(orderApi, 200, "{\"code\": 200, \"message\": \"订单创建成功\", \"data\": {\"orderId\": \"2024001\", \"amount\": 99.99}}", userId, true);
            successResponse.setCondition("$.productId == '1'");
            successResponse.setConditionDesc("当商品ID为1时返回成功");
            mockResponseRepository.save(successResponse);

            MockResponse errorResponse = createMockResponse(orderApi, 400, "{\"code\": 400, \"message\": \"商品库存不足\"}", userId, false);
            errorResponse.setCondition("$.productId == '999'");
            errorResponse.setConditionDesc("当商品ID为999时返回库存不足");
            mockResponseRepository.save(errorResponse);

            // 创建随机响应接口
            MockApi randomApi = createMockApi(project, "随机响应接口", "/random", MockApi.HttpMethod.GET, userId);
            randomApi.setEnableRandom(true);
            mockApiRepository.save(randomApi);

            // 随机响应接口需要激活两个响应
            createMockResponse(randomApi, 200, "{\"code\": 200, \"message\": \"成功（高概率）\", \"data\": \"这是200响应\"}", 80, userId, true);
            createMockResponse(randomApi, 500, "{\"code\": 500, \"message\": \"服务器错误（低概率）\", \"data\": \"这是500响应\"}", 20, userId, true);

            // 初始化缓存
            cacheUtil.initCache();

            log.info("创建示例接口成功，共 {} 个接口", 5);

        } catch (Exception e) {
            log.error("初始化示例接口失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 创建Mock接口
     *
     * @param project     项目
     * @param name        接口名称
     * @param path        接口路径
     * @param method      请求方法
     * @param userId      用户ID
     * @return Mock接口
     */
    private MockApi createMockApi(Project project, String name, String path, MockApi.HttpMethod method, Long userId) {
        MockApi api = new MockApi();
        api.setName(name);
        api.setPath(path);
        api.setMethod(method);
        api.setRequestType(MockApi.RequestType.HTTP);
        api.setProject(project);
        api.setEnabled(true);
        api.setEnableRandom(false);
        api.setCreateUserId(userId);
        api.setCreateTime(LocalDateTime.now());
        api.setUpdateTime(LocalDateTime.now());

        return mockApiRepository.save(api);
    }

    /**
     * 创建Mock响应
     *
     * @param mockApi       接口
     * @param statusCode    状态码
     * @param responseBody  响应体
     * @param userId        用户ID
     * @param active        是否激活
     * @return Mock响应
     */
    private MockResponse createMockResponse(MockApi mockApi, int statusCode, String responseBody, Long userId, boolean active) {
        return createMockResponse(mockApi, statusCode, responseBody, 100, userId, active);
    }

    /**
     * 创建Mock响应（带权重）
     *
     * @param mockApi       接口
     * @param statusCode    状态码
     * @param responseBody  响应体
     * @param weight        权重
     * @param userId        用户ID
     * @param active        是否激活
     * @return Mock响应
     */
    private MockResponse createMockResponse(MockApi mockApi, int statusCode, String responseBody, int weight, Long userId, boolean active) {
        MockResponse response = new MockResponse();
        response.setMockApi(mockApi);
        response.setStatusCode(statusCode);
        response.setContentType("application/json");
        response.setResponseBody(responseBody);
        response.setWeight(weight);
        response.setEnabled(true);
        response.setActive(active);
        response.setCreateTime(LocalDateTime.now());
        response.setUpdateTime(LocalDateTime.now());

        return mockResponseRepository.save(response);
    }
}
