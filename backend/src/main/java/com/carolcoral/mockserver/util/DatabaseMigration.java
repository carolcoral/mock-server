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
    }
}
