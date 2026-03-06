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
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 系统公告实体类
 *
 * @author carolcoral
 * @version 1.0
 * @since 2026-03-06
 */
@Schema(description = "系统公告实体")
@Entity
@Table(name = "t_system_announcement")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemAnnouncement {

    @Schema(description = "公告ID", example = "1")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(description = "公告标题", example = "系统升级通知")
    @Column(nullable = false, length = 200)
    private String title;

    @Schema(description = "公告内容（支持Markdown）", example = "## 升级内容\n1. 新增XX功能\n2. 修复XX问题")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Schema(description = "是否启用", example = "true")
    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true;

    @Schema(description = "优先级", example = "HIGH")
    @Column(length = 20)
    @Builder.Default
    private String priority = "NORMAL";

    @Schema(description = "创建人ID", example = "1")
    @Column(nullable = false)
    private Long createUserId;

    @Schema(description = "创建时间")
    @Column(nullable = false, updatable = false)
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @Column(nullable = false)
    private LocalDateTime updateTime;

    /**
     * 持久化前回调方法
     * 设置创建时间和更新时间
     */
    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
    }

    /**
     * 更新前回调方法
     * 设置更新时间
     */
    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }

    /**
     * 公告优先级枚举
     */
    public enum Priority {
        LOW,
        NORMAL,
        HIGH,
        URGENT
    }
}
