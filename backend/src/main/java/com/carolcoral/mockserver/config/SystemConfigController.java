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
import org.springframework.web.bind.annotation.*;

/**
 * 系统配置Controller
 * 用于管理系统级别的配置，如语言设置
 *
 * @author carolcoral
 * @version 1.0
 * @since 2026-03-06
 */
@Tag(name = "系统配置", description = "系统配置管理接口")
@RestController
@RequestMapping("/system-config")
public class SystemConfigController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SystemConfigController.class);

    /**
     * 构造器
     */
    public SystemConfigController(SystemConfigService systemConfigService) {
        this.systemConfigService = systemConfigService;
    }

    /**
     * 获取系统配置
     *
     * @return 系统配置
     */
    @GetMapping
    @Operation(summary = "获取系统配置")
    public ApiResponse<SystemConfigDTO> getSystemConfig() {
        SystemConfigDTO config = new SystemConfigDTO();
        config.setDefaultLanguage(systemConfigService.getDefaultLanguage());

        // 获取其他系统配置（如有需要）
        // 这里可以扩展其他配置项

        return ApiResponse.success(config);
    }

    /**
     * 更新系统默认语言
     *
     * @param dto 语言配置DTO
     * @return 操作结果
     */
    @PostMapping("/default-language")
    @Operation(summary = "更新系统默认语言")
    public ApiResponse<Void> updateDefaultLanguage(@RequestBody DefaultLanguageDTO dto) {
        systemConfigService.saveConfig("defaultLanguage", dto.getLanguage(), "系统默认语言");
        return ApiResponse.success();
    }
}

/**
 * 系统配置DTO
 */
class SystemConfigDTO {
    private String defaultLanguage;

    public String getDefaultLanguage() {
        return defaultLanguage;
    }

    public void setDefaultLanguage(String defaultLanguage) {
        this.defaultLanguage = defaultLanguage;
    }
}

/**
 * 默认语言更新DTO
 */
class DefaultLanguageDTO {
    private String language;

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}

