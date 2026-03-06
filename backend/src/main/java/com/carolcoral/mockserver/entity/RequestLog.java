/**
* Copyright (c) 2026, XINDU.SITE
* All Rights Reserved.
* XINDU.SITE CONFIDENTIAL
*/

package com.carolcoral.mockserver.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 请求日志实体类
 * 用于记录自定义接口的请求日志
 *
 * @author carolcoral
 * @version 1.0
 * @since 2026-03-06
 */
@Schema(description = "请求日志实体")
@Entity
@Table(name = "t_request_log", indexes = {
    @Index(name = "idx_mock_api_id", columnList = "mock_api_id"),
    @Index(name = "idx_request_time", columnList = "request_time")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestLog {

    @Schema(description = "日志ID", example = "1")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(description = "请求的接口ID", example = "1")
    @Column(nullable = false, name = "mock_api_id")
    private Long mockApiId;

    @Schema(description = "所属项目ID", example = "1")
    @Column(nullable = false, name = "project_id")
    private Long projectId;

    @Schema(description = "请求方法", example = "GET")
    @Column(nullable = false, length = 10, name = "method")
    private String method;

    @Schema(description = "请求路径", example = "/api/user/login")
    @Column(nullable = false, length = 200, name = "path")
    private String path;

    @Schema(description = "请求时间")
    @Column(nullable = false, name = "request_time")
    private LocalDateTime requestTime;

    @Schema(description = "响应状态码", example = "200")
    @Column(name = "status_code")
    private Integer statusCode;

    @Schema(description = "响应时间（毫秒）", example = "150")
    @Column(name = "response_time")
    private Long responseTime;

    @Schema(description = "请求IP", example = "192.168.1.1")
    @Column(length = 50, name = "request_ip")
    private String requestIp;

    @Schema(description = "用户ID（如果已登录）", example = "1")
    @Column(name = "user_id")
    private Long userId;

    @ManyToOne
    @JoinColumn(name = "mock_api_id", insertable = false, updatable = false)
    @Schema(description = "请求的接口")
    private MockApi mockApi;

    /**
     * 持久化前回调方法
     * 设置请求时间
     */
    @PrePersist
    protected void onCreate() {
        if (requestTime == null) {
            requestTime = LocalDateTime.now();
        }
    }
}
