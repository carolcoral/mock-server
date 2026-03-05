package com.carolcoral.mockserver.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@Order(1)
public class DatabaseMigration implements CommandLineRunner {

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
    }
}
