/**
* Copyright (c) 2026, XINDU.SITE，Author: LXW
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
    private Boolean enabled = true;

    @Schema(description = "优先级", example = "HIGH")
    @Column(length = 20)
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
     * 默认构造器
     */
    public SystemAnnouncement() {
    }

    /**
     * 全参构造器
     */
    public SystemAnnouncement(Long id, String title, String content, Boolean enabled, String priority, Long createUserId, LocalDateTime createTime, LocalDateTime updateTime) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.enabled = enabled;
        this.priority = priority;
        this.createUserId = createUserId;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }

    /**
     * Builder方法
     */
    public static SystemAnnouncementBuilder builder() {
        return new SystemAnnouncementBuilder();
    }

    /**
     * Builder类
     */
    public static class SystemAnnouncementBuilder {
        private Long id;
        private String title;
        private String content;
        private Boolean enabled = true;
        private String priority = "NORMAL";
        private Long createUserId;
        private LocalDateTime createTime;
        private LocalDateTime updateTime;

        public SystemAnnouncementBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public SystemAnnouncementBuilder title(String title) {
            this.title = title;
            return this;
        }

        public SystemAnnouncementBuilder content(String content) {
            this.content = content;
            return this;
        }

        public SystemAnnouncementBuilder enabled(Boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public SystemAnnouncementBuilder priority(String priority) {
            this.priority = priority;
            return this;
        }

        public SystemAnnouncementBuilder createUserId(Long createUserId) {
            this.createUserId = createUserId;
            return this;
        }

        public SystemAnnouncementBuilder createTime(LocalDateTime createTime) {
            this.createTime = createTime;
            return this;
        }

        public SystemAnnouncementBuilder updateTime(LocalDateTime updateTime) {
            this.updateTime = updateTime;
            return this;
        }

        public SystemAnnouncement build() {
            SystemAnnouncement announcement = new SystemAnnouncement();
            announcement.id = id;
            announcement.title = title;
            announcement.content = content;
            announcement.enabled = enabled;
            announcement.priority = priority;
            announcement.createUserId = createUserId;
            announcement.createTime = createTime;
            announcement.updateTime = updateTime;
            return announcement;
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public Long getCreateUserId() {
        return createUserId;
    }

    public void setCreateUserId(Long createUserId) {
        this.createUserId = createUserId;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

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
