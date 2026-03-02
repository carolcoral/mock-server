package com.carolcoral.mockserver.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 接口响应实体类
 *
 * @author carolcoral
 */
@Schema(description = "接口响应实体")
@Entity
@Table(name = "t_mock_response")
@Data
public class MockResponse {

    @Schema(description = "响应ID", example = "1")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(description = "HTTP状态码", example = "200")
    @Column(nullable = false)
    private Integer statusCode;

    @Schema(description = "响应内容类型", example = "application/json")
    @Column(nullable = false, length = 100)
    private String contentType = "application/json";

    @Schema(description = "响应头（JSON格式）", example = "{\"X-Custom-Header\": \"value\"}")
    @Column(columnDefinition = "TEXT")
    private String headers;

    @Schema(description = "响应体", example = "{\"code\": 200, \"message\": \"success\"}")
    @Column(columnDefinition = "TEXT", nullable = false)
    private String responseBody;

    @Schema(description = "权重（用于随机返回）", example = "50")
    @Column
    private Integer weight = 100;

    @Schema(description = "条件表达式", example = "$.userId == '123'")
    @Column(length = 500)
    private String condition;

    @Schema(description = "条件描述", example = "当userId等于123时返回此响应")
    @Column(length = 200)
    private String conditionDesc;

    @Schema(description = "是否启用", example = "true")
    @Column(nullable = false)
    private Boolean enabled = true;

    @Schema(description = "创建时间")
    @Column(nullable = false, updatable = false)
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @Column(nullable = false)
    private LocalDateTime updateTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "api_id", nullable = false)
    @Schema(description = "所属接口")
    private MockApi mockApi;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}
