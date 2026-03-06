/**
* Copyright (c) 2026, XINDU.SITE
* All Rights Reserved.
* XINDU.SITE CONFIDENTIAL
*/

package com.carolcoral.mockserver.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class DatabaseMigration implements CommandLineRunner {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DatabaseMigration.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        try {
            // 添加active字段到t_mock_response表
            jdbcTemplate.execute("ALTER TABLE t_mock_response ADD COLUMN active BOOLEAN DEFAULT 0");
            log.info("成功添加active字段到t_mock_response表");
        } catch (Exception e) {
            String errorMsg = e.getMessage();
            if (errorMsg != null && errorMsg.contains("duplicate column name")) {
                log.info("active字段已存在，跳过迁移");
            } else {
                log.warn("添加active字段失败: {}", errorMsg);
            }
        }

        try {
            // 添加is_default字段到t_mock_response表
            jdbcTemplate.execute("ALTER TABLE t_mock_response ADD COLUMN is_default BOOLEAN DEFAULT 0");
            log.info("成功添加is_default字段到t_mock_response表");
        } catch (Exception e) {
            String errorMsg = e.getMessage();
            if (errorMsg != null && errorMsg.contains("duplicate column name")) {
                log.info("is_default字段已存在，跳过迁移");
            } else {
                log.warn("添加is_default字段失败: {}", errorMsg);
            }
        }

        try {
            // 添加response_delay字段到t_mock_response表
            jdbcTemplate.execute("ALTER TABLE t_mock_response ADD COLUMN response_delay INTEGER DEFAULT 0");
            log.info("成功添加response_delay字段到t_mock_response表");
        } catch (Exception e) {
            String errorMsg = e.getMessage();
            if (errorMsg != null && errorMsg.contains("duplicate column name")) {
                log.info("response_delay字段已存在，跳过迁移");
            } else {
                log.warn("添加response_delay字段失败: {}", errorMsg);
            }
        }

        try {
            // 创建t_response_request_param表
            jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS t_response_request_param (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    param_name VARCHAR(100) NOT NULL,
                    param_type VARCHAR(50) NOT NULL,
                    param_value TEXT,
                    required BOOLEAN NOT NULL DEFAULT 1,
                    create_time DATETIME NOT NULL,
                    update_time DATETIME NOT NULL,
                    response_id INTEGER NOT NULL,
                    FOREIGN KEY (response_id) REFERENCES t_mock_response(id)
                )
                """);
            log.info("成功创建t_response_request_param表");
        } catch (Exception e) {
            log.warn("创建t_response_request_param表失败: {}", e.getMessage());
        }

        try {
            // 添加language字段到t_user表（允许NULL，然后更新现有记录）
            jdbcTemplate.execute("ALTER TABLE t_user ADD COLUMN language varchar(10)");
            log.info("成功添加language字段到t_user表");
            // 为现有用户设置默认语言
            jdbcTemplate.update("UPDATE t_user SET language = 'zh-CN' WHERE language IS NULL");
            log.info("为现有用户设置默认语言");
        } catch (Exception e) {
            String errorMsg = e.getMessage();
            if (errorMsg != null && errorMsg.contains("duplicate column name")) {
                log.info("language字段已存在，跳过迁移");
            } else {
                log.warn("添加language字段失败: {}", errorMsg);
            }
        }

        try {
            // 创建t_system_config表
            jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS t_system_config (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    config_key VARCHAR(100) NOT NULL UNIQUE,
                    config_value VARCHAR(500) NOT NULL,
                    description VARCHAR(500),
                    create_time DATETIME NOT NULL,
                    update_time DATETIME NOT NULL
                )
                """);
            log.info("成功创建t_system_config表");
            // 插入默认配置（如果不存在）
            jdbcTemplate.execute("""
                INSERT OR IGNORE INTO t_system_config (config_key, config_value, description, create_time, update_time)
                VALUES ('defaultLanguage', 'zh-CN', '系统默认语言', datetime('now'), datetime('now'))
                """);
            log.info("插入默认系统配置");
        } catch (Exception e) {
            log.warn("创建t_system_config表失败: {}", e.getMessage());
        }
    }
}
