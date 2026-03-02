package com.carolcoral.mockserver.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 自定义接口实体类
 *
 * @author carolcoral
 */
@Schema(description = "自定义接口实体")
@Entity
@Table(name = "t_mock_api")
@Data
public class MockApi {

    @Schema(description = "接口ID", example = "1")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(description = "接口名称", example = "用户登录接口")
    @Column(nullable = false, length = 100)
    private String name;

    @Schema(description = "接口路径", example = "/api/user/login")
    @Column(nullable = false, length = 200)
    private String path;

    @Schema(description = "请求方法", example = "POST", allowableValues = {"GET", "POST", "PUT", "DELETE", "PATCH"})
    @Column(nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private HttpMethod method = HttpMethod.GET;

    @Schema(description = "请求类型", example = "HTTP", allowableValues = {"HTTP", "WEBSOCKET"})
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private RequestType requestType = RequestType.HTTP;

    @Schema(description = "接口描述", example = "用户登录验证接口")
    @Column(length = 500)
    private String description;

    @Schema(description = "是否启用", example = "true")
    @Column(nullable = false)
    private Boolean enabled = true;

    @Schema(description = "默认响应延迟（毫秒）", example = "100")
    @Column
    private Integer responseDelay;

    @Schema(description = "是否启用随机返回", example = "false")
    @Column(nullable = false)
    private Boolean enableRandom = false;

    @Schema(description = "创建时间")
    @Column(nullable = false, updatable = false)
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @Column(nullable = false)
    private LocalDateTime updateTime;

    @Schema(description = "创建人ID", example = "1")
    @Column(nullable = false)
    private Long createUserId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    @Schema(description = "所属项目")
    private Project project;

    @OneToMany(mappedBy = "mockApi", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("statusCode ASC")
    @Schema(description = "接口响应列表")
    private List<MockResponse> responses = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }

    /**
     * HTTP请求方法枚举
     */
    public enum HttpMethod {
        GET, POST, PUT, DELETE, PATCH
    }

    /**
     * 请求类型枚举
     */
    public enum RequestType {
        HTTP, WEBSOCKET
    }
}
