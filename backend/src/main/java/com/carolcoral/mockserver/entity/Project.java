package com.carolcoral.mockserver.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 项目实体类
 *
 * @author carolcoral
 */
@Schema(description = "项目实体")
@Entity
@Table(name = "t_project")
@Data
public class Project {

    @Schema(description = "项目ID", example = "1")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(description = "项目名称", example = "电商平台API")
    @Column(nullable = false, length = 100)
    private String name;

    @Schema(description = "项目描述", example = "电商平台的API接口模拟")
    @Column(length = 500)
    private String description;

    @Schema(description = "项目编码（唯一）", example = "ecmall")
    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Schema(description = "是否启用", example = "true")
    @Column(nullable = false)
    private Boolean enabled = true;

    @Schema(description = "创建时间")
    @Column(nullable = false, updatable = false)
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @Column(nullable = false)
    private LocalDateTime updateTime;

    @Schema(description = "创建人ID", example = "1")
    @Column(nullable = false)
    private Long createUserId;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "t_project_user",
            joinColumns = @JoinColumn(name = "project_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @Schema(description = "项目成员列表")
    private List<User> members = new ArrayList<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Schema(description = "项目下的API列表")
    private List<MockApi> mockApis = new ArrayList<>();

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
