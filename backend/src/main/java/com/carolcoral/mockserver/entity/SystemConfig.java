/**
* Copyright (c) 2026, XINDU.SITE
* All Rights Reserved.
* XINDU.SITE CONFIDENTIAL
*/

package com.carolcoral.mockserver.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
* Copyright (c) 2026, XINDU.SITE
* All Rights Reserved.
* XINDU.SITE CONFIDENTIAL
*/

/**
 * 系统配置实体
 * 用于存储系统级别的配置，如默认语言
 *
 * @author carolcoral
 * @version 1.0
 * @since 2026-03-06
 */
@Schema(description = "系统配置实体")
@Entity
@Table(name = "t_system_config")
public class SystemConfig {

    @Schema(description = "配置ID", example = "1")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(description = "配置键", example = "defaultLanguage")
    @Column(nullable = false, unique = true, length = 100)
    private String configKey;

    @Schema(description = "配置值", example = "zh-CN")
    @Column(nullable = false, length = 500)
    private String configValue;

    @Schema(description = "配置描述")
    @Column(length = 500)
    private String description;

    @Schema(description = "创建时间")
    @Column(nullable = false, updatable = false)
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @Column(nullable = false)
    private LocalDateTime updateTime;
