/**
* Copyright (c) 2026, XINDU.SITE
* All Rights Reserved.
* XINDU.SITE CONFIDENTIAL
*/

package com.carolcoral.mockserver.config;

import com.carolcoral.mockserver.entity.MockApi;
import com.carolcoral.mockserver.entity.MockResponse;
import com.carolcoral.mockserver.entity.Project;
import com.carolcoral.mockserver.entity.SystemConfig;
import com.carolcoral.mockserver.entity.User;
import com.carolcoral.mockserver.plugin.DynamicCompiler;
import com.carolcoral.mockserver.repository.MockApiRepository;
import com.carolcoral.mockserver.repository.MockResponseRepository;
import com.carolcoral.mockserver.repository.ProjectRepository;
import com.carolcoral.mockserver.repository.SystemConfigRepository;
import com.carolcoral.mockserver.repository.UserRepository;
import com.carolcoral.mockserver.util.CacheUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 启动配置类 - 初始化数据
 *
 * @author carolcoral
 */
@Tag(name = "启动配置", description = "应用启动初始化配置")
@Component
public class StartupConfig implements CommandLineRunner {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(StartupConfig.class);

    /**
     * 构造器
     */
    public StartupConfig(UserRepository userRepository,
        ProjectRepository projectRepository,
        MockApiRepository mockApiRepository,
        MockResponseRepository mockResponseRepository,
        PasswordEncoder passwordEncoder,
        CacheUtil cacheUtil,
        SystemConfigRepository systemConfigRepository) {
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.mockApiRepository = mockApiRepository;
        this.mockResponseRepository = mockResponseRepository;
        this.passwordEncoder = passwordEncoder;
        this.cacheUtil = cacheUtil;
        this.systemConfigRepository = systemConfigRepository;
    }

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final MockApiRepository mockApiRepository;
    private final MockResponseRepository mockResponseRepository;
    private final PasswordEncoder passwordEncoder;
    private final CacheUtil cacheUtil;
    private final SystemConfigRepository systemConfigRepository;

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

            // 预编译已存储的自定义响应处理器代码
            precompileCustomTransformers();

            // 初始化系统配置默认值
            initSystemConfigDefaults();

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
                // 管理员已存在，同步更新密码和邮箱（支持密码热更新）
                User admin = existingAdmin.get();
                String newEncodedPassword = passwordEncoder.encode(adminPassword);
                boolean needUpdate = false;

                if (!passwordEncoder.matches(adminPassword, admin.getPassword())) {
                    admin.setPassword(newEncodedPassword);
                    needUpdate = true;
                    log.info("检测到管理员密码已变更，正在同步更新...");
                }

                if (adminEmail != null && !adminEmail.isEmpty()
                        && (admin.getEmail() == null || !adminEmail.equals(admin.getEmail()))) {
                    admin.setEmail(adminEmail);
                    needUpdate = true;
                }

                if (needUpdate) {
                    admin.setUpdateTime(LocalDateTime.now());
                    userRepository.save(admin);
                    log.info("管理员账号已同步更新: {}", adminUsername);
                } else {
                    log.info("管理员账号已存在且配置一致: {}", adminUsername);
                }
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
            // 检查test项目是否已存在
            String projectCode = "test";
            if (projectRepository.existsByCode(projectCode)) {
                log.info("test项目已存在: {}", projectCode);
                return;
            }

            // 获取管理员用户
            User admin = userRepository.findByUsername(adminUsername).orElse(null);
            if (admin == null) {
                log.error("管理员用户不存在，无法创建test项目");
                return;
            }

            // 创建test项目
            Project project = new Project();
            project.setName("测试项目");
            project.setCode(projectCode);
            project.setDescription("这是一个测试项目，包含一些常用的Mock接口");
            project.setEnabled(true);
            project.setCreateUserId(admin.getId());
            project.setCreateTime(LocalDateTime.now());
            project.setUpdateTime(LocalDateTime.now());

            Project savedProject = projectRepository.save(project);
            log.info("创建test项目成功: {}", projectCode);

            // 创建示例接口
            initExampleApis(savedProject, admin.getId());

        } catch (Exception e) {
            log.error("初始化test项目失败: {}", e.getMessage(), e);
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
            // 创建登录接口
            MockApi loginApi = createMockApi(project, "登录接口", "/login", MockApi.HttpMethod.POST, userId);
            createMockResponse(loginApi, 200, "{\"code\": 200, \"message\": \"登录成功\", \"data\": {\"token\": \"test-token-123\", \"userId\": 1, \"username\": \"admin\"}}", userId, true);
            createMockResponse(loginApi, 401, "{\"code\": 401, \"message\": \"用户名或密码错误\"}", userId, false);

            // 创建用户登录接口
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

    /**
     * 预编译已存储的自定义响应处理器代码
     * <p>
     * 应用启动时，将数据库中存储的自定义响应处理器代码预编译到内存中，
     * 这样第一次请求时无需等待编译时间，提升响应速度。
     * </p>
     */
    @Operation(summary = "预编译自定义响应处理器")
    private void precompileCustomTransformers() {
        try {
            // 检查编译器是否可用
            if (!DynamicCompiler.isCompilerAvailable()) {
                log.warn("Java编译器不可用，跳过自定义响应处理器预编译");
                return;
            }

            // 查询所有包含自定义响应源码的接口
            List<MockApi> apisWithCustomSource = mockApiRepository.findAll().stream()
                    .filter(api -> api.getCustomResponseSource() != null
                            && !api.getCustomResponseSource().trim().isEmpty()
                            && api.getEnabled() != null
                            && api.getEnabled())
                    .toList();

            if (apisWithCustomSource.isEmpty()) {
                log.info("没有找到需要预编译的自定义响应处理器");
                return;
            }

            log.info("开始预编译 {} 个自定义响应处理器...", apisWithCustomSource.size());

            int successCount = 0;
            int failCount = 0;

            for (MockApi api : apisWithCustomSource) {
                try {
                    String source = api.getCustomResponseSource();
                    if (source != null && !source.trim().isEmpty()) {
                        // 编译并缓存
                        DynamicCompiler.compileAndInstantiate(api.getId(), source);
                        log.info("预编译成功: 接口={} (ID={})", api.getName(), api.getId());
                        successCount++;
                    }
                } catch (Exception e) {
                    log.error("预编译失败: 接口={} (ID={}), 错误: {}",
                            api.getName(), api.getId(), e.getMessage());
                    failCount++;
                }
            }

            log.info("自定义响应处理器预编译完成: 成功={}, 失败={}", successCount, failCount);

        } catch (Exception e) {
            log.error("预编译自定义响应处理器时发生错误: {}", e.getMessage(), e);
        }
    }

    /**
     * 初始化系统配置默认值
     * <p>
     * 当配置表中不存在对应记录时，使用默认值写入数据库，
     * 确保 SystemConfigController 能返回完整的配置信息。
     * </p>
     */
    @Operation(summary = "初始化系统配置默认值")
    private void initSystemConfigDefaults() {
        try {
            initDefaultConfigIfAbsent("defaultResponseDelay", "0", "默认响应延迟（毫秒）");
            initDefaultConfigIfAbsent("maxResponseDelay", "5000", "最大响应延迟（毫秒）");
            initDefaultConfigIfAbsent("enableRequestLog", "true", "是否启用请求日志");
            initDefaultConfigIfAbsent("logRetentionDays", "30", "日志保留天数");
            initDefaultConfigIfAbsent("maxRequestBodySize", "10", "最大请求体大小（MB）");
            initDefaultConfigIfAbsent("axiosTimeout", "30000", "前端Axios请求超时时间（毫秒）");
            log.info("系统配置默认值初始化完成");
        } catch (Exception e) {
            log.error("初始化系统配置默认值失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 如果配置不存在则使用默认值初始化
     */
    private void initDefaultConfigIfAbsent(String key, String defaultValue, String description) {
        try {
            if (systemConfigRepository.findByConfigKey(key).isEmpty()) {
                SystemConfig config = new SystemConfig();
                config.setConfigKey(key);
                config.setConfigValue(defaultValue);
                config.setDescription(description);
                config.setCreateTime(LocalDateTime.now());
                config.setUpdateTime(LocalDateTime.now());
                systemConfigRepository.save(config);
                log.debug("初始化系统配置默认值: {} = {}", key, defaultValue);
            }
        } catch (Exception e) {
            log.warn("初始化配置 {} 失败: {}", key, e.getMessage());
        }
    }
}
