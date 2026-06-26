/**
* Copyright (c) 2026, XINDU.SITE，Author: LXW
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

        try {
            // v2.1.2: 创建 AI 配置表
            jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS t_ai_config (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    provider VARCHAR(50) NOT NULL UNIQUE,
                    provider_name VARCHAR(100) NOT NULL,
                    api_url VARCHAR(500) NOT NULL,
                    api_key VARCHAR(500) NOT NULL,
                    default_model VARCHAR(100),
                    max_tokens INTEGER DEFAULT 4096,
                    temperature REAL DEFAULT 0.7,
                    enabled BOOLEAN NOT NULL DEFAULT 0,
                    create_time DATETIME NOT NULL,
                    update_time DATETIME NOT NULL
                )
                """);
            log.info("成功创建t_ai_config表");
        } catch (Exception e) {
            log.warn("创建t_ai_config表失败: {}", e.getMessage());
        }

        try {
            // v2.3.0: 创建角色表
            jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS t_role (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name VARCHAR(50) NOT NULL UNIQUE,
                    code VARCHAR(50) NOT NULL UNIQUE,
                    description VARCHAR(200),
                    is_default BOOLEAN NOT NULL DEFAULT 0,
                    create_time DATETIME NOT NULL,
                    update_time DATETIME NOT NULL
                )
                """);
            log.info("成功创建t_role表");
            // 插入默认角色
            jdbcTemplate.execute("""
                INSERT OR IGNORE INTO t_role (id, name, code, description, is_default, create_time, update_time)
                VALUES (1, '管理员', 'ROLE_ADMIN', '系统管理员，拥有所有权限', 0, datetime('now'), datetime('now'))
                """);
            jdbcTemplate.execute("""
                INSERT OR IGNORE INTO t_role (id, name, code, description, is_default, create_time, update_time)
                VALUES (2, '普通用户', 'ROLE_USER', '默认注册用户角色', 1, datetime('now'), datetime('now'))
                """);
        } catch (Exception e) {
            log.warn("创建t_role表失败: {}", e.getMessage());
        }

        try {
            // v2.3.0: 创建权限表
            jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS t_permission (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name VARCHAR(100) NOT NULL,
                    code VARCHAR(100) NOT NULL UNIQUE,
                    group_name VARCHAR(50) NOT NULL,
                    type VARCHAR(20) NOT NULL,
                    sort_order INTEGER NOT NULL DEFAULT 0,
                    create_time DATETIME NOT NULL,
                    update_time DATETIME NOT NULL
                )
                """);
            log.info("成功创建t_permission表");
            // 插入默认权限
            insertDefaultPermissions();
        } catch (Exception e) {
            log.warn("创建t_permission表失败: {}", e.getMessage());
        }

        try {
            // v2.3.0: 创建角色-权限关联表
            jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS t_role_permission (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    role_id BIGINT NOT NULL,
                    permission_id BIGINT NOT NULL
                )
                """);
            log.info("成功创建t_role_permission表");
            // 给管理员角色分配所有权限
            jdbcTemplate.execute("""
                INSERT OR IGNORE INTO t_role_permission (role_id, permission_id)
                SELECT 1, id FROM t_permission
                """);
        } catch (Exception e) {
            log.warn("创建t_role_permission表失败: {}", e.getMessage());
        }

        try {
            // v2.3.0: 给t_user表添加role_id字段
            jdbcTemplate.execute("ALTER TABLE t_user ADD COLUMN role_id BIGINT");
            log.info("成功添加role_id字段到t_user表");
        } catch (Exception e) {
            String errorMsg = e.getMessage();
            if (errorMsg != null && errorMsg.contains("duplicate column name")) {
                log.info("role_id字段已存在，跳过迁移");
            } else {
                log.warn("添加role_id字段失败: {}", errorMsg);
            }
        }

        // 无论列是否已存在，都尝试更新已有用户的 role_id（处理列已存在但数据未迁移的情况）
        try {
            jdbcTemplate.update("UPDATE t_user SET role_id = 1 WHERE role = 'ADMIN' AND role_id IS NULL");
            jdbcTemplate.update("UPDATE t_user SET role_id = 2 WHERE role = 'USER' AND role_id IS NULL");
            log.info("已更新现有用户的role_id");
        } catch (Exception e) {
            log.warn("更新现有用户role_id失败: {}", e.getMessage());
        }

        try {
            // v2.3.0: 创建 AI 调用日志表（用于统计页面展示）
            // 使用 CREATE TABLE IF NOT EXISTS，保留历史数据
            jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS t_ai_call_log (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id BIGINT NOT NULL,
                    username VARCHAR(100),
                    api_type VARCHAR(50) NOT NULL,
                    call_time DATETIME NOT NULL,
                    success BOOLEAN,
                    error_message VARCHAR(500)
                )
                """);
            log.info("成功创建t_ai_call_log表");
            // 创建索引加速按时间查询
            jdbcTemplate.execute("""
                CREATE INDEX IF NOT EXISTS idx_ai_call_time ON t_ai_call_log(call_time)
                """);
            jdbcTemplate.execute("""
                CREATE INDEX IF NOT EXISTS idx_ai_call_username ON t_ai_call_log(username)
                """);
        } catch (Exception e) {
            log.warn("创建t_ai_call_log表失败: {}", e.getMessage());
        }

        try {
            // v2.3.1: 补全项目创建者的成员记录（之前createUserId硬编码跳过成员检查）
            // Hibernate EnumType.ORDINAL: CREATOR=0, ADMIN=1, MEMBER=2
            int migratedCount = jdbcTemplate.update("""
                INSERT OR IGNORE INTO t_project_member (project_id, user_id, role, create_time, update_time)
                SELECT p.id, p.create_user_id, 1, datetime('now'), datetime('now')
                FROM t_project p
                WHERE p.create_user_id IS NOT NULL
                  AND NOT EXISTS (
                      SELECT 1 FROM t_project_member pm
                      WHERE pm.project_id = p.id AND pm.user_id = p.create_user_id
                  )
                """);
            log.info("补全项目创建者成员记录: 迁移 {} 条", migratedCount);

            // 将旧的 CREATOR(ordinal=0) 角色统一更新为 ADMIN(ordinal=1)
            int updatedCount = jdbcTemplate.update(
                "UPDATE t_project_member SET role = 1, update_time = datetime('now') WHERE role = 0");
            if (updatedCount > 0) {
                log.info("将 CREATOR(0) 角色统一更新为 ADMIN(1): {} 条", updatedCount);
            }
        } catch (Exception e) {
            log.warn("补全项目创建者成员记录失败: {}", e.getMessage());
        }
    }

    /**
     * 插入默认权限定义
     */
    private void insertDefaultPermissions() {
        String[][] perms = {
            // 仪表盘
            {"仪表盘-页面访问", "dashboard:view", "仪表盘", "PAGE", "1"},
            // 业务管理
            {"项目管理-页面访问", "project:view", "业务管理", "PAGE", "10"},
            {"项目管理-创建", "project:create", "业务管理", "BUTTON", "11"},
            {"项目管理-编辑", "project:edit", "业务管理", "BUTTON", "12"},
            {"项目管理-删除", "project:delete", "业务管理", "BUTTON", "13"},
            {"接口管理-页面访问", "api:view", "业务管理", "PAGE", "20"},
            {"接口管理-创建", "api:create", "业务管理", "BUTTON", "21"},
            {"接口管理-编辑", "api:edit", "业务管理", "BUTTON", "22"},
            {"接口管理-删除", "api:delete", "业务管理", "BUTTON", "23"},
            {"代码模板-页面访问", "code-template:view", "业务管理", "PAGE", "30"},
            {"代码模板-创建", "code-template:create", "业务管理", "BUTTON", "31"},
            {"代码模板-编辑", "code-template:edit", "业务管理", "BUTTON", "32"},
            {"代码模板-删除", "code-template:delete", "业务管理", "BUTTON", "33"},
            // AI 对话
            {"AI对话-页面访问", "ai-chat:view", "AI对话", "PAGE", "40"},
            // 数据统计
            {"数据统计-页面访问", "statistics:view", "数据统计", "PAGE", "50"},
            // 权限管理
            {"权限管理-页面访问", "permission:view", "权限管理", "PAGE", "60"},
            {"角色管理-页面访问", "role:view", "权限管理", "PAGE", "61"},
            {"角色管理-创建", "role:create", "权限管理", "BUTTON", "62"},
            {"角色管理-编辑", "role:edit", "权限管理", "BUTTON", "63"},
            {"角色管理-删除", "role:delete", "权限管理", "BUTTON", "64"},
            {"权限分配-编辑", "permission:assign", "权限管理", "BUTTON", "65"},
            // 系统管理
            {"邮件模板-页面访问", "email-template:view", "系统管理", "PAGE", "70"},
            {"邮件模板-创建", "email-template:create", "系统管理", "BUTTON", "71"},
            {"邮件模板-编辑", "email-template:edit", "系统管理", "BUTTON", "72"},
            {"邮件模板-删除", "email-template:delete", "系统管理", "BUTTON", "73"},
            {"用户管理-页面访问", "user:view", "权限管理", "PAGE", "66"},
            {"用户管理-创建", "user:create", "权限管理", "BUTTON", "67"},
            {"用户管理-编辑", "user:edit", "权限管理", "BUTTON", "68"},
            {"用户管理-删除", "user:delete", "权限管理", "BUTTON", "69"},
            {"AI设置-页面访问", "ai-settings:view", "系统管理", "PAGE", "90"},
            {"系统设置-页面访问", "settings:view", "系统管理", "PAGE", "100"},
        };

        for (String[] perm : perms) {
            try {
                jdbcTemplate.update(
                    "INSERT OR IGNORE INTO t_permission (name, code, group_name, type, sort_order, create_time, update_time) VALUES (?, ?, ?, ?, ?, datetime('now'), datetime('now'))",
                    perm[0], perm[1], perm[2], perm[3], Integer.parseInt(perm[4])
                );
            } catch (Exception e) {
                log.warn("插入权限失败: {} - {}", perm[1], e.getMessage());
            }
        }

        // 修正已有数据：将用户管理权限的 group_name 从"系统管理"更正为"权限管理"
        try {
            int updated = jdbcTemplate.update(
                "UPDATE t_permission SET group_name = '权限管理' WHERE code IN ('user:view', 'user:create', 'user:edit', 'user:delete') AND group_name = '系统管理'"
            );
            if (updated > 0) {
                log.info("已修正 {} 条用户管理权限的 group_name 从'系统管理'到'权限管理'", updated);
            }
        } catch (Exception e) {
            log.warn("修正用户管理权限 group_name 失败: {}", e.getMessage());
        }
    }
}
