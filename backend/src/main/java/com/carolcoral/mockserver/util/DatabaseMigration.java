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

        try {
            // 添加custom_response_handler字段到t_mock_api表
            jdbcTemplate.execute("ALTER TABLE t_mock_api ADD COLUMN custom_response_handler VARCHAR(500)");
            log.info("成功添加custom_response_handler字段到t_mock_api表");
        } catch (Exception e) {
            String errorMsg = e.getMessage();
            if (errorMsg != null && errorMsg.contains("duplicate column name")) {
                log.info("custom_response_handler字段已存在，跳过迁移");
            } else {
                log.warn("添加custom_response_handler字段失败: {}", errorMsg);
            }
        }

        try {
            // 添加custom_response_source字段到t_mock_api表（TEXT类型，存储动态编译的源码）
            jdbcTemplate.execute("ALTER TABLE t_mock_api ADD COLUMN custom_response_source TEXT");
            log.info("成功添加custom_response_source字段到t_mock_api表");
        } catch (Exception e) {
            String errorMsg = e.getMessage();
            if (errorMsg != null && errorMsg.contains("duplicate column name")) {
                log.info("custom_response_source字段已存在，跳过迁移");
            } else {
                log.warn("添加custom_response_source字段失败: {}", errorMsg);
            }
        }

        try {
            // 添加is_system字段到t_custom_code_template表（v2.1.2 系统默认模板标识）
            jdbcTemplate.execute("ALTER TABLE t_custom_code_template ADD COLUMN is_system BOOLEAN DEFAULT 0");
            log.info("成功添加is_system字段到t_custom_code_template表");
        } catch (Exception e) {
            String errorMsg = e.getMessage();
            if (errorMsg != null && (errorMsg.contains("duplicate column name") || errorMsg.contains("no such table"))) {
                log.info("is_system字段已存在或表不存在，跳过迁移");
            } else {
                log.warn("添加is_system字段失败: {}", errorMsg);
            }
        }

        try {
            // v2.1.2: 将 project_id 改为可空（系统模板不属于任何项目）
            // SQLite 不支持 ALTER COLUMN，需要重建表
            // 注意：v2.1.0 升级时此表由 Hibernate ddl-auto 自动创建，无需迁移
            jdbcTemplate.execute("""
                CREATE TABLE t_custom_code_template_new (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name VARCHAR(100) NOT NULL,
                    description VARCHAR(500),
                    source_code TEXT NOT NULL,
                    language VARCHAR(50) NOT NULL DEFAULT 'JAVA',
                    enabled BOOLEAN NOT NULL DEFAULT 1,
                    is_system BOOLEAN DEFAULT 0,
                    create_time DATETIME NOT NULL,
                    update_time DATETIME NOT NULL,
                    create_user_id BIGINT NOT NULL,
                    project_id BIGINT
                )
                """);
            // 迁移旧数据（如果旧表存在且有数据）
            jdbcTemplate.execute("""
                INSERT INTO t_custom_code_template_new
                SELECT id, name, description, source_code, language, enabled,
                       COALESCE(is_system, 0), create_time, update_time, create_user_id, project_id
                FROM t_custom_code_template
                """);
            jdbcTemplate.execute("DROP TABLE t_custom_code_template");
            jdbcTemplate.execute("ALTER TABLE t_custom_code_template_new RENAME TO t_custom_code_template");
            log.info("成功将project_id改为可空（v2.1.2 代码模板表结构升级）");
        } catch (Exception e) {
            String errorMsg = e.getMessage();
            if (errorMsg != null && (errorMsg.contains("already exists") || errorMsg.contains("no such table"))) {
                log.info("代码模板表无需升级，跳过迁移");
            } else {
                log.warn("代码模板表结构升级失败: {}", errorMsg);
            }
        }
    }
}
