package com.carolcoral.mockserver.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * SQLite环境后置处理器 - 在Spring Boot启动早期创建数据库和日志目录
 *
 * @author carolcoral
 */
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SqliteEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final String PROPERTY_SOURCE_NAME = "sqliteDirectoryConfig";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        try {
            log.info("========================================");
            log.info("SQLite数据库环境初始化");
            log.info("========================================");

            // 获取数据库URL
            String sqliteUrl = environment.getProperty("spring.datasource.url", "jdbc:sqlite:./data/mock-server.db");
            String logFilePath = environment.getProperty("logging.file.name", "./logs/mock-server.log");

            // 创建数据库目录
            String dbDir = extractDirectoryPath(sqliteUrl, "jdbc:sqlite:");
            if (dbDir != null) {
                createDirectory(dbDir);
                log.info("数据库目录: {}", new File(dbDir).getAbsolutePath());
            }

            // 创建日志目录
            String logDir = extractDirectoryPath(logFilePath, null);
            if (logDir != null) {
                createDirectory(logDir);
                log.info("日志目录: {}", new File(logDir).getAbsolutePath());
            }

            // 将绝对路径设置回环境（可选）
            Map<String, Object> properties = new HashMap<>();
            
            // 注册为最高优先级的属性源
            MapPropertySource propertySource = new MapPropertySource(PROPERTY_SOURCE_NAME, properties);
            environment.getPropertySources().addFirst(propertySource);
            
            log.info("========================================");
            log.info("环境初始化完成");
            log.info("========================================");

        } catch (Exception e) {
            log.error("初始化SQLite环境失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 提取目录路径
     *
     * @param path 文件路径或URL
     * @param prefix 需要移除的前缀（如 jdbc:sqlite:）
     * @return 目录路径
     */
    private String extractDirectoryPath(String path, String prefix) {
        if (path == null || path.isEmpty()) {
            return null;
        }

        String filePath = path;
        if (prefix != null && path.startsWith(prefix)) {
            filePath = path.substring(prefix.length());
        }

        // 转换为绝对路径
        File file = new File(filePath);
        if (!file.isAbsolute()) {
            String userDir = System.getProperty("user.dir");
            file = new File(userDir, filePath);
        }

        // 返回目录路径
        File parentDir = file.getParentFile();
        return parentDir != null ? parentDir.getAbsolutePath() : null;
    }

    /**
     * 创建目录（如果不存在）
     *
     * @param dirPath 目录路径
     */
    private void createDirectory(String dirPath) {
        if (dirPath == null || dirPath.isEmpty()) {
            return;
        }

        File directory = new File(dirPath);
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            if (created) {
                log.info("✓ 创建目录成功: {}", directory.getAbsolutePath());
            } else {
                log.error("✗ 创建目录失败: {}", directory.getAbsolutePath());
            }
        } else {
            log.debug("目录已存在: {}", directory.getAbsolutePath());
        }
    }
}
