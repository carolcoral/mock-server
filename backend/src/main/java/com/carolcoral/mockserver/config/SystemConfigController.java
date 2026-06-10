/**
* Copyright (c) 2026, XINDU.SITE
* All Rights Reserved.
* XINDU.SITE CONFIDENTIAL
*/

package com.carolcoral.mockserver.config;

import com.carolcoral.mockserver.dto.ApiResponse;
import com.carolcoral.mockserver.service.SystemConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 系统配置Controller
 * 用于管理系统级别的配置，如语言设置、日期格式、Mock超时配置等
 *
 * @author carolcoral
 * @version 1.1
 * @since 2026-03-06
 */
@Tag(name = "系统配置", description = "系统配置管理接口")
@RestController
@RequestMapping("/api/system-config")
public class SystemConfigController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SystemConfigController.class);

    private final SystemConfigService systemConfigService;

    /**
     * 构造器
     */
    public SystemConfigController(SystemConfigService systemConfigService) {
        this.systemConfigService = systemConfigService;
    }

    /**
     * 获取系统配置
     * <p>返回当前系统级别的配置信息，包括默认语言、Mock超时配置等。</p>
     *
     * @return 系统配置信息
     */
    @GetMapping
    @Operation(summary = "获取系统配置")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<SystemConfigDTO> getSystemConfig() {
        SystemConfigDTO config = new SystemConfigDTO();
        config.setDefaultLanguage(systemConfigService.getDefaultLanguage());
        config.setDateFormat(systemConfigService.getConfig("dateFormat"));
        config.setDefaultResponseDelay(parseIntConfig("defaultResponseDelay", 0));
        config.setMaxResponseDelay(parseIntConfig("maxResponseDelay", 5000));
        config.setEnableRequestLog(parseBooleanConfig("enableRequestLog", true));
        config.setLogRetentionDays(parseIntConfig("logRetentionDays", 30));
        config.setMaxRequestBodySize(parseIntConfig("maxRequestBodySize", 10));
        config.setAxiosTimeout(parseIntConfig("axiosTimeout", 30000));
        return ApiResponse.success(config);
    }

    /**
     * 更新系统语言配置
     * <p>需要管理员权限。更新后会持久化到数据库，所有用户重新登录后将使用新的语言设置。</p>
     *
     * @param dto 语言配置DTO，包含 {@code language} 字段（如 zh-CN, en-US, ja-JP）
     * @return 操作结果
     */
    @PostMapping("/language")
    @Operation(summary = "更新系统语言")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> updateLanguage(@RequestBody DefaultLanguageDTO dto) {
        systemConfigService.saveConfig("defaultLanguage", dto.getLanguage(), "系统默认语言");
        log.info("管理员更新系统语言为: {}", dto.getLanguage());
        return ApiResponse.success();
    }

    /**
     * 更新系统日期格式配置
     * <p>需要管理员权限。更新后会持久化到数据库。</p>
     *
     * @param dto 日期格式配置DTO，包含 {@code dateFormat} 字段（如 YYYY-MM-DD, DD/MM/YYYY）
     * @return 操作结果
     */
    @PostMapping("/date-format")
    @Operation(summary = "更新系统日期格式")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> updateDateFormat(@RequestBody DateFormatDTO dto) {
        systemConfigService.saveConfig("dateFormat", dto.getDateFormat(), "系统日期格式");
        log.info("管理员更新系统日期格式为: {}", dto.getDateFormat());
        return ApiResponse.success();
    }

    /**
     * 更新Mock配置（响应延迟、日志、请求体大小限制等）
     * <p>需要管理员权限。更新后会持久化到数据库。</p>
     *
     * @param dto Mock配置DTO
     * @return 操作结果
     */
    @PostMapping("/mock")
    @Operation(summary = "更新Mock配置")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> updateMockConfig(@RequestBody MockConfigDTO dto) {
        if (dto.getDefaultResponseDelay() != null) {
            systemConfigService.saveConfig("defaultResponseDelay", String.valueOf(dto.getDefaultResponseDelay()), "默认响应延迟（毫秒）");
        }
        if (dto.getMaxResponseDelay() != null) {
            systemConfigService.saveConfig("maxResponseDelay", String.valueOf(dto.getMaxResponseDelay()), "最大响应延迟（毫秒）");
        }
        if (dto.getEnableRequestLog() != null) {
            systemConfigService.saveConfig("enableRequestLog", String.valueOf(dto.getEnableRequestLog()), "是否启用请求日志");
        }
        if (dto.getLogRetentionDays() != null) {
            systemConfigService.saveConfig("logRetentionDays", String.valueOf(dto.getLogRetentionDays()), "日志保留天数");
        }
        if (dto.getMaxRequestBodySize() != null) {
            systemConfigService.saveConfig("maxRequestBodySize", String.valueOf(dto.getMaxRequestBodySize()), "最大请求体大小（MB）");
        }
        if (dto.getAxiosTimeout() != null) {
            systemConfigService.saveConfig("axiosTimeout", String.valueOf(dto.getAxiosTimeout()), "前端Axios请求超时时间（毫秒）");
        }
        log.info("管理员更新Mock配置: defaultResponseDelay={}, maxResponseDelay={}, enableRequestLog={}, logRetentionDays={}, maxRequestBodySize={}, axiosTimeout={}",
                dto.getDefaultResponseDelay(), dto.getMaxResponseDelay(), dto.getEnableRequestLog(),
                dto.getLogRetentionDays(), dto.getMaxRequestBodySize(), dto.getAxiosTimeout());
        return ApiResponse.success();
    }

    /**
     * 解析整数型配置，不存在时返回默认值
     */
    private int parseIntConfig(String key, int defaultValue) {
        String value = systemConfigService.getConfig(key);
        if (value != null && !value.isEmpty()) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                log.warn("配置项 {} 的值 '{}' 无法解析为整数，使用默认值 {}", key, value, defaultValue);
            }
        }
        return defaultValue;
    }

    /**
     * 解析布尔型配置，不存在时返回默认值
     */
    private boolean parseBooleanConfig(String key, boolean defaultValue) {
        String value = systemConfigService.getConfig(key);
        if (value != null && !value.isEmpty()) {
            return Boolean.parseBoolean(value);
        }
        return defaultValue;
    }
}

/**
 * 系统配置DTO
 */
class SystemConfigDTO {
    /** 系统默认语言，如 zh-CN, en-US, ja-JP */
    private String defaultLanguage;
    /** 系统日期格式，如 YYYY-MM-DD, DD/MM/YYYY */
    private String dateFormat;
    /** 默认响应延迟（毫秒） */
    private Integer defaultResponseDelay;
    /** 最大响应延迟（毫秒） */
    private Integer maxResponseDelay;
    /** 是否启用请求日志 */
    private Boolean enableRequestLog;
    /** 日志保留天数 */
    private Integer logRetentionDays;
    /** 最大请求体大小（MB） */
    private Integer maxRequestBodySize;
    /** 前端Axios请求超时时间（毫秒） */
    private Integer axiosTimeout;

    public String getDefaultLanguage() {
        return defaultLanguage;
    }

    public void setDefaultLanguage(String defaultLanguage) {
        this.defaultLanguage = defaultLanguage;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public Integer getDefaultResponseDelay() { return defaultResponseDelay; }
    public void setDefaultResponseDelay(Integer defaultResponseDelay) { this.defaultResponseDelay = defaultResponseDelay; }

    public Integer getMaxResponseDelay() { return maxResponseDelay; }
    public void setMaxResponseDelay(Integer maxResponseDelay) { this.maxResponseDelay = maxResponseDelay; }

    public Boolean getEnableRequestLog() { return enableRequestLog; }
    public void setEnableRequestLog(Boolean enableRequestLog) { this.enableRequestLog = enableRequestLog; }

    public Integer getLogRetentionDays() { return logRetentionDays; }
    public void setLogRetentionDays(Integer logRetentionDays) { this.logRetentionDays = logRetentionDays; }

    public Integer getMaxRequestBodySize() { return maxRequestBodySize; }
    public void setMaxRequestBodySize(Integer maxRequestBodySize) { this.maxRequestBodySize = maxRequestBodySize; }

    public Integer getAxiosTimeout() { return axiosTimeout; }
    public void setAxiosTimeout(Integer axiosTimeout) { this.axiosTimeout = axiosTimeout; }
}

/**
 * 默认语言更新DTO
 */
class DefaultLanguageDTO {
    /** 语言代码，如 zh-CN, en-US, ja-JP */
    private String language;

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}

/**
 * 日期格式更新DTO
 */
class DateFormatDTO {
    /** 日期格式，如 YYYY-MM-DD, DD/MM/YYYY, MM/DD/YYYY */
    private String dateFormat;

    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }
}

/**
 * Mock配置DTO
 */
class MockConfigDTO {
    /** 默认响应延迟（毫秒） */
    private Integer defaultResponseDelay;
    /** 最大响应延迟（毫秒） */
    private Integer maxResponseDelay;
    /** 是否启用请求日志 */
    private Boolean enableRequestLog;
    /** 日志保留天数 */
    private Integer logRetentionDays;
    /** 最大请求体大小（MB） */
    private Integer maxRequestBodySize;
    /** 前端Axios请求超时时间（毫秒） */
    private Integer axiosTimeout;

    public Integer getDefaultResponseDelay() { return defaultResponseDelay; }
    public void setDefaultResponseDelay(Integer defaultResponseDelay) { this.defaultResponseDelay = defaultResponseDelay; }

    public Integer getMaxResponseDelay() { return maxResponseDelay; }
    public void setMaxResponseDelay(Integer maxResponseDelay) { this.maxResponseDelay = maxResponseDelay; }

    public Boolean getEnableRequestLog() { return enableRequestLog; }
    public void setEnableRequestLog(Boolean enableRequestLog) { this.enableRequestLog = enableRequestLog; }

    public Integer getLogRetentionDays() { return logRetentionDays; }
    public void setLogRetentionDays(Integer logRetentionDays) { this.logRetentionDays = logRetentionDays; }

    public Integer getMaxRequestBodySize() { return maxRequestBodySize; }
    public void setMaxRequestBodySize(Integer maxRequestBodySize) { this.maxRequestBodySize = maxRequestBodySize; }

    public Integer getAxiosTimeout() { return axiosTimeout; }
    public void setAxiosTimeout(Integer axiosTimeout) { this.axiosTimeout = axiosTimeout; }
}

