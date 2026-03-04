package com.carolcoral.mockserver.config;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

/**
 * Dotenv配置类 - 加载.env文件到系统属性
 * 确保Spring Boot能够读取.env文件中的配置
 *
 * @author carolcoral
 */
@Slf4j
@Configuration
public class DotenvConfig {

    @PostConstruct
    public void loadDotenv() {
        try {
            Dotenv dotenv = Dotenv.configure()
                    .directory("./")
                    .ignoreIfMalformed()
                    .ignoreIfMissing()
                    .load();

            // 将.env文件中的变量加载到系统属性，供Spring @Value使用
            dotenv.entries().forEach(entry -> {
                String key = entry.getKey();
                String value = entry.getValue();
                
                // 如果系统属性未设置，则从.env文件加载
                if (System.getProperty(key) == null && System.getenv(key) == null) {
                    System.setProperty(key, value);
                    log.debug("Loaded from .env file: {}={}", key, "***".repeat(value.length() / 3));
                }
            });

            log.info(".env配置文件加载成功");
        } catch (Exception e) {
            log.warn("无法加载.env文件，将使用系统环境变量或默认值: {}", e.getMessage());
        }
    }
}
